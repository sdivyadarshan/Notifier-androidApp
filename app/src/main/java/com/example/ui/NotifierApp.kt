package com.example.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import com.example.ui.screens.AboutUsScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.ControlsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.screens.SimulatorScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.example.viewmodel.NotifierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifierApp(
    viewModel: NotifierViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val recentInspectorLogs by viewModel.recentInspectorLogs.collectAsStateWithLifecycle()
    val latestLiveNotification by viewModel.latestLiveNotification.collectAsStateWithLifecycle()
    val isListenerConnected by viewModel.isListenerConnected.collectAsStateWithLifecycle()
    val historyCategoryFilter by viewModel.historyCategoryFilter.collectAsStateWithLifecycle()
    val historyDecisionFilter by viewModel.historyDecisionFilter.collectAsStateWithLifecycle()
    val dryRunResult by viewModel.dryRunResult.collectAsStateWithLifecycle()

    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ElegantDarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFFCB045)
                                    )
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                        )
                    }

                    Column {
                        Text(
                            text = "Notifier",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            color = ElegantDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isListenerConnected) AllowedGreen else SuppressedRed)
                            )
                            Text(
                                text = if (isListenerConnected) "SHIELD ACTIVE" else "SERVICE PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = if (isListenerConnected) AllowedGreen else SuppressedRed
                            )
                        }
                    }
                }

                // Elegant circular action button with subtle border
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkOutline, CircleShape)
                        .clickable { showHelpDialog = true }
                        .testTag("help_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "How Notifier Works",
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .drawBehind {
                        drawLine(
                            color = ElegantDarkOutlineVariant,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .testTag("app_navigation_bar"),
                containerColor = ElegantDarkSurfaceVariant,
                tonalElevation = 0.dp
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElegantDarkOnPrimary,
                    selectedTextColor = ElegantDarkPrimary,
                    indicatorColor = ElegantDarkPrimary,
                    unselectedIconColor = ElegantDarkTextSecondary,
                    unselectedTextColor = ElegantDarkTextSecondary
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Tune else Icons.Outlined.Tune,
                            contentDescription = "Controls"
                        )
                    },
                    label = {
                        Text(
                            "Controls",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_controls")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = {
                        Text(
                            "History",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_history")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.DataObject else Icons.Outlined.DataObject,
                            contentDescription = "Inspector"
                        )
                    },
                    label = {
                        Text(
                            "Inspector",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_inspector")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsActive,
                            contentDescription = "Simulator"
                        )
                    },
                    label = {
                        Text(
                            "Simulator",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_simulator")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setSelectedTab(4) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "About"
                        )
                    },
                    label = {
                        Text(
                            "About",
                            fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_about")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ControlsScreen(
                    viewModel = viewModel,
                    preferences = preferences,
                    stats = stats,
                    isListenerConnected = isListenerConnected
                )
                1 -> HistoryScreen(
                    viewModel = viewModel,
                    logs = allLogs,
                    selectedCategory = historyCategoryFilter,
                    selectedDecision = historyDecisionFilter
                )
                2 -> InspectorScreen(
                    viewModel = viewModel,
                    recentLogs = recentInspectorLogs,
                    latestLiveNotification = latestLiveNotification
                )
                3 -> SimulatorScreen(
                    viewModel = viewModel,
                    dryRunResult = dryRunResult
                )
                4 -> AboutUsScreen()
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "How Notifier Works",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Notifier is a companion layer that intercepts Instagram notifications through Android's Notification Access system.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Granular Control: Choose which categories (DMs, Reels, Likes, etc.) you want to see or silently dismiss.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Safety Principle: If a notification structure is ambiguous or confidence is low, Notifier will NEVER dismiss it. Important messages are always preserved.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• 100% On-Device: No network requests, no accounts, and no data tracking.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
