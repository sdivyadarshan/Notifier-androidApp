package com.example.model

/**
 * Categories of Instagram notification activities.
 */
enum class InstagramCategory(
    val title: String,
    val emoji: String,
    val description: String,
    val defaultAction: FilterAction
) {
    DIRECT_MESSAGES(
        title = "Direct Messages",
        emoji = "💬",
        description = "Personal 1-on-1 and group chat messages",
        defaultAction = FilterAction.ALLOW
    ),
    STORY_REPLIES(
        title = "Story & Message Reactions",
        emoji = "↩️",
        description = "Direct replies and emoji reactions to messages & stories",
        defaultAction = FilterAction.ALLOW
    ),
    MENTIONS(
        title = "Mentions & Tags",
        emoji = "🏷️",
        description = "When someone tags or mentions you in posts or stories",
        defaultAction = FilterAction.ALLOW
    ),
    REELS(
        title = "Reels",
        emoji = "📹",
        description = "Shared Reels, clips, and video recommendations",
        defaultAction = FilterAction.SUPPRESS
    ),
    PHOTOS_AND_VIDEOS(
        title = "Photos & Videos",
        emoji = "📸",
        description = "Feed photo posts, carousel albums, video uploads, and media shares",
        defaultAction = FilterAction.SUPPRESS
    ),
    SHARED_POSTS(
        title = "Shared Posts",
        emoji = "📤",
        description = "Shared feed posts, carousels, and photo links",
        defaultAction = FilterAction.SUPPRESS
    ),
    LIKES(
        title = "Likes & Reactions",
        emoji = "❤️",
        description = "Likes on your photos, reels, stories, or comments",
        defaultAction = FilterAction.SUPPRESS
    ),
    COMMENTS(
        title = "Comments",
        emoji = "💭",
        description = "New comments or replies on your posts",
        defaultAction = FilterAction.SUPPRESS
    ),
    FOLLOWS(
        title = "Follows & Requests",
        emoji = "👤",
        description = "New followers, follow requests, and suggested accounts",
        defaultAction = FilterAction.SUPPRESS
    ),
    LIVE(
        title = "Live Broadcasts",
        emoji = "🔴",
        description = "Alerts when accounts start streaming live video",
        defaultAction = FilterAction.SUPPRESS
    ),
    OTHER(
        title = "Other Activity",
        emoji = "📢",
        description = "Broadcast channels, anniversary reminders, and tips",
        defaultAction = FilterAction.SUPPRESS
    ),
    UNKNOWN(
        title = "Unknown / Ambiguous",
        emoji = "❓",
        description = "Uncertain notification format (Always preserved for safety)",
        defaultAction = FilterAction.ALLOW
    )
}

enum class FilterAction {
    ALLOW,
    SUPPRESS
}

enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class FilterDecision {
    ALLOWED,
    SUPPRESSED,
    KEPT_SAFE_LOW_CONFIDENCE,
    KEPT_SAFE_UNKNOWN
}

data class ClassificationResult(
    val category: InstagramCategory,
    val confidence: ConfidenceLevel,
    val decision: FilterDecision,
    val ruleExplanation: String,
    val matchedCues: List<String>,
    val shouldDismiss: Boolean
)
