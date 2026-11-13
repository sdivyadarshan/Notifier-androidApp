package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import java.util.concurrent.atomic.AtomicInteger

object TestNotificationSender {

    const val CHANNEL_ID = "instagram_test_channel"
    private val notificationIdCounter = AtomicInteger(1001)

    data class SimulationScenario(
        val label: String,
        val emoji: String,
        val senderTitle: String,
        val messageText: String,
        val channelId: String = CHANNEL_ID,
        val isMessageStyle: Boolean = false,
        val categoryDescription: String
    )

    val PRESET_SCENARIOS = listOf(
        SimulationScenario(
            label = "Reel Shared",
            emoji = "📹",
            senderTitle = "Arun",
            messageText = "sent you a reel",
            categoryDescription = "Reels activity"
        ),
        SimulationScenario(
            label = "Special Person Reel",
            emoji = "⭐",
            senderTitle = "Alex",
            messageText = "sent you a reel",
            categoryDescription = "Reel from Special Person (Allowed via Exception)"
        ),
        SimulationScenario(
            label = "Photos & Videos",
            emoji = "📸",
            senderTitle = "Kavya",
            messageText = "shared a photo with you",
            categoryDescription = "Feed photo / video post (Distinct from Reels & DMs)"
        ),
        SimulationScenario(
            label = "Direct Message",
            emoji = "💬",
            senderTitle = "Priya Sharma",
            messageText = "Hey! Are you coming to dinner tonight?",
            isMessageStyle = true,
            categoryDescription = "Direct 1-on-1 message"
        ),
        SimulationScenario(
            label = "Story Reply",
            emoji = "↩️",
            senderTitle = "Sarah Jenkins",
            messageText = "replied to your story: Haha so true! 😂",
            categoryDescription = "Story reply reaction"
        ),
        SimulationScenario(
            label = "Post Like",
            emoji = "❤️",
            senderTitle = "Instagram",
            messageText = "kavya_m and 14 others liked your post",
            categoryDescription = "Social like interaction"
        ),
        SimulationScenario(
            label = "New Comment",
            emoji = "💭",
            senderTitle = "Alex Chen",
            messageText = "commented: Such a fantastic capture! Which lens?",
            categoryDescription = "Post comment"
        ),
        SimulationScenario(
            label = "Follower Alert",
            emoji = "👤",
            senderTitle = "David Miller",
            messageText = "started following you",
            categoryDescription = "Follower update"
        ),
        SimulationScenario(
            label = "Mention",
            emoji = "🏷️",
            senderTitle = "Elena Rostova",
            messageText = "mentioned you in a comment: @you have you seen this?",
            categoryDescription = "Tag / Mention"
        ),
        SimulationScenario(
            label = "Live Video",
            emoji = "🔴",
            senderTitle = "Marcus Aurel",
            messageText = "is live now. Watch before it ends!",
            categoryDescription = "Live streaming alert"
        ),
        SimulationScenario(
            label = "Shared Post",
            emoji = "📤",
            senderTitle = "Jessica Taylor",
            messageText = "shared a post by @nationalgeographic",
            categoryDescription = "Shared feed post"
        ),
        SimulationScenario(
            label = "Ambiguous Notification",
            emoji = "❓",
            senderTitle = "Instagram Security",
            messageText = "Account alert: unusual sign-in detected near Seattle",
            categoryDescription = "Uncertain structure (Safety Principle preserved)"
        )
    )

    fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_test_name)
            val descriptionText = context.getString(R.string.channel_test_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun dispatchTestNotification(
        context: Context,
        title: String,
        text: String,
        addReplyAction: Boolean = false,
        channelId: String = CHANNEL_ID
    ): Int {
        createChannelIfNeeded(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifId = notificationIdCounter.incrementAndGet()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifier_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (addReplyAction) {
            val remoteInput = RemoteInput.Builder("key_text_reply")
                .setLabel("Reply to $title")
                .build()
            val replyAction = NotificationCompat.Action.Builder(
                R.drawable.ic_notifier_icon,
                "Reply",
                pendingIntent
            ).addRemoteInput(remoteInput).build()

            builder.addAction(replyAction)
            builder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
        }

        try {
            notificationManager.notify(notifId, builder.build())
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }

        return notifId
    }
}
