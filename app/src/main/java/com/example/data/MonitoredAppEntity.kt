package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an application monitored by Notifier.
 * Enables multi-app notification interception and custom filtering for WhatsApp, Telegram, Snapchat, etc.
 */
@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
