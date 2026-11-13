package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_logs WHERE filterDecision = :decision ORDER BY timestamp DESC")
    fun getLogsByDecision(decision: String): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_logs WHERE detectedCategory = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentInspectorLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM notification_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneLogsOlderThan(cutoffTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM notification_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE filterDecision = 'SUPPRESSED'")
    fun getSuppressedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE filterDecision = 'ALLOWED'")
    fun getAllowedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE filterDecision LIKE 'KEPT_SAFE%'")
    fun getKeptSafeCount(): Flow<Int>

    // Preferences
    @Query("SELECT * FROM category_preferences")
    fun getAllPreferences(): Flow<List<CategoryPreferenceEntity>>

    @Query("SELECT * FROM category_preferences")
    suspend fun getAllPreferencesSync(): List<CategoryPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: CategoryPreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<CategoryPreferenceEntity>)

    // Special Persons Exceptions
    @Query("SELECT * FROM special_persons ORDER BY createdAt DESC")
    fun getAllSpecialPersons(): Flow<List<SpecialPersonEntity>>

    @Query("SELECT * FROM special_persons ORDER BY createdAt DESC")
    suspend fun getAllSpecialPersonsSync(): List<SpecialPersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialPerson(person: SpecialPersonEntity): Long

    @Query("DELETE FROM special_persons WHERE id = :id")
    suspend fun deleteSpecialPersonById(id: Long)

    @Query("DELETE FROM special_persons")
    suspend fun clearSpecialPersons()

    @Query("SELECT COUNT(*) FROM special_persons")
    fun getSpecialPersonsCount(): Flow<Int>

    // Custom Filter Rules
    @Query("SELECT * FROM custom_filter_rules ORDER BY createdAt DESC")
    fun getAllCustomRules(): Flow<List<CustomFilterRuleEntity>>

    @Query("SELECT * FROM custom_filter_rules ORDER BY createdAt DESC")
    suspend fun getAllCustomRulesSync(): List<CustomFilterRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRule(rule: CustomFilterRuleEntity): Long

    @Query("DELETE FROM custom_filter_rules WHERE id = :id")
    suspend fun deleteCustomRule(id: Long)

    @Query("UPDATE custom_filter_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateCustomRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM custom_filter_rules")
    suspend fun clearAllCustomRules()

    // Monitored Apps
    @Query("SELECT * FROM monitored_apps ORDER BY isBuiltIn DESC, addedAt ASC")
    fun getAllMonitoredApps(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps ORDER BY isBuiltIn DESC, addedAt ASC")
    suspend fun getAllMonitoredAppsSync(): List<MonitoredAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitoredApp(app: MonitoredAppEntity)

    @Query("DELETE FROM monitored_apps WHERE packageName = :packageName AND isBuiltIn = 0")
    suspend fun deleteMonitoredApp(packageName: String)

    @Query("UPDATE monitored_apps SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun updateMonitoredAppEnabled(packageName: String, isEnabled: Boolean)
}
