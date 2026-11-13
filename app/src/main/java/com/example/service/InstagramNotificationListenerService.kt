package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.classifier.InstagramNotificationClassifier
import com.example.data.AppDatabase
import com.example.data.NotificationLogEntity
import com.example.data.NotificationRepository
import com.example.data.SpecialPersonEntity
import com.example.model.FilterAction
import com.example.model.InstagramCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InstagramNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: NotificationRepository
    private val classifier = InstagramNotificationClassifier()

    // In-memory cache for instant zero-lag classification without DB latency
    @Volatile
    private var cachedPreferences: Map<InstagramCategory, FilterAction> =
        InstagramCategory.entries.associateWith { it.defaultAction }

    @Volatile
    private var cachedSafeMode: Boolean = true

    @Volatile
    private var cachedSpecialPersons: List<SpecialPersonEntity> = emptyList()

    @Volatile
    private var cachedCustomRules: List<com.example.data.CustomFilterRuleEntity> = emptyList()

    @Volatile
    private var cachedMonitoredApps: List<com.example.data.MonitoredAppEntity> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = NotificationRepository(database.notificationDao(), applicationContext)
        _isServiceActive.value = true
        Log.d(TAG, "InstagramNotificationListenerService created")

        // Instant synchronous fast-load of preferences from persistent cache
        try {
            cachedPreferences = repository.getPreferencesFast()
            cachedSafeMode = repository.isSafeModeActiveSync()
        } catch (_: Exception) {}

        // Preload and continuously synchronize in-memory cache
        scope.launch {
            try {
                cachedPreferences = repository.getPreferencesSync()
                cachedSafeMode = repository.isSafeModeActiveSync()
                cachedSpecialPersons = repository.getSpecialPersonsSync()
                cachedCustomRules = repository.getCustomRulesSync()
                cachedMonitoredApps = repository.getMonitoredAppsSync()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing cache", e)
            }

            launch {
                repository.categoryPreferences.collect {
                    cachedPreferences = it
                }
            }
            launch {
                repository.safeModeEnabled.collect {
                    cachedSafeMode = it
                }
            }
            launch {
                repository.specialPersons.collect {
                    cachedSpecialPersons = it
                }
            }
            launch {
                repository.customRules.collect {
                    cachedCustomRules = it
                }
            }
            launch {
                repository.monitoredApps.collect {
                    cachedMonitoredApps = it
                }
            }
        }

        // Start Always-on Protection Service if enabled
        if (repository.isAlwaysOnServiceEnabled()) {
            promoteToForeground()
            AlwaysOnProtectionService.start(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _isServiceActive.value = false
        _isListenerConnected.value = false
        Log.d(TAG, "InstagramNotificationListenerService destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerConnected.value = true
        Log.d(TAG, "Notification listener connected to system successfully")
        try {
            if (::repository.isInitialized && repository.isAlwaysOnServiceEnabled()) {
                promoteToForeground()
                AlwaysOnProtectionService.start(applicationContext)
            }
        } catch (_: Exception) {}
        processActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerConnected.value = false
        Log.d(TAG, "Notification listener disconnected from system, triggering rebind")
        rebindService(applicationContext)
        try {
            if (::repository.isInitialized && repository.isAlwaysOnServiceEnabled()) {
                promoteToForeground()
                AlwaysOnProtectionService.start(applicationContext)
            }
        } catch (_: Exception) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed from recents: ensuring listener continuity")
        try {
            if (::repository.isInitialized && repository.isAlwaysOnServiceEnabled()) {
                promoteToForeground()
            }
        } catch (_: Exception) {}
        rebindService(applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SHIELD_CHANNEL_ID,
                "Notifier Background Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Instagram notification filtering active in real time."
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun promoteToForeground() {
        try {
            createNotificationChannel()
            val openAppIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, SHIELD_CHANNEL_ID)
                .setContentTitle("Notifier Active Protection")
                .setContentText("Real-time Instagram filtering active in background")
                .setSmallIcon(R.drawable.ic_notification_shield)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    SHIELD_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(SHIELD_NOTIFICATION_ID, notification)
            }
            Log.i(TAG, "Promoted InstagramNotificationListenerService to FOREGROUND SERVICE")
        } catch (e: Exception) {
            Log.e(TAG, "Could not promote listener to foreground", e)
        }
    }

    private fun processActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            Log.d(TAG, "Scanning ${active.size} active notifications on connect")
            for (sbn in active) {
                val pkg = sbn.packageName ?: continue
                val isInstagram = pkg == INSTAGRAM_PACKAGE || pkg == INSTAGRAM_LITE_PACKAGE
                val isTestSimulation = pkg == applicationContext.packageName
                val isMonitored = cachedMonitoredApps.any { it.isEnabled && it.packageName.equals(pkg, ignoreCase = true) }
                if (isInstagram || isTestSimulation || isMonitored) {
                    onNotificationPosted(sbn)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed scanning active notifications", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return
        val isInstagram = pkgName == INSTAGRAM_PACKAGE || pkgName == INSTAGRAM_LITE_PACKAGE
        val isTestSimulation = pkgName == applicationContext.packageName
        val isMonitoredApp = cachedMonitoredApps.any { it.isEnabled && it.packageName.equals(pkgName, ignoreCase = true) }

        // Only process Instagram, custom monitored apps, or local simulation notifications
        if (!isInstagram && !isTestSimulation && !isMonitoredApp) {
            return
        }
        if (pkgName == applicationContext.packageName &&
            (sbn.notification.channelId == SHIELD_CHANNEL_ID ||
             sbn.id == SHIELD_NOTIFICATION_ID ||
             sbn.notification.channelId == AlwaysOnProtectionService.CHANNEL_ID ||
             sbn.id == AlwaysOnProtectionService.NOTIFICATION_ID)
        ) {
            return
        }

        // Hold WakeLock to prevent OEM CPU sleep / suspension during background filtering
        var wakeLock: PowerManager.WakeLock? = null
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Notifier:ListenerLock")?.apply {
                setReferenceCounted(false)
                acquire(2500) // Reduced Wakelock max duration for low battery impact (0-3%)
            }
        } catch (_: Exception) {}

        // Instant synchronous metadata extraction and zero-lag classification
        val metadata = NotificationInspectorManager.extractMetadata(sbn)
        val classification = classifier.classify(
            metadata = metadata,
            userPreferences = cachedPreferences,
            safeModeEnabled = cachedSafeMode,
            specialPersons = cachedSpecialPersons,
            customRules = cachedCustomRules
        )

        var dismissedSuccessfully = false

        // ZERO-LAG DISMISSAL: Cancel immediately before asynchronous database logging!
        if (classification.shouldDismiss) {
            try {
                cancelNotification(sbn.key)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try { snoozeNotification(sbn.key, 86400000L) } catch (_: Exception) {}
                }
                dismissedSuccessfully = true
                Log.i(TAG, "Suppressed & dismissed notification: ${metadata.title} - ${metadata.text} [Category: ${classification.category.name}]")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dismiss notification ${sbn.key}, attempting fallback", e)
            }

            try {
                // Secondary fallback using tag & id
                if (sbn.tag != null) {
                    cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                } else {
                    cancelNotification(sbn.packageName, null, sbn.id)
                }
                dismissedSuccessfully = true
            } catch (e2: Exception) {
                Log.e(TAG, "Secondary fallback dismissal failed", e2)
            }

            // Robust OEM Fallback: Retry cancellation aggressively over the next 1.6 seconds
            // Optimized for low battery impact
            scope.launch(Dispatchers.Main) {
                for (i in 1..2) {
                    delay(800) // Wait 800ms between attempts
                    try {
                        cancelNotification(sbn.key)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try { snoozeNotification(sbn.key, 86400000L) } catch (_: Exception) {}
                        }
                        if (sbn.tag != null) {
                            cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                        } else {
                            cancelNotification(sbn.packageName, null, sbn.id)
                        }
                        try { cancelNotifications(arrayOf(sbn.key)) } catch (_: Exception) {}
                    } catch (_: Exception) {}
                }
            }

            // Cleanup orphaned group summary if all child notifications are dismissed
            try {
                val groupKey = sbn.groupKey
                if (!groupKey.isNullOrBlank()) {
                    val active = activeNotifications ?: emptyArray()
                    val groupSiblings = active.filter {
                        it.packageName == sbn.packageName && it.groupKey == groupKey && it.key != sbn.key
                    }
                    if (groupSiblings.size == 1 && (groupSiblings[0].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) {
                        cancelNotification(groupSiblings[0].key)
                        Log.i(TAG, "Dismissed orphaned group summary ${groupSiblings[0].key}")
                    }
                }
            } catch (_: Exception) {}
        } else {
            Log.i(TAG, "Preserved notification: ${metadata.title} [Category: ${classification.category.name}, Decision: ${classification.decision.name}]")
        }

        // Log to database asynchronously without delaying cancellation
        scope.launch {
            try {
                val extras = sbn.notification.extras
                val extrasFormatted = NotificationInspectorManager.formatExtras(extras)
                val actionsJoined = metadata.actions.joinToString(", ")

                val logEntity = NotificationLogEntity(
                    packageName = sbn.packageName,
                    title = metadata.title,
                    text = metadata.text,
                    subText = metadata.subText,
                    channelId = metadata.channelId,
                    notificationCategory = metadata.notificationCategory,
                    actionsJson = actionsJoined,
                    extrasSummary = extrasFormatted,
                    detectedCategory = classification.category.name,
                    confidence = classification.confidence.name,
                    ruleExplanation = classification.ruleExplanation,
                    matchedCues = classification.matchedCues.joinToString(" • "),
                    filterDecision = classification.decision.name,
                    timestamp = System.currentTimeMillis(),
                    dismissedSuccessfully = dismissedSuccessfully
                )

                repository.logNotification(logEntity)
                _latestInspectedNotification.value = logEntity
            } catch (e: Exception) {
                Log.e(TAG, "Error logging notification from $pkgName", e)
            } finally {
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    companion object {
        private const val TAG = "NotifierListener"
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val INSTAGRAM_LITE_PACKAGE = "com.instagram.lite"
        const val SHIELD_CHANNEL_ID = "notifier_always_on_shield"
        const val SHIELD_NOTIFICATION_ID = 9001

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _isListenerConnected = MutableStateFlow(false)
        val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

        private val _latestInspectedNotification = MutableStateFlow<NotificationLogEntity?>(null)
        val latestInspectedNotification: StateFlow<NotificationLogEntity?> = _latestInspectedNotification.asStateFlow()

        /**
         * Re-binds the NotificationListenerService using Android's standard requestRebind API.
         * Note: Toggling component state to DISABLED in the background causes Android 14 to kill the IPC binder.
         */
        fun rebindService(context: Context) {
            try {
                val component = ComponentName(context, InstagramNotificationListenerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        requestRebind(component)
                        Log.i(TAG, "Dispatched requestRebind for $component")
                    } catch (e: Exception) {
                        Log.d(TAG, "requestRebind notice: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to rebind notification listener service", e)
            }
        }
    }
}
