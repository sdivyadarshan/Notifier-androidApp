package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationLogEntity
import com.example.model.InstagramCategory
import com.example.ui.theme.AllowedGreen
import com.example.ui.theme.ElegantDarkAvatarBg
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkOutlineVariant
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import com.example.ui.theme.KeptSafeAmber
import com.example.ui.theme.SuppressedRed
import com.example.viewmodel.NotifierViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InspectorScreen(
    viewModel: NotifierViewModel,
    recentLogs: List<NotificationLogEntity>,
    latestLiveNotification: NotificationLogEntity?,
    modifier: Modifier = Modifier
) {
    var selectedLogId by remember { mutableStateOf<Long?>(null) }

    val activeLog = remember(selectedLogId, recentLogs, latestLiveNotification) {
        if (selectedLogId != null) {
            recentLogs.find { it.id == selectedLogId }
        } else {
            latestLiveNotification ?: recentLogs.firstOrNull()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .testTag("inspector_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Diagnostic Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantDarkSurfaceVariant
                ),
                border = BorderStroke(1.dp, ElegantDarkOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Notification Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                        Text(
                            text = "Inspect raw intent payloads, extras bundles, and classifier decision trees.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }
            }
        }

        // Selector row for recent notifications
        if (recentLogs.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "RECENT EVENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = ElegantDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentLogs) { log ->
                            val isSelected = activeLog?.id == log.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLogId = log.id },
                                label = {
                                    Text(
                                        text = "${log.title.ifEmpty { "Notice" }}: ${log.text.take(15)}..."
                                    )
                                },
                                leadingIcon = {
                                    val cat = try {
                                        InstagramCategory.valueOf(log.detectedCategory)
                                    } catch (_: Exception) {
                                        InstagramCategory.UNKNOWN
                                    }
                                    Text(text = cat.emoji)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = ElegantDarkSurface,
                                    labelColor = ElegantDarkTextSecondary,
                                    selectedContainerColor = ElegantDarkPrimary,
                                    selectedLabelColor = ElegantDarkOnPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = ElegantDarkOutlineVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        if (activeLog != null) {
            item {
                InspectionDetailCard(log = activeLog)
            }
        } else {
            item {
                EmptyInspectorCard()
            }
        }

        // Educational Guide: How Android Notification Metadata drives classification
        item {
            ClassificationMetadataGuideCard()
        }
    }
}

@Composable
private fun InspectionDetailCard(log: NotificationLogEntity) {
    val context = LocalContext.current
    val category = try {
        InstagramCategory.valueOf(log.detectedCategory)
    } catch (_: Exception) {
        InstagramCategory.UNKNOWN
    }

    val timeFormat = remember { SimpleDateFormat("h:mm:ss a (MMM d)", Locale.getDefault()) }
    val timeString = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspection_detail_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Title & Classification Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = category.emoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkPrimary
                        )
                        Text(
                            text = "Decision: ${log.filterDecision}",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                log.filterDecision == "SUPPRESSED" -> SuppressedRed
                                log.filterDecision == "ALLOWED" -> AllowedGreen
                                else -> KeptSafeAmber
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val payload = """
                                Package: ${log.packageName}
                                Title: ${log.title}
                                Text: ${log.text}
                                SubText: ${log.subText}
                                Channel ID: ${log.channelId}
                                Category: ${log.notificationCategory}
                                Actions: ${log.actionsJson}
                                Rule: ${log.ruleExplanation}
                                Decision: ${log.filterDecision}
                                Confidence: ${log.confidence}
                                Extras: ${log.extrasSummary}
                            """.trimIndent()
                            clipboard.setPrimaryClip(ClipData.newPlainText("Notifier Metadata", payload))
                            Toast.makeText(context, "Metadata copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_metadata_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantDarkOutlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Metadata",
                            modifier = Modifier.size(14.dp),
                            tint = ElegantDarkPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 11.sp, color = ElegantDarkPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElegantDarkPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Confidence: ${log.confidence}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ElegantDarkOutlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Grid
            InspectorField(label = "Package", value = log.packageName)
            InspectorField(label = "Title", value = log.title.ifEmpty { "null / empty" })
            InspectorField(label = "Text", value = log.text.ifEmpty { "null / empty" })
            if (log.subText.isNotEmpty()) {
                InspectorField(label = "SubText", value = log.subText)
            }
            InspectorField(label = "Channel ID", value = log.channelId.ifEmpty { "Default Channel" })
            InspectorField(label = "Category", value = log.notificationCategory.ifEmpty { "None specified" })
            InspectorField(label = "Actions", value = log.actionsJson.ifEmpty { "No actions attached" })
            InspectorField(label = "Timestamp", value = timeString)
            InspectorField(label = "Heuristic Rule", value = log.ruleExplanation)
            if (log.matchedCues.isNotEmpty()) {
                InspectorField(label = "Matched Cues", value = log.matchedCues)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ElegantDarkOutlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Raw Extras Key-Value Dump
            Text(
                text = "Android Notification Extras Dump",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElegantDarkSurfaceVariant)
                    .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = log.extrasSummary.ifEmpty { "No extra bundle data available." },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElegantDarkPrimary
                )
            }
        }
    }
}

@Composable
private fun InspectorField(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = ElegantDarkTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ElegantDarkTextPrimary
        )
    }
}

@Composable
private fun EmptyInspectorCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkAvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DataObject,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No Inspector Data Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkTextPrimary
            )

            Text(
                text = "Send a test notification in the Simulator or wait for Instagram notifications to arrive to inspect their internal structure.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClassificationMetadataGuideCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("metadata_guide_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Metadata Classification Analysis",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                        Text(
                            text = "How Android metadata reliably isolates Instagram DMs from clutter",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ElegantDarkTextSecondary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = ElegantDarkOutlineVariant)

                    GuideItem(
                        tag = "Package Name",
                        detail = "Identifies notifications originating strictly from com.instagram.android or the local simulation suite."
                    )
                    GuideItem(
                        tag = "Notification Actions",
                        detail = "Direct messages attach an actionable 'Reply' RemoteInput intent, distinguishing human conversation from broadcasted content."
                    )
                    GuideItem(
                        tag = "Android Category & Channel",
                        detail = "Android assigns category 'msg' or dedicated direct message channels to conversational chats, separate from social feeds."
                    )
                    GuideItem(
                        tag = "Bundle Extras",
                        detail = "Contains structured keys (android.title, android.text, android.conversationTitle, android.template) used to inspect styling templates like MessagingStyle."
                    )
                    GuideItem(
                        tag = "Heuristic Cue Matching",
                        detail = "Extracts signature phrases ('sent you a reel', 'liked your post', 'commented', 'started following you', 'live now') with high confidence."
                    )
                    GuideItem(
                        tag = "Safe Mode Fallback",
                        detail = "When structural cues or confidence are ambiguous, Safe Mode defaults to ALLOWED to prevent suppressing vital communications."
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideItem(tag: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "• $tag",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = ElegantDarkPrimary
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = ElegantDarkTextSecondary,
            lineHeight = 16.sp
        )
    }
}

