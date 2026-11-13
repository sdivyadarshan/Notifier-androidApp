package com.example.classifier

import com.example.data.CustomFilterRuleEntity
import com.example.data.SpecialPersonEntity
import com.example.model.ClassificationResult
import com.example.model.ConfidenceLevel
import com.example.model.FilterAction
import com.example.model.FilterDecision
import com.example.model.InstagramCategory

/**
 * Heuristic classifier for Instagram notifications.
 *
 * Instagram presents different activities with varying notification structures.
 * This classifier inspects text, channel, category, actions, and extras,
 * enforcing the critical Safety Principle and supporting Special Persons exceptions.
 */
class InstagramNotificationClassifier {

    data class NotificationMetadata(
        val packageName: String,
        val title: String,
        val text: String,
        val subText: String = "",
        val bigText: String = "",
        val channelId: String = "",
        val notificationCategory: String = "",
        val actions: List<String> = emptyList(),
        val extrasKeys: List<String> = emptyList()
    )

    fun classify(
        metadata: NotificationMetadata,
        userPreferences: Map<InstagramCategory, FilterAction>,
        safeModeEnabled: Boolean = true,
        specialPersons: List<SpecialPersonEntity> = emptyList(),
        customRules: List<CustomFilterRuleEntity> = emptyList()
    ): ClassificationResult {
        val titleLower = metadata.title.trim().lowercase()
        val textLower = metadata.text.trim().lowercase()
        
        // currentEventText excludes bigText to avoid false positives on historical conversation lines
        // e.g. if someone sent a reel previously, but the new message is just "Hi".
        val currentEventText = "${metadata.title} ${metadata.text} ${metadata.subText}".lowercase()
        
        // combinedText includes bigText for broader context checking when needed
        val combinedText = "$currentEventText ${metadata.bigText}".lowercase()
        
        val channelLower = metadata.channelId.lowercase()
        val actionsLower = metadata.actions.map { it.lowercase() }
        val hasReplyAction = actionsLower.any { it.contains("reply") || it.contains("respond") }
        val hasMarkReadAction = actionsLower.any { it.contains("mark as read") || it.contains("read") }

        val matchedCues = mutableListOf<String>()
        val matchedSpecialPerson = findMatchingSpecialPerson(metadata, specialPersons)

        // 0. CUSTOM USER FILTER RULES ("Starts with...", "Contains...", "Ends with...", "Exact")
        // Evaluated first with highest priority!
        for (rule in customRules) {
            if (!rule.isEnabled) continue
            val matchesApp = rule.appPackage == "*" ||
                    rule.appPackage.equals(metadata.packageName, ignoreCase = true) ||
                    (rule.appPackage.contains("instagram", ignoreCase = true) &&
                     (metadata.packageName.contains("instagram", ignoreCase = true) || metadata.packageName.contains("example")))
            if (!matchesApp) continue

            val pattern = rule.pattern.trim().lowercase()
            if (pattern.isEmpty()) continue

            val targetText = when (rule.targetField.uppercase()) {
                "TITLE" -> titleLower
                "TEXT" -> textLower
                else -> currentEventText
            }

            val matches = when (rule.matchType.uppercase()) {
                "STARTS_WITH" -> targetText.startsWith(pattern) || textLower.startsWith(pattern)
                "CONTAINS" -> targetText.contains(pattern) || combinedText.contains(pattern)
                "ENDS_WITH" -> targetText.endsWith(pattern) || textLower.endsWith(pattern)
                "EXACT" -> targetText == pattern || textLower == pattern
                else -> targetText.contains(pattern)
            }

            if (matches) {
                val isSuppress = rule.action.equals("SUPPRESS", ignoreCase = true)
                val decision = if (isSuppress) FilterDecision.SUPPRESSED else FilterDecision.ALLOWED
                val actionLabel = if (isSuppress) "Suppressed" else "Allowed"
                matchedCues.add("Custom Rule: [${rule.matchType}] '${rule.pattern}'")
                return ClassificationResult(
                    category = InstagramCategory.OTHER,
                    confidence = ConfidenceLevel.HIGH,
                    decision = decision,
                    ruleExplanation = "Custom Rule Matched: [${rule.matchType}] '${rule.pattern}' on ${rule.appName} -> $actionLabel",
                    matchedCues = matchedCues,
                    shouldDismiss = isSuppress
                )
            }
        }

        // 1. STORY & MESSAGE REACTIONS
        // Handles emoji reactions to stories as well as direct message emoji reactions ("Reacted 😂 to your message")
        val isEmojiReaction = currentEventText.contains("replied to your story") ||
            currentEventText.contains("reacted to your story") ||
            currentEventText.contains("responded to your story") ||
            currentEventText.contains("sent a reaction to your story") ||
            currentEventText.contains("reacted to your message") ||
            currentEventText.contains("reacted to a message") ||
            currentEventText.contains("reacted to your") ||
            currentEventText.contains("sent a reaction") ||
            textLower.startsWith("reacted ") ||
            textLower.startsWith("reacted:") ||
            textLower == "reacted" ||
            currentEventText.contains("reacted with") ||
            Regex("reacted.*to your message").containsMatchIn(currentEventText) ||
            Regex("reacted.*to your story").containsMatchIn(currentEventText)

        if (isEmojiReaction) {
            matchedCues.add("Matched emoji reaction / story reply phrase")
            // If Story Replies OR Likes/Reactions are muted, respect the user's wish to suppress reactions
            val storyRepliesAction = userPreferences[InstagramCategory.STORY_REPLIES] ?: FilterAction.ALLOW
            val likesAction = userPreferences[InstagramCategory.LIKES] ?: FilterAction.SUPPRESS

            val targetCategory = if (storyRepliesAction == FilterAction.SUPPRESS) {
                InstagramCategory.STORY_REPLIES
            } else if (likesAction == FilterAction.SUPPRESS && !currentEventText.contains("story")) {
                InstagramCategory.LIKES
            } else {
                InstagramCategory.STORY_REPLIES
            }

            return evaluateDecision(
                category = targetCategory,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected story reply or message emoji reaction ($textLower).",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 2. REELS (Special Persons Exception Checked Here)
        val isReelActivity = currentEventText.contains("sent you a reel") ||
            currentEventText.contains("shared a reel") ||
            currentEventText.contains("sent a reel") ||
            currentEventText.contains("watch reel") ||
            currentEventText.contains("reels from") ||
            (channelLower.contains("reel") && !currentEventText.contains("liked"))

        if (isReelActivity) {
            matchedCues.add("Matched reel sharing cue")

            // SPECIAL PERSONS REEL EXCEPTION:
            // If the sender is in the Special Persons list, ALWAYS allow notification even if Reels are muted!
            if (matchedSpecialPerson != null) {
                val displayName = if (matchedSpecialPerson.nickname.isNotBlank()) {
                    "${matchedSpecialPerson.nickname} (@${matchedSpecialPerson.instagramId})"
                } else {
                    "@${matchedSpecialPerson.instagramId}"
                }
                matchedCues.add("Special Person matched: $displayName")
                return ClassificationResult(
                    category = InstagramCategory.REELS,
                    confidence = ConfidenceLevel.HIGH,
                    decision = FilterDecision.ALLOWED,
                    ruleExplanation = "Allowed via Special Persons exception: Received Reel from $displayName.",
                    matchedCues = matchedCues,
                    shouldDismiss = false
                )
            }

            return evaluateDecision(
                category = InstagramCategory.REELS,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Identified shared reel activity from text cues or reels channel.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 3. PHOTOS & VIDEOS (Feed posts, carousels, photo/video uploads)
        // User directive: "add for only photos and videos becoz this diff from reels and dm"
        val isPhotoOrVideoContent = currentEventText.contains("shared a photo") ||
            currentEventText.contains("sent a photo") ||
            currentEventText.contains("posted a photo") ||
            currentEventText.contains("photo post") ||
            currentEventText.contains("added a photo") ||
            currentEventText.contains("added 2 photos") ||
            currentEventText.contains("shared a video") ||
            currentEventText.contains("sent a video") ||
            currentEventText.contains("posted a video") ||
            currentEventText.contains("video post") ||
            currentEventText.contains("new photo") ||
            currentEventText.contains("new video") ||
            currentEventText.contains("carousel")

        if (isPhotoOrVideoContent) {
            matchedCues.add("Matched photo/video post pattern")
            return evaluateDecision(
                category = InstagramCategory.PHOTOS_AND_VIDEOS,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected photo post or video upload (distinct from short Reels & DMs).",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 4. LIKES & REACTIONS
        if (currentEventText.contains("liked your") ||
            currentEventText.contains("liked a comment") ||
            currentEventText.contains("and others liked") ||
            channelLower.contains("like")
        ) {
            matchedCues.add("Matched like/reaction token")
            return evaluateDecision(
                category = InstagramCategory.LIKES,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected like or reaction on post, reel, story, or comment.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 5. MENTIONS & TAGS
        if (currentEventText.contains("mentioned you") ||
            currentEventText.contains("tagged you in") ||
            currentEventText.contains("@mentioned")
        ) {
            matchedCues.add("Matched mention/tag phrasing")
            return evaluateDecision(
                category = InstagramCategory.MENTIONS,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected user mention or tag in comment, story, or caption.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 6. COMMENTS
        if (currentEventText.contains("commented on") ||
            currentEventText.contains("commented:") ||
            currentEventText.contains("replied to your comment") ||
            currentEventText.contains("left a comment") ||
            channelLower.contains("comment")
        ) {
            matchedCues.add("Matched comment pattern")
            return evaluateDecision(
                category = InstagramCategory.COMMENTS,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Identified new comment or comment reply.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 7. SHARED POSTS (Direct message post shares, attachments, media)
        val isSharedPost = currentEventText.contains("shared a post") ||
            currentEventText.contains("sent a post") ||
            currentEventText.contains("sent you a post") ||
            currentEventText.contains("shared you a post") ||
            currentEventText.contains("sent you a photo") ||
            currentEventText.contains("sent you a video") ||
            currentEventText.contains("shared a photo") ||
            currentEventText.contains("shared a video") ||
            currentEventText.contains("sent an attachment") ||
            currentEventText.contains("sent you an attachment") ||
            currentEventText.contains("shared an attachment") ||
            textLower.startsWith("sent you a post") ||
            textLower.startsWith("sent a post") ||
            textLower.startsWith("shared a post") ||
            textLower.startsWith("sent you a photo") ||
            textLower.startsWith("sent you a video")

        if (isSharedPost) {
            matchedCues.add("Matched shared post/media pattern ($textLower)")
            val sharedAction = userPreferences[InstagramCategory.SHARED_POSTS] ?: FilterAction.SUPPRESS
            val photosAction = userPreferences[InstagramCategory.PHOTOS_AND_VIDEOS] ?: FilterAction.SUPPRESS
            val targetCategory = if (sharedAction == FilterAction.SUPPRESS) {
                InstagramCategory.SHARED_POSTS
            } else if (photosAction == FilterAction.SUPPRESS) {
                InstagramCategory.PHOTOS_AND_VIDEOS
            } else {
                InstagramCategory.SHARED_POSTS
            }

            return evaluateDecision(
                category = targetCategory,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected shared post, reel, or media attachment in direct chat.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 8. FOLLOWS
        if (currentEventText.contains("started following you") ||
            currentEventText.contains("requested to follow you") ||
            currentEventText.contains("accepted your follow request") ||
            channelLower.contains("follow")
        ) {
            matchedCues.add("Matched follow/request phrasing")
            return evaluateDecision(
                category = InstagramCategory.FOLLOWS,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Identified new follower or follow request event.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 9. LIVE BROADCASTS
        if (currentEventText.contains("is live now") ||
            currentEventText.contains("started a live video") ||
            currentEventText.contains("went live") ||
            currentEventText.contains("live broadcast") ||
            channelLower.contains("live")
        ) {
            matchedCues.add("Matched live broadcast signal")
            return evaluateDecision(
                category = InstagramCategory.LIVE,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected live video streaming alert.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 10. DIRECT MESSAGES
        val hasDirectChannel = channelLower.contains("direct") || channelLower.contains("messages")
        val isMessageCategory = metadata.notificationCategory.equals("msg", ignoreCase = true) ||
                metadata.notificationCategory.contains("message", ignoreCase = true)
        val hasMessagingExtras = metadata.extrasKeys.any {
            it.contains("messagingStyle", ignoreCase = true) ||
            it.contains("conversationTitle", ignoreCase = true) ||
            it.contains("messages", ignoreCase = true)
        }

        if (hasReplyAction || hasDirectChannel || isMessageCategory || hasMessagingExtras) {
            matchedCues.add(
                when {
                    hasReplyAction -> "Direct Reply action present"
                    hasDirectChannel -> "Direct message channel ID"
                    isMessageCategory -> "Notification Category is MSG"
                    else -> "MessagingStyle extras detected"
                }
            )
            return evaluateDecision(
                category = InstagramCategory.DIRECT_MESSAGES,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Identified interactive personal Direct Message via reply affordance or messaging metadata.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // Check for explicit messaging phrasing
        if (combinedText.contains("sent you a message") ||
            combinedText.contains("sent a message") ||
            combinedText.contains("messaged you") ||
            combinedText.contains("sent a voice message")
        ) {
            matchedCues.add("Matched direct message phrasing")
            return evaluateDecision(
                category = InstagramCategory.DIRECT_MESSAGES,
                confidence = ConfidenceLevel.HIGH,
                ruleExplanation = "Detected direct message phrasing in notification.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // Secondary conversational check
        val isSystemAlertOrNotice = combinedText.contains("notice") ||
                combinedText.contains("alert") ||
                combinedText.contains("security") ||
                combinedText.contains("account") ||
                combinedText.contains("log in") ||
                combinedText.contains("sign-in") ||
                combinedText.contains("login") ||
                combinedText.contains("password") ||
                metadata.title.contains("Instagram", ignoreCase = true)

        if (!isSystemAlertOrNotice && metadata.title.isNotEmpty() && metadata.text.isNotEmpty() &&
            metadata.title.split(" ").size <= 3 &&
            !containsGenericSocialKeywords(combinedText) &&
            (channelLower.contains("direct") || channelLower.contains("chat") || metadata.actions.isNotEmpty())
        ) {
            matchedCues.add("Sender name + conversational text format")
            return evaluateDecision(
                category = InstagramCategory.DIRECT_MESSAGES,
                confidence = ConfidenceLevel.MEDIUM,
                ruleExplanation = "Conversational structure detected (Personal Direct Message format).",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 11. OTHER INSTAGRAM ACTIVITY (Announcements, broadcast channels, reminders)
        if (combinedText.contains("broadcast channel") ||
            combinedText.contains("channel update") ||
            combinedText.contains("reminded you") ||
            combinedText.contains("memory") ||
            combinedText.contains("suggestions for you")
        ) {
            matchedCues.add("Matched general announcement / channel keyword")
            return evaluateDecision(
                category = InstagramCategory.OTHER,
                confidence = ConfidenceLevel.MEDIUM,
                ruleExplanation = "Detected broadcast channel or general platform announcement.",
                matchedCues = matchedCues,
                userPreferences = userPreferences,
                safeModeEnabled = safeModeEnabled,
                matchedSpecialPerson = matchedSpecialPerson
            )
        }

        // 12. UNKNOWN / AMBIGUOUS -> CRITICAL SAFETY FALLBACK
        matchedCues.add("No confident heuristic pattern matched")
        val explanation = if (safeModeEnabled) {
            "Safe Mode Fallback: Notification category could not be determined with high confidence. Defaulted to ALLOW to prevent accidental alert loss."
        } else {
            "Ambiguous notification structure. Preserved notification safe under Notifier Safety Principle."
        }
        return ClassificationResult(
            category = InstagramCategory.UNKNOWN,
            confidence = ConfidenceLevel.LOW,
            decision = FilterDecision.KEPT_SAFE_UNKNOWN,
            ruleExplanation = explanation,
            matchedCues = if (safeModeEnabled) matchedCues + listOf("Safe Mode Fallback: Preserved") else matchedCues,
            shouldDismiss = false
        )
    }

    private fun evaluateDecision(
        category: InstagramCategory,
        confidence: ConfidenceLevel,
        ruleExplanation: String,
        matchedCues: List<String>,
        userPreferences: Map<InstagramCategory, FilterAction>,
        safeModeEnabled: Boolean,
        matchedSpecialPerson: SpecialPersonEntity? = null
    ): ClassificationResult {
        // If special person has reelsOnly = false, allow all their notifications
        if (matchedSpecialPerson != null && !matchedSpecialPerson.reelsOnly) {
            val displayName = if (matchedSpecialPerson.nickname.isNotBlank()) {
                "${matchedSpecialPerson.nickname} (@${matchedSpecialPerson.instagramId})"
            } else {
                "@${matchedSpecialPerson.instagramId}"
            }
            return ClassificationResult(
                category = category,
                confidence = ConfidenceLevel.HIGH,
                decision = FilterDecision.ALLOWED,
                ruleExplanation = "Allowed via Special Person exception (All activity from $displayName is allowed).",
                matchedCues = matchedCues + listOf("Special Person: $displayName"),
                shouldDismiss = false
            )
        }

        val preference = userPreferences[category]
            ?: if (category == InstagramCategory.PHOTOS_AND_VIDEOS) {
                userPreferences[InstagramCategory.SHARED_POSTS] ?: category.defaultAction
            } else if (category == InstagramCategory.SHARED_POSTS) {
                userPreferences[InstagramCategory.PHOTOS_AND_VIDEOS] ?: category.defaultAction
            } else {
                category.defaultAction
            }

        // SAFE MODE FALLBACK: If safe mode is active, any low confidence notification
        // or unclassified alert is strictly defaulted to ALLOW to protect the user.
        if (safeModeEnabled && (confidence == ConfidenceLevel.LOW || category == InstagramCategory.UNKNOWN)) {
            return ClassificationResult(
                category = category,
                confidence = confidence,
                decision = if (category == InstagramCategory.UNKNOWN) FilterDecision.KEPT_SAFE_UNKNOWN else FilterDecision.KEPT_SAFE_LOW_CONFIDENCE,
                ruleExplanation = "Safe Mode Fallback: $ruleExplanation (Defaulted to ALLOW to prevent accidental loss of important alerts)",
                matchedCues = matchedCues + listOf("Safe Mode Protection Active"),
                shouldDismiss = false
            )
        }

        // If confidence is LOW even when safe mode is off, keep safe
        if (confidence == ConfidenceLevel.LOW) {
            return ClassificationResult(
                category = category,
                confidence = confidence,
                decision = FilterDecision.KEPT_SAFE_LOW_CONFIDENCE,
                ruleExplanation = "$ruleExplanation (Kept safe due to low classification confidence)",
                matchedCues = matchedCues,
                shouldDismiss = false
            )
        }

        return if (preference == FilterAction.SUPPRESS) {
            ClassificationResult(
                category = category,
                confidence = confidence,
                decision = FilterDecision.SUPPRESSED,
                ruleExplanation = ruleExplanation,
                matchedCues = matchedCues,
                shouldDismiss = true
            )
        } else {
            ClassificationResult(
                category = category,
                confidence = confidence,
                decision = FilterDecision.ALLOWED,
                ruleExplanation = ruleExplanation,
                matchedCues = matchedCues,
                shouldDismiss = false
            )
        }
    }

    private fun findMatchingSpecialPerson(
        metadata: NotificationMetadata,
        specialPersons: List<SpecialPersonEntity>
    ): SpecialPersonEntity? {
        if (specialPersons.isEmpty()) return null
        val titleClean = metadata.title.trim().lowercase().removePrefix("@")
        val textClean = metadata.text.trim().lowercase()
        val combined = "${metadata.title} ${metadata.text} ${metadata.subText}".lowercase()

        for (person in specialPersons) {
            val handle = person.instagramId.trim().lowercase().removePrefix("@")
            val nick = person.nickname.trim().lowercase()

            val handleMatches = handle.isNotEmpty() && (
                titleClean == handle ||
                titleClean.startsWith("$handle ") ||
                titleClean.contains("($handle)") ||
                titleClean.contains("@$handle") ||
                titleClean.contains(handle) ||
                combined.contains("@$handle") ||
                combined.contains("$handle sent") ||
                combined.contains("$handle shared") ||
                textClean.startsWith("$handle ") ||
                textClean.contains("by $handle")
            )

            val nickMatches = nick.isNotEmpty() && (
                titleClean == nick ||
                titleClean.startsWith("$nick ") ||
                titleClean.contains(nick) ||
                combined.contains(nick) ||
                combined.contains("$nick sent") ||
                combined.contains("$nick shared") ||
                textClean.startsWith("$nick ")
            )

            if (handleMatches || nickMatches) {
                return person
            }
        }
        return null
    }

    private fun containsGenericSocialKeywords(text: String): Boolean {
        val keywords = listOf(
            "post", "reel", "story", "like", "comment", "follower", "live", "shared", "photo", "video"
        )
        return keywords.any { text.contains(it) }
    }
}

