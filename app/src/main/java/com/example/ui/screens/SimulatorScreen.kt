package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClassificationResult
import com.example.model.FilterDecision
import com.example.service.TestNotificationSender
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

@Composable
fun SimulatorScreen(
    viewModel: NotifierViewModel,
    dryRunResult: ClassificationResult?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var customTitle by remember { mutableStateOf("Arun") }
    var customText by remember { mutableStateOf("sent you a reel") }
    var customReplyAction by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .testTag("simulator_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner info
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
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Notification Simulator Lab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                        Text(
                            text = "Test how Notifier classifies and filters incoming notifications in real time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }
            }
        }

        // Preset Scenarios
        item {
            Text(
                text = "PRESET SCENARIOS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = ElegantDarkTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(TestNotificationSender.PRESET_SCENARIOS) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onSelect = {
                            customTitle = scenario.senderTitle
                            customText = scenario.messageText
                            customReplyAction = scenario.isMessageStyle
                            viewModel.runDryRunClassification(
                                title = scenario.senderTitle,
                                text = scenario.messageText,
                                actions = if (scenario.isMessageStyle) listOf("Reply") else emptyList()
                            )
                        },
                        onSendReal = {
                            viewModel.sendTestNotification(context, scenario)
                            Toast.makeText(context, "Dispatched ${scenario.label} notification", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Interactive Notification Sandbox
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantDarkSurface
                ),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Interactive Notification Sandbox",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )
                    Text(
                        text = "Customize notification payload to test edge cases or inspect heuristics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary,
                        focusedContainerColor = ElegantDarkSurfaceVariant,
                        unfocusedContainerColor = ElegantDarkSurfaceVariant,
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextSecondary
                    )

                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = {
                            customTitle = it
                            viewModel.runDryRunClassification(
                                title = it,
                                text = customText,
                                actions = if (customReplyAction) listOf("Reply") else emptyList()
                            )
                        },
                        label = { Text("Sender / Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulator_title_input"),
                        singleLine = true,
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it
                            viewModel.runDryRunClassification(
                                title = customTitle,
                                text = it,
                                actions = if (customReplyAction) listOf("Reply") else emptyList()
                            )
                        },
                        label = { Text("Notification Body / Text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulator_text_input"),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = customReplyAction,
                            onCheckedChange = {
                                customReplyAction = it
                                viewModel.runDryRunClassification(
                                    title = customTitle,
                                    text = customText,
                                    actions = if (it) listOf("Reply") else emptyList()
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ElegantDarkPrimary,
                                checkmarkColor = ElegantDarkOnPrimary,
                                uncheckedColor = ElegantDarkOutline
                            )
                        )
                        Text(
                            text = "Include 'Reply' action (Direct Message style)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElegantDarkTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.runDryRunClassification(
                                    title = customTitle,
                                    text = customText,
                                    actions = if (customReplyAction) listOf("Reply") else emptyList()
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_dry_run_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ElegantDarkOutline)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElegantDarkPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dry Run", color = ElegantDarkPrimary)
                        }

                        Button(
                            onClick = {
                                viewModel.sendCustomTestNotification(
                                    context = context,
                                    title = customTitle,
                                    text = customText,
                                    addReply = customReplyAction
                                )
                                Toast.makeText(context, "Sent to Android Notification System!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_real_notification_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post to Bar")
                        }
                    }
                }
            }
        }

        // Live Classification Result Card
        if (dryRunResult != null) {
            item {
                ClassificationResultDisplayCard(result = dryRunResult)
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: TestNotificationSender.SimulationScenario,
    onSelect: () -> Unit,
    onSendReal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant),
        modifier = Modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Text(text = scenario.emoji, fontSize = 16.sp)
                }
                Text(
                    text = scenario.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${scenario.senderTitle}: ${scenario.messageText}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = ElegantDarkTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElegantDarkOutline),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Inspect", fontSize = 11.sp, color = ElegantDarkPrimary)
                }

                Button(
                    onClick = onSendReal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Fire 🔔", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ClassificationResultDisplayCard(result: ClassificationResult) {
    val isSuppressed = result.decision == FilterDecision.SUPPRESSED
    val isAllowed = result.decision == FilterDecision.ALLOWED
    val isKeptSafe = result.decision.name.startsWith("KEPT_SAFE")

    val decisionColor = when {
        isSuppressed -> SuppressedRed
        isAllowed -> AllowedGreen
        else -> KeptSafeAmber
    }

    val decisionLabel = when {
        isSuppressed -> "🔕 SUPPRESS (Dismiss Notification)"
        isAllowed -> "🔔 ALLOW (Leave Notification Visible)"
        else -> "🛡️ KEPT SAFE (Preserved via Safety Principle)"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(1.dp, ElegantDarkOutlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("classification_result_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "CLASSIFICATION RESULT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = ElegantDarkTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkAvatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = result.category.emoji, fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = result.category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                    Text(
                        text = "Confidence: ${result.confidence}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantDarkTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(decisionColor.copy(alpha = 0.18f))
                    .padding(12.dp)
            ) {
                Text(
                    text = decisionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = decisionColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Reasoning:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkTextPrimary
            )
            Text(
                text = result.ruleExplanation,
                style = MaterialTheme.typography.bodyMedium,
                color = ElegantDarkTextSecondary
            )

            if (result.matchedCues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Matched Cues:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextPrimary
                )
                result.matchedCues.forEach { cue ->
                    Text(
                        text = "• $cue",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )
                }
            }
        }
    }
}
