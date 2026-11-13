package com.example

import com.example.classifier.InstagramNotificationClassifier
import com.example.model.FilterAction
import com.example.model.FilterDecision
import com.example.model.InstagramCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var classifier: InstagramNotificationClassifier
    private val defaultPreferences = mapOf(
        InstagramCategory.DIRECT_MESSAGES to FilterAction.ALLOW,
        InstagramCategory.STORY_REPLIES to FilterAction.ALLOW,
        InstagramCategory.MENTIONS to FilterAction.ALLOW,
        InstagramCategory.REELS to FilterAction.SUPPRESS,
        InstagramCategory.SHARED_POSTS to FilterAction.SUPPRESS,
        InstagramCategory.LIKES to FilterAction.SUPPRESS,
        InstagramCategory.COMMENTS to FilterAction.SUPPRESS,
        InstagramCategory.FOLLOWS to FilterAction.SUPPRESS,
        InstagramCategory.LIVE to FilterAction.SUPPRESS,
        InstagramCategory.OTHER to FilterAction.SUPPRESS,
        InstagramCategory.UNKNOWN to FilterAction.ALLOW
    )

    @Before
    fun setUp() {
        classifier = InstagramNotificationClassifier()
    }

    @Test
    fun testReelNotificationClassificationAndSuppression() {
        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Arun",
            text = "sent you a reel"
        )
        val result = classifier.classify(metadata, defaultPreferences)

        assertEquals(InstagramCategory.REELS, result.category)
        assertEquals(FilterDecision.SUPPRESSED, result.decision)
        assertTrue(result.shouldDismiss)
    }

    @Test
    fun testDirectMessageNotificationAllowance() {
        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Priya",
            text = "Hey are you coming to dinner?",
            actions = listOf("Reply")
        )
        val result = classifier.classify(metadata, defaultPreferences)

        assertEquals(InstagramCategory.DIRECT_MESSAGES, result.category)
        assertEquals(FilterDecision.ALLOWED, result.decision)
        assertFalse(result.shouldDismiss)
    }

    @Test
    fun testStoryReplyNotificationAllowance() {
        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Sarah",
            text = "replied to your story: Haha so funny!"
        )
        val result = classifier.classify(metadata, defaultPreferences)

        assertEquals(InstagramCategory.STORY_REPLIES, result.category)
        assertEquals(FilterDecision.ALLOWED, result.decision)
        assertFalse(result.shouldDismiss)
    }

    @Test
    fun testLikesSuppression() {
        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "user123 and 5 others liked your post"
        )
        val result = classifier.classify(metadata, defaultPreferences)

        assertEquals(InstagramCategory.LIKES, result.category)
        assertEquals(FilterDecision.SUPPRESSED, result.decision)
        assertTrue(result.shouldDismiss)
    }

    @Test
    fun testSafetyPrincipleForAmbiguousNotification() {
        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Notice",
            text = "We noticed something unusual about your account"
        )
        val result = classifier.classify(metadata, defaultPreferences)

        assertEquals(InstagramCategory.UNKNOWN, result.category)
        assertEquals(FilterDecision.KEPT_SAFE_UNKNOWN, result.decision)
        assertFalse("Safety principle must NEVER dismiss ambiguous notifications", result.shouldDismiss)
    }

    @Test
    fun testSpecialPersonReelAllowedWhenReelsSuppressed() {
        val specialPersons = listOf(
            com.example.data.SpecialPersonEntity(
                id = 1,
                instagramId = "alex_c",
                nickname = "Alex",
                reelsOnly = true
            )
        )

        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Alex",
            text = "sent you a reel"
        )

        val result = classifier.classify(
            metadata = metadata,
            userPreferences = defaultPreferences,
            specialPersons = specialPersons
        )

        assertEquals(InstagramCategory.REELS, result.category)
        assertEquals(FilterDecision.ALLOWED, result.decision)
        assertFalse("Special person reel must be allowed even when general reels are suppressed", result.shouldDismiss)
    }

    @Test
    fun testRegularPersonReelStillSuppressedWhenSpecialPersonsExist() {
        val specialPersons = listOf(
            com.example.data.SpecialPersonEntity(
                id = 1,
                instagramId = "alex_c",
                nickname = "Alex",
                reelsOnly = true
            )
        )

        val metadata = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "RandomUser99",
            text = "sent you a reel"
        )

        val result = classifier.classify(
            metadata = metadata,
            userPreferences = defaultPreferences,
            specialPersons = specialPersons
        )

        assertEquals(InstagramCategory.REELS, result.category)
        assertEquals(FilterDecision.SUPPRESSED, result.decision)
        assertTrue("Non-special person reel must be suppressed", result.shouldDismiss)
    }

    @Test
    fun testPhotosAndVideosClassification() {
        val metadataPhoto = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "Kavya",
            text = "shared a photo with you"
        )
        val resultPhoto = classifier.classify(metadataPhoto, defaultPreferences)
        assertEquals(InstagramCategory.PHOTOS_AND_VIDEOS, resultPhoto.category)

        val metadataVideo = InstagramNotificationClassifier.NotificationMetadata(
            packageName = "com.instagram.android",
            title = "David",
            text = "shared a video with you"
        )
        val resultVideo = classifier.classify(metadataVideo, defaultPreferences)
        assertEquals(InstagramCategory.PHOTOS_AND_VIDEOS, resultVideo.category)
    }
}

