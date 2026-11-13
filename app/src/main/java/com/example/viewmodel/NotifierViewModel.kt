package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.classifier.InstagramNotificationClassifier
import com.example.data.AppDatabase
import com.example.data.NotificationLogEntity
import com.example.data.NotificationRepository
import com.example.data.NotifierStats
import com.example.data.SpecialPersonEntity
import com.example.model.ClassificationResult
import com.example.model.FilterAction
import com.example.model.FilterDecision
import com.example.model.InstagramCategory
import com.example.service.AlwaysOnProtectionService
import com.example.service.InstagramNotificationListenerService
import com.example.service.TestNotificationSender
import android.os.PowerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PresetType(val label: String, val description: String) {
    ONLY_DMS(
        label = "Only DMs",
        description = "Allow only direct chats & story replies. Suppress Reels, Photos & Videos, and social clutter."
    ),
    ONLY_REELS(
        label = "Only Reels",
        description = "Allow only incoming Reels and video clips. Suppress DMs, Photos & Videos, and social clutter."
    ),
    ONLY_PHOTOS_AND_VIDEOS(
        label = "Only Photos & Videos",
        description = "Allow only feed photos, carousels, and video posts. Suppress Reels, DMs, and social clutter."
    ),
    SPECIAL_PERSONS_REELS(
        label = "Special Persons Reels",
        description = "Allow DMs plus Reels exclusively from your Special Persons exception list."
    ),
    PERSONAL_ONLY(
        label = "Personal Only",
        description = "Allow DMs, Story Replies & Mentions. Suppress Reels, Photos & Videos, Likes, and Follows."
    ),
    BALANCED(
        label = "Balanced",
        description = "Allow DMs, Replies, Mentions & Comments. Suppress Reels, Likes, Follows & Live."
    ),
    ALLOW_ALL(
        label = "Allow All",
        description = "Allow all notifications without dismissing any."
    ),
    SUPPRESS_ALL_SOCIAL(
        label = "Focus Shield",
        description = "Strict mode: Dismiss all non-essential notifications."
    )
}

class NotifierViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = NotificationRepository(database.notificationDao(), application)
    private val classifier = InstagramNotificationClassifier()

    val safeModeEnabled: StateFlow<Boolean> = repository.safeModeEnabled
    val historyRetention: StateFlow<com.example.model.HistoryRetention> = repository.historyRetention

    val preferences: StateFlow<Map<InstagramCategory, FilterAction>> =
        repository.categoryPreferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InstagramCategory.entries.associateWith { it.defaultAction }
        )

    val stats: StateFlow<NotifierStats> =
        repository.stats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotifierStats()
        )

    val allLogs: StateFlow<List<NotificationLogEntity>> =
        repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentInspectorLogs: StateFlow<List<NotificationLogEntity>> =
        repository.recentInspectorLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isListenerConnected: StateFlow<Boolean> =
        InstagramNotificationListenerService.isListenerConnected

    val latestLiveNotification: StateFlow<NotificationLogEntity?> =
        InstagramNotificationListenerService.latestInspectedNotification

    // Special Persons Exceptions
    val specialPersons: StateFlow<List<SpecialPersonEntity>> =
        repository.specialPersons.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Custom Filter Rules ("Starts with...", "Contains...", etc.)
    val customRules: StateFlow<List<com.example.data.CustomFilterRuleEntity>> =
        repository.customRules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monitored Apps (Instagram, WhatsApp, Snapchat, Telegram, etc.)
    val monitoredApps: StateFlow<List<com.example.data.MonitoredAppEntity>> =
        repository.monitoredApps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.getMonitoredAppsSync()
        }
    }

    // 3-5 Second Setup Delay State
    private val _isSettingUpFilter = MutableStateFlow(false)
    val isSettingUpFilter: StateFlow<Boolean> = _isSettingUpFilter.asStateFlow()

    private val _setupProgress = MutableStateFlow(0f)
    val setupProgress: StateFlow<Float> = _setupProgress.asStateFlow()

    private val _setupStatusMessage = MutableStateFlow("")
    val setupStatusMessage: StateFlow<String> = _setupStatusMessage.asStateFlow()

    private val _activeSetupPreset = MutableStateFlow<String?>(null)
    val activeSetupPreset: StateFlow<String?> = _activeSetupPreset.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _historyCategoryFilter = MutableStateFlow<InstagramCategory?>(null)
    val historyCategoryFilter: StateFlow<InstagramCategory?> = _historyCategoryFilter.asStateFlow()

    private val _historyDecisionFilter = MutableStateFlow<FilterDecision?>(null)
    val historyDecisionFilter: StateFlow<FilterDecision?> = _historyDecisionFilter.asStateFlow()

    private val _dryRunResult = MutableStateFlow<ClassificationResult?>(null)
    val dryRunResult: StateFlow<ClassificationResult?> = _dryRunResult.asStateFlow()

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun setCategoryFilter(category: InstagramCategory?) {
        _historyCategoryFilter.value = category
    }

    fun setDecisionFilter(decision: FilterDecision?) {
        _historyDecisionFilter.value = decision
    }

    fun addSpecialPerson(instagramId: String, nickname: String = "", reelsOnly: Boolean = true) {
        viewModelScope.launch {
            repository.addSpecialPerson(instagramId, nickname, reelsOnly)
        }
    }

    fun updateSpecialPerson(id: Long, instagramId: String, nickname: String, reelsOnly: Boolean) {
        viewModelScope.launch {
            repository.updateSpecialPerson(id, instagramId, nickname, reelsOnly)
        }
    }

    fun removeSpecialPerson(id: Long) {
        viewModelScope.launch {
            repository.removeSpecialPerson(id)
        }
    }

    fun addCustomRule(
        appPackage: String = "com.instagram.android",
        appName: String = "Instagram",
        matchType: String = "STARTS_WITH",
        pattern: String,
        targetField: String = "TEXT",
        action: String = "SUPPRESS"
    ) {
        viewModelScope.launch {
            repository.addCustomRule(
                appPackage = appPackage,
                appName = appName,
                matchType = matchType,
                pattern = pattern,
                targetField = targetField,
                action = action
            )
        }
    }

    fun removeCustomRule(id: Long) {
        viewModelScope.launch {
            repository.removeCustomRule(id)
        }
    }

    fun toggleCustomRule(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleCustomRule(id, isEnabled)
        }
    }

    fun addMonitoredApp(packageName: String, appName: String) {
        viewModelScope.launch {
            repository.addMonitoredApp(packageName, appName)
        }
    }

    fun removeMonitoredApp(packageName: String) {
        viewModelScope.launch {
            repository.removeMonitoredApp(packageName)
        }
    }

    fun toggleMonitoredApp(packageName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMonitoredApp(packageName, isEnabled)
        }
    }

    fun toggleCategoryAction(category: InstagramCategory) {
        viewModelScope.launch {
            val currentMap = repository.getPreferencesSync()
            val currentAction = currentMap[category] ?: category.defaultAction
            val newAction = if (currentAction == FilterAction.ALLOW) FilterAction.SUPPRESS else FilterAction.ALLOW
            repository.setCategoryAction(category, newAction)
        }
    }

    fun setCategoryAction(category: InstagramCategory, action: FilterAction) {
        viewModelScope.launch {
            repository.setCategoryAction(category, action)
        }
    }

    /**
     * Applies filter change with a 3-5 second animated setup period,
     * fulfilling the user requirement: "if select any option like only dm , only reels
     * like take some 3-5 seconds make changes and setup that change".
     */
    fun applyPresetWithSetup(presetType: PresetType) {
        viewModelScope.launch {
            _isSettingUpFilter.value = true
            _activeSetupPreset.value = presetType.label

            // Step 1 (~1s): Analyze parameters
            _setupProgress.value = 0.18f
            _setupStatusMessage.value = "Analyzing notification filter for ${presetType.label}..."
            delay(1000)

            // Step 2 (~1.1s): Calibrate heuristic classifier
            _setupProgress.value = 0.52f
            _setupStatusMessage.value = "Calibrating heuristic filters & Safe Mode safeguards..."
            delay(1100)

            // Step 3 (~1.1s): Synchronize rules & special persons
            _setupProgress.value = 0.84f
            _setupStatusMessage.value = "Synchronizing Special Persons exceptions & listener..."
            delay(1100)

            // Commit rule changes into repository
            applyPresetInternal(presetType)

            // Step 4 (0.6s): Finalize
            _setupProgress.value = 1.0f
            _setupStatusMessage.value = "${presetType.label} filter active & verified!"
            delay(600)

            _isSettingUpFilter.value = false
            _activeSetupPreset.value = null
        }
    }

    private suspend fun applyPresetInternal(presetType: PresetType) {
        val presetMap = when (presetType) {
            PresetType.ONLY_DMS -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
                InstagramCategory.STORY_REPLIES to FilterAction.ALLOW,
                InstagramCategory.MENTIONS to FilterAction.SUPPRESS,
                InstagramCategory.REELS to FilterAction.SUPPRESS,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.ONLY_REELS -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.SUPPRESS,
                InstagramCategory.STORY_REPLIES to FilterAction.SUPPRESS,
                InstagramCategory.MENTIONS to FilterAction.SUPPRESS,
                InstagramCategory.REELS to FilterAction.ALLOW,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.ONLY_PHOTOS_AND_VIDEOS -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.SUPPRESS,
                InstagramCategory.STORY_REPLIES to FilterAction.SUPPRESS,
                InstagramCategory.MENTIONS to FilterAction.SUPPRESS,
                InstagramCategory.REELS to FilterAction.SUPPRESS,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.ALLOW,
                InstagramCategory.SHARED_POSTS to FilterAction.ALLOW,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.SPECIAL_PERSONS_REELS -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
                InstagramCategory.STORY_REPLIES to FilterAction.ALLOW,
                InstagramCategory.MENTIONS to FilterAction.ALLOW,
                InstagramCategory.REELS to FilterAction.SUPPRESS, // Bypassed exclusively by Special Persons
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.PERSONAL_ONLY -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
                InstagramCategory.STORY_REPLIES to FilterAction.ALLOW,
                InstagramCategory.MENTIONS to FilterAction.ALLOW,
                InstagramCategory.REELS to FilterAction.SUPPRESS,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.BALANCED -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
                InstagramCategory.STORY_REPLIES to FilterAction.ALLOW,
                InstagramCategory.MENTIONS to FilterAction.ALLOW,
                InstagramCategory.COMMENTS to FilterAction.ALLOW,
                InstagramCategory.REELS to FilterAction.SUPPRESS,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
            PresetType.ALLOW_ALL -> InstagramCategory.entries.associateWith { FilterAction.ALLOW }
            PresetType.SUPPRESS_ALL_SOCIAL -> mapOf(
                InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
                InstagramCategory.STORY_REPLIES to FilterAction.SUPPRESS,
                InstagramCategory.MENTIONS to FilterAction.SUPPRESS,
                InstagramCategory.REELS to FilterAction.SUPPRESS,
                InstagramCategory.PHOTOS_AND_VIDEOS to FilterAction.SUPPRESS,
                InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
                InstagramCategory.LIKES to FilterAction.SUPPRESS,
                InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
                InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
                InstagramCategory.LIVE to FilterAction.SUPPRESS,
                InstagramCategory.OTHER to FilterAction.SUPPRESS,
                InstagramCategory.UNKNOWN to FilterAction.ALLOW
            )
        }
        repository.applyPreset(presetMap)
    }

    fun applyPreset(presetType: PresetType) {
        viewModelScope.launch {
            applyPresetInternal(presetType)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setSafeModeEnabled(enabled: Boolean) {
        repository.setSafeModeEnabled(enabled)
    }

    fun setHistoryRetention(retention: com.example.model.HistoryRetention) {
        viewModelScope.launch {
            repository.setHistoryRetention(retention)
        }
    }

    fun runDryRunClassification(
        title: String,
        text: String,
        channelId: String = "",
        actions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val metadata = InstagramNotificationClassifier.NotificationMetadata(
                packageName = "com.instagram.android",
                title = title,
                text = text,
                channelId = channelId,
                actions = actions
            )
            val prefs = repository.getPreferencesSync()
            val isSafeMode = repository.isSafeModeActiveSync()
            val specialPersons = repository.getSpecialPersonsSync()
            val result = classifier.classify(
                metadata = metadata,
                userPreferences = prefs,
                safeModeEnabled = isSafeMode,
                specialPersons = specialPersons
            )
            _dryRunResult.value = result
        }
    }

    fun sendTestNotification(context: Context, scenario: TestNotificationSender.SimulationScenario) {
        TestNotificationSender.dispatchTestNotification(
            context = context,
            title = scenario.senderTitle,
            text = scenario.messageText,
            addReplyAction = scenario.isMessageStyle
        )
    }

    fun sendCustomTestNotification(
        context: Context,
        title: String,
        text: String,
        addReply: Boolean
    ) {
        TestNotificationSender.dispatchTestNotification(
            context = context,
            title = title,
            text = text,
            addReplyAction = addReply
        )
    }

    fun checkNotificationListenerPermission(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val cn = ComponentName(context, InstagramNotificationListenerService::class.java)
        return flat.contains(cn.flattenToString()) ||
            flat.contains(cn.flattenToShortString()) ||
            flat.contains(context.packageName)
    }

    // Display Over Other Apps (SYSTEM_ALERT_WINDOW)
    private val _isOverlayPermissionGranted = MutableStateFlow(false)
    val isOverlayPermissionGranted: StateFlow<Boolean> = _isOverlayPermissionGranted.asStateFlow()

    // Battery Optimization Exemption
    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    // Notification Listener Status
    private val _isListenerPermissionGranted = MutableStateFlow(false)
    val isListenerPermissionGranted: StateFlow<Boolean> = _isListenerPermissionGranted.asStateFlow()

    // Always-on Background Service
    val isAlwaysOnServiceRunning: StateFlow<Boolean> = AlwaysOnProtectionService.isServiceRunning
    val alwaysOnServiceEnabled: StateFlow<Boolean> = repository.alwaysOnServiceEnabled

    fun refreshSystemPermissions(context: Context) {
        _isListenerPermissionGranted.value = checkNotificationListenerPermission(context)
        _isOverlayPermissionGranted.value = checkOverlayPermission(context)
        _isBatteryOptimizationIgnored.value = checkBatteryOptimizationIgnored(context)

        // Ensure Always-on service is running if enabled and listener is granted
        if (alwaysOnServiceEnabled.value) {
            AlwaysOnProtectionService.start(context)
        }
    }

    fun checkOverlayPermission(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (_: Exception) {
            false
        }
    }

    fun checkBatteryOptimizationIgnored(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun setAlwaysOnServiceEnabled(context: Context, enabled: Boolean) {
        repository.setAlwaysOnServiceEnabled(enabled)
        if (enabled) {
            AlwaysOnProtectionService.start(context)
        } else {
            AlwaysOnProtectionService.stop(context)
        }
    }
}
