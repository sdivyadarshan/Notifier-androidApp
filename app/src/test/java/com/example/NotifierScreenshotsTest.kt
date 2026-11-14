package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.NotificationLogEntity
import com.example.data.NotificationRepository
import com.example.ui.NotifierApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NotifierViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class NotifierScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createPopulatedViewModel(): NotifierViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val repo = NotificationRepository(db.notificationDao(), app)

        runBlocking {
            repo.clearHistory()
            repo.addSpecialPerson("alex_v", "Alex", reelsOnly = true)
            repo.addSpecialPerson("priya_s", "Priya", reelsOnly = false)
            repo.addCustomRule(
                appPackage = "com.instagram.android",
                appName = "Instagram",
                matchType = "CONTAINS",
                pattern = "suggested for you",
                targetField = "TEXT",
                action = "SUPPRESS"
            )

            val now = System.currentTimeMillis()
            repo.logNotification(
                NotificationLogEntity(
                    packageName = "com.instagram.android",
                    title = "Priya Sharma",
                    text = "Hey! Are you coming to dinner tonight?",
                    channelId = "ig_direct",
                    notificationCategory = "msg",
                    actionsJson = "Reply, Mark as read",
                    extrasSummary = "android.title: Priya Sharma\nandroid.text: Hey! Are you coming to dinner tonight?",
                    detectedCategory = "DIRECT_MESSAGES",
                    confidence = "HIGH",
                    ruleExplanation = "Identified interactive personal Direct Message via reply affordance or messaging metadata.",
                    matchedCues = "Direct Reply action present",
                    filterDecision = "ALLOWED",
                    timestamp = now - 120_000L,
                    dismissedSuccessfully = false
                )
            )
            repo.logNotification(
                NotificationLogEntity(
                    packageName = "com.instagram.android",
                    title = "Arun",
                    text = "sent you a reel",
                    channelId = "ig_reels",
                    notificationCategory = "social",
                    actionsJson = "",
                    extrasSummary = "android.title: Arun\nandroid.text: sent you a reel\nandroid.channelId: ig_reels",
                    detectedCategory = "REELS",
                    confidence = "HIGH",
                    ruleExplanation = "Identified shared reel activity from text cues or reels channel.",
                    matchedCues = "Matched reel sharing cue",
                    filterDecision = "SUPPRESSED",
                    timestamp = now - 60_000L,
                    dismissedSuccessfully = true
                )
            )
            repo.logNotification(
                NotificationLogEntity(
                    packageName = "com.instagram.android",
                    title = "Instagram Security",
                    text = "New login detected from Chrome on Android",
                    channelId = "ig_security",
                    notificationCategory = "sys",
                    actionsJson = "",
                    extrasSummary = "android.title: Instagram Security\nandroid.text: New login detected from Chrome on Android",
                    detectedCategory = "UNKNOWN",
                    confidence = "LOW",
                    ruleExplanation = "Safe Mode Fallback: Notification category could not be determined with high confidence. Defaulted to ALLOW to prevent accidental alert loss.",
                    matchedCues = "No confident heuristic pattern matched • Safe Mode Fallback: Preserved",
                    filterDecision = "KEPT_SAFE_UNKNOWN",
                    timestamp = now - 30_000L,
                    dismissedSuccessfully = false
                )
            )
        }

        val viewModel = NotifierViewModel(app)
        viewModel.runDryRunClassification("Arun", "sent you a reel")
        return viewModel
    }

    @Test
    fun captureAllScreenScreenshots() {
        val viewModel = createPopulatedViewModel()

        composeTestRule.setContent {
            MyApplicationTheme {
                NotifierApp(viewModel = viewModel)
            }
        }

        val tabs = listOf(
            0 to "../docs/screenshots/screen_controls.png",
            1 to "../docs/screenshots/screen_history.png",
            2 to "../docs/screenshots/screen_inspector.png",
            3 to "../docs/screenshots/screen_simulator.png",
            4 to "../docs/screenshots/screen_about.png"
        )

        for ((tabIndex, path) in tabs) {
            composeTestRule.runOnIdle {
                viewModel.setSelectedTab(tabIndex)
            }
            composeTestRule.waitForIdle()
            composeTestRule.mainClock.advanceTimeBy(400)
            composeTestRule.waitForIdle()
            composeTestRule.onRoot().captureRoboImage(filePath = path)
        }
    }
}
