package com.example.ui.components

import android.content.Context
import android.content.pm.ApplicationInfo
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun MonitoredAppsCard(
    monitoredApps: List<MonitoredAppEntity>,
    onAddAppClick: () -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onDeleteApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("monitored_apps_card"),
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
                            imageVector = Icons.Default.Apps,
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
                                text = "Monitored Apps",
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
                                    text = "${monitoredApps.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                        Text(
                            text = "Filter messaging & notification apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onAddAppClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_monitored_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add App",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Explanatory note
            Text(
                text = "Add other messaging apps (WhatsApp, Telegram, Snapchat, etc.) to intercept notifications and suppress them with custom keyword & prefix rules.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                lineHeight = 17.sp
            )

            // List of Monitored Apps
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                monitoredApps.forEach { app ->
                    MonitoredAppItemRow(
                        app = app,
                        onToggle = { onToggleApp(app.packageName, it) },
                        onDelete = { onDeleteApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonitoredAppItemRow(
    app: MonitoredAppEntity,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = app.appName.take(1).uppercase()
                    Text(
                        text = initial.ifBlank { "A" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )

                        if (app.isBuiltIn) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElegantDarkPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PRIMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = ElegantDarkTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = app.isEnabled,
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

                if (!app.isBuiltIn) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove app",
                            tint = ElegantDarkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

data class PopularAppPreset(
    val appName: String,
    val packageName: String,
    val emoji: String
)

private val POPULAR_MESSAGING_APPS = listOf(
    PopularAppPreset("WhatsApp", "com.whatsapp", "💬"),
    PopularAppPreset("Telegram", "org.telegram.messenger", "✈️"),
    PopularAppPreset("Snapchat", "com.snapchat.android", "👻"),
    PopularAppPreset("Discord", "com.discord", "🎮"),
    PopularAppPreset("Messenger", "com.facebook.orca", "⚡"),
    PopularAppPreset("Messages", "com.google.android.apps.messaging", "✉️"),
    PopularAppPreset("X / Twitter", "com.twitter.android", "🐦"),
    PopularAppPreset("Reddit", "com.reddit.frontpage", "🤖")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMonitoredAppDialog(
    existingApps: List<MonitoredAppEntity>,
    onDismiss: () -> Unit,
    onAddApp: (String, String) -> Unit
) {
    val context = LocalContext.current
    var customAppName by remember { mutableStateOf("") }
    var customPackageName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val installedApps = remember {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(0)
            packages
                .filter { appInfo ->
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    appInfo.packageName.contains("message") ||
                    appInfo.packageName.contains("chat") ||
                    appInfo.packageName.contains("social")
                }
                .map { appInfo ->
                    val label = pm.getApplicationLabel(appInfo).toString()
                    label to appInfo.packageName
                }
                .sortedBy { it.first.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val filteredInstalledApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            installedApps.take(10)
        } else {
            installedApps.filter {
                it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.contains(searchQuery, ignoreCase = true)
            }.take(15)
        }
    }

    val existingPackages = remember(existingApps) {
        existingApps.map { it.packageName.lowercase() }.toSet()
    }

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
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Add Monitored App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Popular Messaging Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "POPULAR MESSAGING APPS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        POPULAR_MESSAGING_APPS.forEach { preset ->
                            val alreadyAdded = existingPackages.contains(preset.packageName.lowercase())
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (alreadyAdded) ElegantDarkOutlineVariant.copy(alpha = 0.3f)
                                        else ElegantDarkSurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (alreadyAdded) Color.Transparent else ElegantDarkOutlineVariant,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = !alreadyAdded) {
                                        onAddApp(preset.packageName, preset.appName)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(preset.emoji, fontSize = 13.sp)
                                    Text(
                                        text = preset.appName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (alreadyAdded) ElegantDarkTextSecondary else ElegantDarkTextPrimary
                                    )
                                    if (alreadyAdded) {
                                        Text(
                                            text = "(Added)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = ElegantDarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Package Entry
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "OR ADD CUSTOM APP",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextSecondary
                    )
                    OutlinedTextField(
                        value = customAppName,
                        onValueChange = { customAppName = it },
                        label = { Text("App Name (e.g. Signal)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customPackageName,
                        onValueChange = {
                            customPackageName = it
                            errorMessage = null
                        },
                        label = { Text("Package Name (e.g. org.thoughtcrime.securesms)") },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it, color = SuppressedRed) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Installed Apps Search
                if (installedApps.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SEARCH INSTALLED APPS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextSecondary
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search apps on your device...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = ElegantDarkTextSecondary)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantDarkPrimary,
                                unfocusedBorderColor = ElegantDarkOutline,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElegantDarkBackground)
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredInstalledApps) { (name, pkg) ->
                                val isAdded = existingPackages.contains(pkg.lowercase())
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(enabled = !isAdded) {
                                            onAddApp(pkg, name)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isAdded) ElegantDarkTextSecondary else ElegantDarkTextPrimary
                                        )
                                        Text(
                                            text = pkg,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = ElegantDarkTextSecondary
                                        )
                                    }
                                    if (isAdded) {
                                        Text(
                                            text = "Added",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = AllowedGreen
                                        )
                                    } else {
                                        Text(
                                            text = "+ Add",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantDarkPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pkg = customPackageName.trim()
                    if (pkg.isEmpty()) {
                        errorMessage = "Please enter or pick an app package"
                        return@Button
                    }
                    val name = customAppName.trim().ifEmpty { pkg }
                    onAddApp(pkg, name)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add App", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ElegantDarkTextSecondary)
            }
        }
    )
}
