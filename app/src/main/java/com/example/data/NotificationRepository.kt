package com.example.data

import android.content.Context
import com.example.model.FilterAction
import com.example.model.HistoryRetention
import com.example.model.InstagramCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class NotifierStats(
    val totalIntercepted: Int = 0,
    val suppressedCount: Int = 0,
    val allowedCount: Int = 0,
    val keptSafeCount: Int = 0
)

class NotificationRepository(
    private val dao: NotificationDao,
    context: Context? = null
) {
    private val prefs = context?.getSharedPreferences("notifier_settings", Context.MODE_PRIVATE)

    private val _safeModeEnabled = MutableStateFlow(prefs?.getBoolean("safe_mode_enabled", true) ?: true)
    val safeModeEnabled: StateFlow<Boolean> = _safeModeEnabled.asStateFlow()

    private val _alwaysOnServiceEnabled = MutableStateFlow(prefs?.getBoolean("always_on_service_enabled", true) ?: true)
    val alwaysOnServiceEnabled: StateFlow<Boolean> = _alwaysOnServiceEnabled.asStateFlow()

    private val _historyRetention = MutableStateFlow(
        try {
            HistoryRetention.valueOf(prefs?.getString("history_retention", HistoryRetention.SEVEN_DAYS.name) ?: HistoryRetention.SEVEN_DAYS.name)
        } catch (_: Exception) {
            HistoryRetention.SEVEN_DAYS
        }
    )
    val historyRetention: StateFlow<HistoryRetention> = _historyRetention.asStateFlow()

    val allLogs: Flow<List<NotificationLogEntity>> = dao.getAllLogs()

    val recentInspectorLogs: Flow<List<NotificationLogEntity>> = dao.getRecentInspectorLogs()

    val categoryPreferences: Flow<Map<InstagramCategory, FilterAction>> =
        dao.getAllPreferences().map { entities ->
            val map = InstagramCategory.entries.associateWith { it.defaultAction }.toMutableMap()
            entities.forEach { entity ->
                try {
                    val category = InstagramCategory.valueOf(entity.categoryName)
                    val action = FilterAction.valueOf(entity.action)
                    map[category] = action
                } catch (_: Exception) {
                    // ignore invalid entries
                }
            }
            map
        }

    val stats: Flow<NotifierStats> = combine(
        dao.getTotalLogsCount(),
        dao.getSuppressedCount(),
        dao.getAllowedCount(),
        dao.getKeptSafeCount()
    ) { total, suppressed, allowed, keptSafe ->
        NotifierStats(
            totalIntercepted = total,
            suppressedCount = suppressed,
            allowedCount = allowed,
            keptSafeCount = keptSafe
        )
    }

    fun getPreferencesFast(): Map<InstagramCategory, FilterAction> {
        val map = InstagramCategory.entries.associateWith { it.defaultAction }.toMutableMap()
        prefs?.let { sp ->
            InstagramCategory.entries.forEach { cat ->
                val savedAction = sp.getString("cat_pref_${cat.name}", null)
                if (savedAction != null) {
                    try {
                        map[cat] = FilterAction.valueOf(savedAction)
                    } catch (_: Exception) {}
                }
            }
        }
        return map
    }

    suspend fun getPreferencesSync(): Map<InstagramCategory, FilterAction> {
        val map = InstagramCategory.entries.associateWith { it.defaultAction }.toMutableMap()
        val entities = dao.getAllPreferencesSync()
        entities.forEach { entity ->
            try {
                val category = InstagramCategory.valueOf(entity.categoryName)
                val action = FilterAction.valueOf(entity.action)
                map[category] = action
                prefs?.edit()?.putString("cat_pref_${category.name}", action.name)?.apply()
            } catch (_: Exception) {
                // ignore
            }
        }
        return map
    }

    fun isSafeModeActiveSync(): Boolean {
        return _safeModeEnabled.value
    }

    fun setSafeModeEnabled(enabled: Boolean) {
        _safeModeEnabled.value = enabled
        prefs?.edit()?.putBoolean("safe_mode_enabled", enabled)?.apply()
    }

    fun isAlwaysOnServiceEnabled(): Boolean {
        return _alwaysOnServiceEnabled.value
    }

    fun setAlwaysOnServiceEnabled(enabled: Boolean) {
        _alwaysOnServiceEnabled.value = enabled
        prefs?.edit()?.putBoolean("always_on_service_enabled", enabled)?.apply()
    }

    suspend fun setHistoryRetention(retention: HistoryRetention) {
        _historyRetention.value = retention
        prefs?.edit()?.putString("history_retention", retention.name)?.apply()
        pruneExpiredLogs()
    }

    suspend fun pruneExpiredLogs(): Int {
        val currentRetention = _historyRetention.value
        if (currentRetention == HistoryRetention.FOREVER) return 0
        val cutoff = System.currentTimeMillis() - currentRetention.durationMillis
        return dao.pruneLogsOlderThan(cutoff)
    }

    suspend fun setCategoryAction(category: InstagramCategory, action: FilterAction) {
        prefs?.edit()?.putString("cat_pref_${category.name}", action.name)?.apply()
        dao.insertPreference(
            CategoryPreferenceEntity(
                categoryName = category.name,
                action = action.name,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun applyPreset(preset: Map<InstagramCategory, FilterAction>) {
        val editor = prefs?.edit()
        preset.forEach { (cat, act) ->
            editor?.putString("cat_pref_${cat.name}", act.name)
        }
        editor?.apply()

        val entities = preset.map { (cat, act) ->
            CategoryPreferenceEntity(
                categoryName = cat.name,
                action = act.name,
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.insertPreferences(entities)
    }

    suspend fun logNotification(log: NotificationLogEntity): Long {
        val id = dao.insertLog(log)
        pruneExpiredLogs()
        return id
    }

    suspend fun clearHistory() {
        dao.clearAllLogs()
    }

    // Special Persons Exceptions
    val specialPersons: Flow<List<SpecialPersonEntity>> = dao.getAllSpecialPersons()

    suspend fun getSpecialPersonsSync(): List<SpecialPersonEntity> {
        return dao.getAllSpecialPersonsSync()
    }

    suspend fun addSpecialPerson(instagramId: String, nickname: String = "", reelsOnly: Boolean = true): Long {
        val cleanedId = instagramId.trim().removePrefix("@")
        val person = SpecialPersonEntity(
            instagramId = cleanedId,
            nickname = nickname.trim(),
            reelsOnly = reelsOnly,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertSpecialPerson(person)
    }

    suspend fun updateSpecialPerson(id: Long, instagramId: String, nickname: String, reelsOnly: Boolean) {
        val cleanedId = instagramId.trim().removePrefix("@")
        val person = SpecialPersonEntity(
            id = id,
            instagramId = cleanedId,
            nickname = nickname.trim(),
            reelsOnly = reelsOnly,
            createdAt = System.currentTimeMillis() // Optional, keep updated time or old time. We'll just set it to now for simplicity.
        )
        dao.insertSpecialPerson(person)
    }

    suspend fun removeSpecialPerson(id: Long) {
        dao.deleteSpecialPersonById(id)
    }

    suspend fun clearAllSpecialPersons() {
        dao.clearSpecialPersons()
    }

    // Custom Filter Rules
    val customRules: Flow<List<CustomFilterRuleEntity>> = dao.getAllCustomRules()

    suspend fun getCustomRulesSync(): List<CustomFilterRuleEntity> {
        return dao.getAllCustomRulesSync()
    }

    suspend fun addCustomRule(
        appPackage: String = "com.instagram.android",
        appName: String = "Instagram",
        matchType: String = "STARTS_WITH",
        pattern: String,
        targetField: String = "TEXT",
        action: String = "SUPPRESS"
    ): Long {
        val rule = CustomFilterRuleEntity(
            appPackage = appPackage.trim(),
            appName = appName.trim(),
            matchType = matchType.trim().uppercase(),
            pattern = pattern.trim(),
            targetField = targetField.trim().uppercase(),
            action = action.trim().uppercase(),
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertCustomRule(rule)
    }

    suspend fun removeCustomRule(id: Long) {
        dao.deleteCustomRule(id)
    }

    suspend fun toggleCustomRule(id: Long, isEnabled: Boolean) {
        dao.updateCustomRuleEnabled(id, isEnabled)
    }

    suspend fun clearAllCustomRules() {
        dao.clearAllCustomRules()
    }

    // Monitored Apps
    val monitoredApps: Flow<List<MonitoredAppEntity>> = dao.getAllMonitoredApps()

    suspend fun getMonitoredAppsSync(): List<MonitoredAppEntity> {
        val apps = dao.getAllMonitoredAppsSync().toMutableList()
        // Ensure Instagram is always present as built-in
        if (apps.none { it.packageName == "com.instagram.android" }) {
            val ig = MonitoredAppEntity(
                packageName = "com.instagram.android",
                appName = "Instagram",
                isEnabled = true,
                isBuiltIn = true
            )
            dao.insertMonitoredApp(ig)
            apps.add(0, ig)
        }
        return apps
    }

    suspend fun addMonitoredApp(packageName: String, appName: String): Boolean {
        val cleanPkg = packageName.trim()
        if (cleanPkg.isEmpty()) return false
        val app = MonitoredAppEntity(
            packageName = cleanPkg,
            appName = appName.trim().ifEmpty { cleanPkg },
            isEnabled = true,
            isBuiltIn = cleanPkg == "com.instagram.android" || cleanPkg == "com.instagram.lite",
            addedAt = System.currentTimeMillis()
        )
        dao.insertMonitoredApp(app)
        return true
    }

    suspend fun removeMonitoredApp(packageName: String) {
        dao.deleteMonitoredApp(packageName)
    }

    suspend fun toggleMonitoredApp(packageName: String, isEnabled: Boolean) {
        dao.updateMonitoredAppEnabled(packageName, isEnabled)
    }
}
