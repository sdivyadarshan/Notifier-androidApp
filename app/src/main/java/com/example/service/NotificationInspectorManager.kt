package com.example.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.example.classifier.InstagramNotificationClassifier

object NotificationInspectorManager {

    fun extractMetadata(sbn: StatusBarNotification): InstagramNotificationClassifier.NotificationMetadata {
        val notification = sbn.notification
        val extras = notification.extras ?: Bundle()

        // Extract Title across various potential extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: notification.tickerText?.toString()
            ?: ""

        // Extract primary text and big text
        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
            ?: ""

        var bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        // Extract text lines (e.g. InboxStyle or bundled messages)
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val linesJoined = textLines?.filterNotNull()?.joinToString(" ") ?: ""

        // Extract MessagingStyle messages (e.g., modern Instagram DMs, Reels, and photo shares)
        val extractedMessages = mutableListOf<String>()
        val rawMessages = extras.get(Notification.EXTRA_MESSAGES) ?: extras.get("android.messages")
        if (rawMessages is Array<*>) {
            for (msgItem in rawMessages) {
                if (msgItem is Bundle) {
                    val msgText = msgItem.getCharSequence("text")?.toString()
                    val sender = msgItem.getCharSequence("sender")?.toString()
                    if (!msgText.isNullOrBlank()) {
                        if (!sender.isNullOrBlank()) {
                            extractedMessages.add("$sender: $msgText")
                        } else {
                            extractedMessages.add(msgText)
                        }
                    }
                } else if (msgItem != null) {
                    try {
                        val textMethod = msgItem.javaClass.getMethod("getText")
                        val itemText = textMethod.invoke(msgItem) as? CharSequence
                        val senderMethod = msgItem.javaClass.getMethod("getSender")
                        val itemSender = senderMethod.invoke(msgItem)
                        if (!itemText.isNullOrBlank()) {
                            if (itemSender != null) {
                                extractedMessages.add("$itemSender: $itemText")
                            } else {
                                extractedMessages.add(itemText.toString())
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // If primary text is blank, populate from extracted messages, lines, or tickerText
        if (text.isBlank()) {
            text = when {
                extractedMessages.isNotEmpty() -> extractedMessages.last()
                linesJoined.isNotBlank() -> linesJoined
                notification.tickerText != null -> notification.tickerText.toString()
                else -> ""
            }
        }

        // Combine all conversational cues into bigText if bigText was empty
        if (bigText.isBlank()) {
            bigText = buildString {
                if (linesJoined.isNotBlank()) append(linesJoined).append(" ")
                if (extractedMessages.isNotEmpty()) append(extractedMessages.joinToString(" • "))
            }.trim()
        } else if (extractedMessages.isNotEmpty()) {
            bigText = "$bigText • ${extractedMessages.joinToString(" • ")}"
        }

        val channelId = notification.channelId ?: ""
        val category = notification.category ?: ""

        val actions = mutableListOf<String>()
        notification.actions?.forEach { action ->
            action.title?.let { actions.add(it.toString()) }
        }

        val extrasKeys = extras.keySet().toList()

        return InstagramNotificationClassifier.NotificationMetadata(
            packageName = sbn.packageName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            channelId = channelId,
            notificationCategory = category,
            actions = actions,
            extrasKeys = extrasKeys
        )
    }

    fun formatExtras(extras: Bundle?): String {
        if (extras == null) return "No extras bundle"
        val sb = StringBuilder()
        val keys = extras.keySet().sorted()
        for (key in keys) {
            val value = extras.get(key)
            val displayVal = when (value) {
                is CharSequence -> value.toString()
                is Boolean, is Number -> value.toString()
                is Array<*> -> value.joinToString(", ")
                null -> "null"
                else -> "${value.javaClass.simpleName} ($value)"
            }
            sb.appendLine("$key: $displayVal")
        }
        return sb.toString().trimEnd()
    }
}
