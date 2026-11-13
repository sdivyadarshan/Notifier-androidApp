package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AllowedGreen
import com.example.ui.theme.ElegantDarkAvatarBg
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkOutlineVariant
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceHighlight
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import com.example.ui.theme.KeptSafeAmber
import com.example.ui.theme.SuppressedRed

@Composable
fun PermissionBanner(
    isListenerGranted: Boolean,
    isOverlayGranted: Boolean,
    isBatteryOptimizedIgnored: Boolean,
    isAlwaysOnEnabled: Boolean,
    onToggleAlwaysOn: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGranted = isListenerGranted && isOverlayGranted && isBatteryOptimizedIgnored
    var isExpanded by remember { mutableStateOf(!allGranted) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("permission_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allGranted) ElegantDarkSurfaceVariant else Color(0xFF1E1610)
        ),
        border = BorderStroke(
            1.dp,
            if (allGranted) ElegantDarkOutline else KeptSafeAmber.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (allGranted) AllowedGreen.copy(alpha = 0.15f)
                            else KeptSafeAmber.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = "Permission Status",
                        tint = if (allGranted) AllowedGreen else KeptSafeAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (allGranted) "Always-On Shield Active" else "Zero-Lag Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (allGranted) AllowedGreen.copy(alpha = 0.2f)
                                    else KeptSafeAmber.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (allGranted) "3/3 Ready" else "${countGranted(isListenerGranted, isOverlayGranted, isBatteryOptimizedIgnored)}/3 Granted",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (allGranted) AllowedGreen else KeptSafeAmber
                            )
                        }
                    }

                    Text(
                        text = if (allGranted) {
                            "Running in background • Zero lag when Notifier is closed"
                        } else {
                            "Grant 'Display Over Other Apps' to make it always on without lag"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ElegantDarkTextSecondary
                )
            }

            // Expanded Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "To eliminate notification delay when the app is closed, Android requires three permissions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Notification Listener Permission
                    PermissionItemRow(
                        title = "1. Notification Access",
                        description = "Intercepts and evaluates incoming Instagram notifications.",
                        isGranted = isListenerGranted,
                        icon = Icons.Default.NotificationsActive,
                        buttonText = "Grant Notification Access",
                        testTag = "grant_listener_button",
                        onGrantClick = {
                            openNotificationListenerSettings(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Display Over Other Apps Permission (User's specific request!)
                    PermissionItemRow(
                        title = "2. Display Over Other Apps",
                        description = "Allows Notifier to run constantly in the background and dismiss unwanted reels without OEM sleep lag.",
                        isGranted = isOverlayGranted,
                        icon = Icons.Default.Layers,
                        buttonText = "Allow Display Over Apps",
                        testTag = "grant_overlay_button",
                        onGrantClick = {
                            openOverlaySettings(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Unrestricted Battery / Battery Optimization
                    PermissionItemRow(
                        title = "3. Unrestricted Battery",
                        description = "Prevents Android Doze mode from freezing the background listener when locked.",
                        isGranted = isBatteryOptimizedIgnored,
                        icon = Icons.Default.BatterySaver,
                        buttonText = "Disable Battery Optimization",
                        testTag = "grant_battery_button",
                        onGrantClick = {
                            requestIgnoreBatteryOptimizations(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Always-on Background Service Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElegantDarkSurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isAlwaysOnEnabled) ElegantDarkPrimary else ElegantDarkTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Always-On Background Shield",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkTextPrimary
                                )
                                Text(
                                    text = "Runs low-overhead persistent service to guarantee 0ms lag.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantDarkTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isAlwaysOnEnabled,
                            onCheckedChange = onToggleAlwaysOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElegantDarkOnPrimary,
                                checkedTrackColor = ElegantDarkPrimary,
                                uncheckedThumbColor = ElegantDarkTextSecondary,
                                uncheckedTrackColor = ElegantDarkSurfaceHighlight
                            ),
                            modifier = Modifier.testTag("always_on_service_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Background Reliability Guide for Realme / ColorOS / Xiaomi / OnePlus / Samsung
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B2028)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF384556))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ElegantDarkPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = ElegantDarkPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Background Reliability Guide",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantDarkTextPrimary
                                    )
                                    Text(
                                        text = "Keep filter working when app is closed or cleared",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElegantDarkTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "• Persistent Shield: The ongoing status bar notification ensures HiOS / Android gives Notifier foreground priority.\n• Lock in Recents: In Recent Apps (multitasking carousel), tap the padlock 🔒 on Notifier to prevent HiOS from swiping it away.\n• HiOS 15 App Info: Long press Notifier icon > App Info > Battery > select 'Unrestricted' and toggle 'Allow background activity' ON.\n• Turn OFF 'Pause app activity if unused' in App Info.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantDarkTextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { requestIgnoreBatteryOptimizations(context) },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .testTag("battery_opt_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantDarkPrimary,
                                        contentColor = ElegantDarkOnPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatterySaver,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Battery Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { openAppSettings(context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_app_settings_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("App Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        com.example.service.InstagramNotificationListenerService.rebindService(context)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("rebind_listener_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Rebind", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    buttonText: String,
    testTag: String,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) ElegantDarkSurface else Color(0xFF241C16)
        ),
        border = BorderStroke(
            1.dp,
            if (isGranted) ElegantDarkOutlineVariant else KeptSafeAmber.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) AllowedGreen.copy(alpha = 0.15f)
                            else KeptSafeAmber.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) AllowedGreen else KeptSafeAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantDarkTextSecondary
                    )
                }

                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = AllowedGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private fun countGranted(vararg permissions: Boolean): Int {
    return permissions.count { it }
}

private fun openNotificationListenerSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun openOverlaySettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    }
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    }
}

private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
