package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Special Person exception rule.
 *
 * When an Instagram Reel is sent by a user matching either the [instagramId] (handle)
 * or [nickname], Notifier will allow the notification to be displayed even if general Reels
 * are muted.
 */
@Entity(tableName = "special_persons")
data class SpecialPersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val instagramId: String,
    val nickname: String = "",
    val reelsOnly: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
