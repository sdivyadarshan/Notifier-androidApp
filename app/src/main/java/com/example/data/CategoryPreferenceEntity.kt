package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_preferences")
data class CategoryPreferenceEntity(
    @PrimaryKey
    val categoryName: String,
    val action: String, // ALLOW or SUPPRESS
    val updatedAt: Long = System.currentTimeMillis()
)
