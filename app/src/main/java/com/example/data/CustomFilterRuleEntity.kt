package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a custom text-matching filter rule.
 * Allows users to suppress or allow notifications based on prefix ("starts with"),
 * substring ("contains"), suffix ("ends with"), or exact match on notification text or title.
 */
@Entity(tableName = "custom_filter_rules")
data class CustomFilterRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appPackage: String = "com.instagram.android", // package name or "*" for all apps
    val appName: String = "Instagram",
    val matchType: String = "STARTS_WITH", // "STARTS_WITH", "CONTAINS", "ENDS_WITH", "EXACT"
    val pattern: String,
    val targetField: String = "TEXT", // "TEXT", "TITLE", "ANY"
    val action: String = "SUPPRESS", // "SUPPRESS", "ALLOW"
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
