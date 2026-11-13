package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String = "",
    val channelId: String = "",
    val notificationCategory: String = "",
    val actionsJson: String = "",
    val extrasSummary: String = "",
    val detectedCategory: String,
    val confidence: String,
    val ruleExplanation: String,
    val matchedCues: String = "",
    val filterDecision: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dismissedSuccessfully: Boolean = false
)
