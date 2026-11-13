package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationLogEntity
import com.example.model.FilterDecision
import com.example.model.HistoryRetention
import com.example.model.InstagramCategory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
fun HistoryScreen(
    viewModel: NotifierViewModel,
    logs: List<NotificationLogEntity>,
    selectedCategory: InstagramCategory?,
    selectedDecision: FilterDecision?,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    val historyRetention by viewModel.historyRetention.collectAsState()

    val filteredLogs = logs.filter { log ->
        val matchesCat = selectedCategory == null || log.detectedCategory == selectedCategory.name
        val matchesDecision = selectedDecision == null || log.filterDecision == selectedDecision.name
        matchesCat && matchesDecision
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .testTag("history_screen_root")
    ) {
        // Header with Retention & Clear Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Notification History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextPrimary
                )
                Text(
                    text = "${filteredLogs.size} events recorded • Retention: ${historyRetention.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showRetentionDialog = true },
                    modifier = Modifier.testTag("retention_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElegantDarkOutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElegantDarkPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ElegantDarkPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = historyRetention.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElegantDarkPrimary
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = SuppressedRed
                        )
                    }
                }
            }
        }

        // Filter Decision Chips Row
        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = ElegantDarkSurface,
            labelColor = ElegantDarkTextSecondary,
            selectedContainerColor = ElegantDarkPrimary,
            selectedLabelColor = ElegantDarkOnPrimary
        )
        val chipBorder = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = ElegantDarkOutlineVariant
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedDecision == null,
                    onClick = { viewModel.setDecisionFilter(null) },
                    label = { Text("All (${logs.size})") },
                    colors = chipColors,
                    border = chipBorder
                )
            }
            item {
                FilterChip(
                    selected = selectedDecision == FilterDecision.SUPPRESSED,
                    onClick = {
                        viewModel.setDecisionFilter(
                            if (selectedDecision == FilterDecision.SUPPRESSED) null else FilterDecision.SUPPRESSED
                        )
                    },
                    label = {
                        Text("🔕 Suppressed (${logs.count { it.filterDecision == "SUPPRESSED" }})")
                    },
                    colors = chipColors,
                    border = chipBorder
                )
            }
            item {
                FilterChip(
                    selected = selectedDecision == FilterDecision.ALLOWED,
                    onClick = {
                        viewModel.setDecisionFilter(
                            if (selectedDecision == FilterDecision.ALLOWED) null else FilterDecision.ALLOWED
                        )
                    },
                    label = {
                        Text("🔔 Allowed (${logs.count { it.filterDecision == "ALLOWED" }})")
                    },
                    colors = chipColors,
                    border = chipBorder
                )
            }
            item {
                FilterChip(
                    selected = selectedDecision == FilterDecision.KEPT_SAFE_UNKNOWN,
                    onClick = {
                        viewModel.setDecisionFilter(
                            if (selectedDecision == FilterDecision.KEPT_SAFE_UNKNOWN) null else FilterDecision.KEPT_SAFE_UNKNOWN
                        )
                    },
                    label = {
                        Text("🛡️ Kept Safe (${logs.count { it.filterDecision.startsWith("KEPT_SAFE") }})")
                    },
                    colors = chipColors,
                    border = chipBorder
                )
            }
        }

        // Filter Category Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("All Categories") },
                    colors = chipColors,
                    border = chipBorder
                )
            }
            items(InstagramCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        viewModel.setCategoryFilter(
                            if (selectedCategory == category) null else category
                        )
                    },
                    label = { Text("${category.emoji} ${category.title}") },
                    colors = chipColors,
                    border = chipBorder
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notification Log Items List
        if (filteredLogs.isEmpty()) {
            EmptyHistoryState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_logs_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedDecision == FilterDecision.SUPPRESSED) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("suppressed_filter_banner"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                            border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SuppressedRed.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = SuppressedRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Suppressed Instagram Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantDarkTextPrimary
                                    )
                                    Text(
                                        text = "These notifications were automatically intercepted & suppressed according to your preferences.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantDarkTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredLogs, key = { it.id }) { log ->
                    HistoryLogCard(log = log)
                }
            }
        }
    }

    if (showRetentionDialog) {
        RetentionDialog(
            currentRetention = historyRetention,
            onSelectRetention = {
                viewModel.setHistoryRetention(it)
                showRetentionDialog = false
            },
            onDismiss = { showRetentionDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Notification History?") },
            text = { Text("This will remove all suppressed and observed Instagram notification logs from the local database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = SuppressedRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = ElegantDarkTextSecondary)
                }
            },
            containerColor = ElegantDarkSurface,
            titleContentColor = ElegantDarkTextPrimary,
            textContentColor = ElegantDarkTextSecondary
        )
    }
}

@Composable
private fun HistoryLogCard(log: NotificationLogEntity) {
    var expanded by remember { mutableStateOf(false) }

    val category = try {
        InstagramCategory.valueOf(log.detectedCategory)
    } catch (_: Exception) {
        InstagramCategory.UNKNOWN
    }

    val isSuppressed = log.filterDecision == "SUPPRESSED"
    val isAllowed = log.filterDecision == "ALLOWED"
    val isKeptSafe = log.filterDecision.startsWith("KEPT_SAFE")

    val decisionColor = when {
        isSuppressed -> SuppressedRed
        isAllowed -> AllowedGreen
        else -> KeptSafeAmber
    }

    val decisionLabel = when {
        isSuppressed -> "🔕 SUPPRESSED"
        isAllowed -> "🔔 ALLOWED"
        else -> "🛡️ KEPT SAFE"
    }

    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timeString = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("history_item_${log.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.emoji,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantDarkTextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(decisionColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = decisionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = decisionColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Title and Text
            Text(
                text = log.title.ifEmpty { "Instagram Notification" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ElegantDarkTextPrimary
            )

            if (log.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantDarkTextSecondary
                )
            }

            // Expandable details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElegantDarkSurfaceVariant)
                        .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Classification Explanation:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                    Text(
                        text = log.ruleExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )

                    if (log.matchedCues.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Detected Cues: ${log.matchedCues}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = ElegantDarkTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Confidence: ${log.confidence} • Dismissed: ${if (log.dismissedSuccessfully) "Yes" else "No"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantDarkTextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Less info" else "Rule trace",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantDarkPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "No Notifications Captured Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkTextPrimary
            )

            Text(
                text = "When Instagram sends notifications, Notifier will observe them here and record any suppressed items.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun RetentionDialog(
    currentRetention: HistoryRetention,
    onSelectRetention: (HistoryRetention) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "History Retention",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElegantDarkTextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Configure how long suppressed & logged notifications are retained locally. Logs older than this threshold are pruned automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                HistoryRetention.entries.forEach { option ->
                    val isSelected = option == currentRetention
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRetention(option) }
                            .testTag("retention_option_${option.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElegantDarkPrimary.copy(alpha = 0.15f) else ElegantDarkSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ElegantDarkPrimary else ElegantDarkOutlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ElegantDarkPrimary else ElegantDarkTextPrimary
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantDarkTextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = ElegantDarkPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("retention_done_button")
            ) {
                Text("Close", color = ElegantDarkPrimary)
            }
        },
        containerColor = ElegantDarkSurface,
        titleContentColor = ElegantDarkTextPrimary,
        textContentColor = ElegantDarkTextSecondary
    )
}

