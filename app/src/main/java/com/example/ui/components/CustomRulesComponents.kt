package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.CustomFilterRuleEntity
import com.example.data.MonitoredAppEntity
import com.example.ui.theme.AllowedGreen
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkOutlineVariant
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import com.example.ui.theme.SuppressedRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomRulesCard(
    customRules: List<CustomFilterRuleEntity>,
    onAddRuleClick: () -> Unit,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onQuickAddRule: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("custom_rules_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Prefix & Keyword Rules",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElegantDarkPrimary.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${customRules.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                        Text(
                            text = "Block notifications by prefix or keywords",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onAddRuleClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_custom_rule_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Explanatory note
            Text(
                text = "Specify custom starting phrases or keywords to block or allow immediately (e.g., if notification starts with 'sent a reel...' or 'Sent you a post').",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                lineHeight = 17.sp
            )

            // Quick Preset Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ONE-TAP PRESET SUGGESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElegantDarkTextSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickSuggestionChip(
                        label = "Starts with \"Sent you a post\"",
                        onClick = { onQuickAddRule("Sent you a post", "STARTS_WITH") }
                    )
                    QuickSuggestionChip(
                        label = "Starts with \"sent a reel\"",
                        onClick = { onQuickAddRule("sent a reel", "STARTS_WITH") }
                    )
                    QuickSuggestionChip(
                        label = "Starts with \"Reacted\"",
                        onClick = { onQuickAddRule("Reacted", "STARTS_WITH") }
                    )
                    QuickSuggestionChip(
                        label = "Starts with \"Sent you a photo\"",
                        onClick = { onQuickAddRule("Sent you a photo", "STARTS_WITH") }
                    )
                }
            }

            // List of Rules or Empty State
            if (customRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElegantDarkSurfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No custom rules active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap a preset above or '+ Add' to create your first rule",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customRules.forEach { rule ->
                        CustomRuleItemRow(
                            rule = rule,
                            onToggle = { onToggleRule(rule.id, it) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ElegantDarkSurfaceVariant)
            .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = ElegantDarkPrimary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = ElegantDarkTextPrimary
            )
        }
    }
}

@Composable
private fun CustomRuleItemRow(
    rule: CustomFilterRuleEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ElegantDarkSurfaceVariant)
            .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // App Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElegantDarkPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = rule.appName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkPrimary
                        )
                    }

                    // Condition Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        val matchLabel = when (rule.matchType) {
                            "STARTS_WITH" -> "STARTS WITH"
                            "CONTAINS" -> "CONTAINS"
                            "ENDS_WITH" -> "ENDS WITH"
                            "EXACT" -> "EXACT MATCH"
                            else -> rule.matchType
                        }
                        Text(
                            text = matchLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElegantDarkTextSecondary
                        )
                    }

                    // Action Badge
                    val isSuppress = rule.action.equals("SUPPRESS", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSuppress) SuppressedRed.copy(alpha = 0.15f) else AllowedGreen.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSuppress) "BLOCK" else "ALLOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuppress) SuppressedRed else AllowedGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"${rule.pattern}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextPrimary
                )

                Text(
                    text = "Field: ${rule.targetField.lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = ElegantDarkTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantDarkOnPrimary,
                        checkedTrackColor = ElegantDarkPrimary,
                        uncheckedThumbColor = ElegantDarkTextSecondary,
                        uncheckedTrackColor = ElegantDarkOutline,
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete rule",
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCustomRuleDialog(
    monitoredApps: List<MonitoredAppEntity>,
    onDismiss: () -> Unit,
    onAddRule: (String, String, String, String, String, String) -> Unit
) {
    var selectedApp by remember {
        mutableStateOf(monitoredApps.firstOrNull() ?: MonitoredAppEntity("com.instagram.android", "Instagram", isBuiltIn = true))
    }
    var matchType by remember { mutableStateOf("STARTS_WITH") }
    var pattern by remember { mutableStateOf("") }
    var targetField by remember { mutableStateOf("TEXT") }
    var action by remember { mutableStateOf("SUPPRESS") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElegantDarkSurface,
        titleContentColor = ElegantDarkTextPrimary,
        textContentColor = ElegantDarkTextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Rule,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Add Prefix / Keyword Rule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Target App Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TARGET APP",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        monitoredApps.forEach { app ->
                            FilterChip(
                                selected = selectedApp.packageName == app.packageName,
                                onClick = { selectedApp = app },
                                label = { Text(app.appName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantDarkPrimary,
                                    selectedLabelColor = ElegantDarkOnPrimary,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantDarkTextPrimary
                                )
                            )
                        }
                    }
                }

                // Match Condition Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MATCH CONDITION",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "STARTS_WITH" to "Starts with",
                            "CONTAINS" to "Contains",
                            "ENDS_WITH" to "Ends with"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = matchType == type,
                                onClick = { matchType = type },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantDarkPrimary,
                                    selectedLabelColor = ElegantDarkOnPrimary,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantDarkTextPrimary
                                )
                            )
                        }
                    }
                }

                // Notification Text Input
                OutlinedTextField(
                    value = pattern,
                    onValueChange = {
                        pattern = it
                        errorMessage = null
                    },
                    label = { Text("Notification text prefix / keyword") },
                    placeholder = { Text("e.g. sent a reel..., Sent you a post") },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = SuppressedRed) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary,
                        cursorColor = ElegantDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Selector (Suppress vs Allow)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ACTION WHEN MATCHED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = action == "SUPPRESS",
                            onClick = { action = "SUPPRESS" },
                            label = { Text("Block / Suppress 🚫", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SuppressedRed.copy(alpha = 0.25f),
                                selectedLabelColor = SuppressedRed,
                                containerColor = ElegantDarkSurfaceVariant,
                                labelColor = ElegantDarkTextPrimary
                            )
                        )
                        FilterChip(
                            selected = action == "ALLOW",
                            onClick = { action = "ALLOW" },
                            label = { Text("Allow Notification ✅", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AllowedGreen.copy(alpha = 0.25f),
                                selectedLabelColor = AllowedGreen,
                                containerColor = ElegantDarkSurfaceVariant,
                                labelColor = ElegantDarkTextPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = pattern.trim()
                    if (trimmed.isEmpty()) {
                        errorMessage = "Please enter text to match"
                        return@Button
                    }
                    onAddRule(
                        selectedApp.packageName,
                        selectedApp.appName,
                        matchType,
                        trimmed,
                        targetField,
                        action
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Rule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ElegantDarkTextSecondary)
            }
        }
    )
}
