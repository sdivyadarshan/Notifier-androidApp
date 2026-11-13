package com.example.model

enum class HistoryRetention(
    val label: String,
    val description: String,
    val durationMillis: Long
) {
    ONE_DAY(
        label = "24 Hours",
        description = "Keep suppressed logs for 24 hours",
        durationMillis = 24L * 60 * 60 * 1000
    ),
    THREE_DAYS(
        label = "3 Days",
        description = "Keep suppressed logs for 3 days",
        durationMillis = 3L * 24 * 60 * 60 * 1000
    ),
    SEVEN_DAYS(
        label = "7 Days",
        description = "Keep suppressed logs for 1 week",
        durationMillis = 7L * 24 * 60 * 60 * 1000
    ),
    THIRTY_DAYS(
        label = "30 Days",
        description = "Keep suppressed logs for 30 days",
        durationMillis = 30L * 24 * 60 * 60 * 1000
    ),
    FOREVER(
        label = "Forever",
        description = "Keep all history indefinitely",
        durationMillis = Long.MAX_VALUE
    )
}
