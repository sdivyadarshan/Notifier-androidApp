package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NotifierStats
import com.example.data.SpecialPersonEntity
import com.example.model.FilterAction
import com.example.model.InstagramCategory
import com.example.ui.components.AddCustomRuleDialog
import com.example.ui.components.AddMonitoredAppDialog
import com.example.ui.components.CustomRulesCard
import com.example.ui.components.MonitoredAppsCard
import com.example.ui.components.PermissionBanner
import com.example.ui.theme.AllowedGreen
import com.example.ui.theme.ElegantDarkAvatarBg
import com.example.ui.theme.ElegantDarkBackground
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
import com.example.viewmodel.NotifierViewModel
import com.example.viewmodel.PresetType

@Composable
fun ControlsScreen(
    viewModel: NotifierViewModel,
    preferences: Map<InstagramCategory, FilterAction>,
    stats: NotifierStats,
    isListenerConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val safeModeEnabled by viewModel.safeModeEnabled.collectAsStateWithLifecycle()
    val specialPersons by viewModel.specialPersons.collectAsStateWithLifecycle()
    val customRules by viewModel.customRules.collectAsStateWithLifecycle()
    val monitoredApps by viewModel.monitoredApps.collectAsStateWithLifecycle()

    val isSettingUpFilter by viewModel.isSettingUpFilter.collectAsStateWithLifecycle()
    val setupProgress by viewModel.setupProgress.collectAsStateWithLifecycle()
    val setupStatusMessage by viewModel.setupStatusMessage.collectAsStateWithLifecycle()
    val activeSetupPreset by viewModel.activeSetupPreset.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val isListenerPermissionGranted by viewModel.isListenerPermissionGranted.collectAsStateWithLifecycle()
    val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted.collectAsStateWithLifecycle()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsStateWithLifecycle()
    val alwaysOnServiceEnabled by viewModel.alwaysOnServiceEnabled.collectAsStateWithLifecycle()

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<SpecialPersonEntity?>(null) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddAppDialog by remember { mutableStateOf(false) }

    // 3-5 Second Setup Progress Dialog
    if (isSettingUpFilter) {
        FilterSetupProgressDialog(
            presetName = activeSetupPreset ?: "Filter Option",
            progress = setupProgress,
            statusMessage = setupStatusMessage
        )
    }

    // Add or Edit Special Person Dialog
    if (showAddPersonDialog || personToEdit != null) {
        AddSpecialPersonDialog(
            initialPerson = personToEdit,
            onDismiss = {
                showAddPersonDialog = false
                personToEdit = null
            },
            onAddPerson = { instaId, nickname, reelsOnly ->
                if (personToEdit != null) {
                    viewModel.updateSpecialPerson(personToEdit!!.id, instaId, nickname, reelsOnly)
                } else {
                    viewModel.addSpecialPerson(instaId, nickname, reelsOnly)
                }
                showAddPersonDialog = false
                personToEdit = null
            }
        )
    }

    // Add Custom Prefix / Keyword Rule Dialog
    if (showAddRuleDialog) {
        AddCustomRuleDialog(
            monitoredApps = monitoredApps,
            onDismiss = { showAddRuleDialog = false },
            onAddRule = { appPkg, appName, matchType, pattern, targetField, action ->
                viewModel.addCustomRule(
                    appPackage = appPkg,
                    appName = appName,
                    matchType = matchType,
                    pattern = pattern,
                    targetField = targetField,
                    action = action
                )
                showAddRuleDialog = false
            }
        )
    }

    // Add Monitored Messaging App Dialog
    if (showAddAppDialog) {
        AddMonitoredAppDialog(
            existingApps = monitoredApps,
            onDismiss = { showAddAppDialog = false },
            onAddApp = { pkg, name ->
                viewModel.addMonitoredApp(pkg, name)
                showAddAppDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .testTag("controls_screen_column"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PermissionBanner(
                isListenerGranted = isListenerConnected || isListenerPermissionGranted,
                isOverlayGranted = isOverlayPermissionGranted,
                isBatteryOptimizedIgnored = isBatteryOptimizationIgnored,
                isAlwaysOnEnabled = alwaysOnServiceEnabled,
                onToggleAlwaysOn = { enabled ->
                    viewModel.setAlwaysOnServiceEnabled(context, enabled)
                }
            )
        }

        // Hero Card: Protection Status
        item {
            ProtectionStatusCard(
                stats = stats,
                isListenerConnected = isListenerConnected,
                preferences = preferences
            )
        }

        // Safe Mode / Fallback Protection Card
        item {
            SafeModeFallbackCard(
                isSafeModeEnabled = safeModeEnabled,
                onToggleSafeMode = { viewModel.setSafeModeEnabled(it) }
            )
        }

        // PRIMARY QUICK MODES (User requested: Only DMs, Only Reels, Only Photos & Videos)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "QUICK FILTER MODES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = ElegantDarkTextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "3-5s Setup",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = ElegantDarkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary Quick Modes Grid / Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        QuickModeCard(
                            title = "Only DMs",
                            emoji = "💬",
                            subtitle = "Chats & story replies only",
                            badge = "Focus",
                            onClick = { viewModel.applyPresetWithSetup(PresetType.ONLY_DMS) }
                        )
                    }
                    item {
                        QuickModeCard(
                            title = "Only Reels",
                            emoji = "📹",
                            subtitle = "Watch clips & video shares",
                            badge = "Reels",
                            onClick = { viewModel.applyPresetWithSetup(PresetType.ONLY_REELS) }
                        )
                    }
                    item {
                        QuickModeCard(
                            title = "Only Photos & Videos",
                            emoji = "📸",
                            subtitle = "Feed photos, carousels & video posts",
                            badge = "Media",
                            onClick = { viewModel.applyPresetWithSetup(PresetType.ONLY_PHOTOS_AND_VIDEOS) }
                        )
                    }
                    item {
                        QuickModeCard(
                            title = "Special Persons Reels",
                            emoji = "⭐",
                            subtitle = "DMs + Reels from VIP list only",
                            badge = "VIP",
                            onClick = { viewModel.applyPresetWithSetup(PresetType.SPECIAL_PERSONS_REELS) }
                        )
                    }
                }
            }
        }

        // CUSTOM PREFIX & KEYWORD RULES (User Directive: if notification starts like 'sent a reel...' then suppress it)
        item {
            CustomRulesCard(
                customRules = customRules,
                onAddRuleClick = { showAddRuleDialog = true },
                onToggleRule = { id, enabled -> viewModel.toggleCustomRule(id, enabled) },
                onDeleteRule = { viewModel.removeCustomRule(it) },
                onQuickAddRule = { pattern, matchType ->
                    viewModel.addCustomRule(
                        appPackage = "com.instagram.android",
                        appName = "Instagram",
                        matchType = matchType,
                        pattern = pattern,
                        targetField = "TEXT",
                        action = "SUPPRESS"
                    )
                }
            )
        }

        // MONITORED MESSAGING APPS (User Directive: option to add a new App for messages)
        item {
            MonitoredAppsCard(
                monitoredApps = monitoredApps,
                onAddAppClick = { showAddAppDialog = true },
                onToggleApp = { pkg, enabled -> viewModel.toggleMonitoredApp(pkg, enabled) },
                onDeleteApp = { viewModel.removeMonitoredApp(it) }
            )
        }

        // SPECIAL PERSONS EXCEPTION LIST (User Directive: add exception option where user enter insta id or nickname)
        item {
            SpecialPersonsCard(
                specialPersons = specialPersons,
                onAddPersonClick = { showAddPersonDialog = true },
                onRemovePerson = { viewModel.removeSpecialPerson(it) },
                onEditPerson = { personToEdit = it }
            )
        }

        // Quick Presets Row (Balanced, Personal Only, Focus Shield, Allow All)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "MORE PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = ElegantDarkTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(
                        listOf(
                            PresetType.PERSONAL_ONLY,
                            PresetType.BALANCED,
                            PresetType.SUPPRESS_ALL_SOCIAL,
                            PresetType.ALLOW_ALL
                        )
                    ) { preset ->
                        PresetCard(
                            preset = preset,
                            onClick = { viewModel.applyPresetWithSetup(preset) }
                        )
                    }
                }
            }
        }

        // Header for Granular Filter Rules
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    text = "CATEGORY FILTER RULES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = ElegantDarkTextSecondary
                )
            }
        }

        // Switchboard Items for each category
        items(InstagramCategory.entries) { category ->
            val action = preferences[category] ?: category.defaultAction
            val isAllowed = action == FilterAction.ALLOW
            val isUnknownCategory = category == InstagramCategory.UNKNOWN

            CategoryControlCard(
                category = category,
                isAllowed = isAllowed,
                isUnknownCategory = isUnknownCategory,
                onToggle = {
                    if (!isUnknownCategory) {
                        viewModel.toggleCategoryAction(category)
                    }
                }
            )
        }

        // Quick Inspector / Debug Launcher Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElegantDarkSurfaceVariant)
                    .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(16.dp))
                    .clickable { viewModel.setSelectedTab(2) }
                    .padding(16.dp)
                    .testTag("inspector_shortcut_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElegantDarkPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataObject,
                                contentDescription = null,
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Notification Inspector",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElegantDarkAvatarBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DEBUG",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3-5 Second Setup Progress Dialog.
 * Fulfills the user requirement: "if select any option like only dm , only reels
 * like take some 3-5 seconds make changes and setup that change".
 */
@Composable
private fun FilterSetupProgressDialog(
    presetName: String,
    progress: Float,
    statusMessage: String
) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissible while applying setup */ },
        containerColor = ElegantDarkSurface,
        titleContentColor = ElegantDarkTextPrimary,
        textContentColor = ElegantDarkTextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Configuring $presetName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextPrimary
                )
                Text(
                    text = "Applying changes in 3-5 seconds...",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkPrimary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElegantDarkPrimary,
                    trackColor = ElegantDarkSurfaceHighlight
                )

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            // Self-dismissing after timer finishes
        }
    )
}

/**
 * Special Persons Exception List Card.
 * Enables users to add Instagram IDs or nicknames so that sent Reels are never muted.
 */
@Composable
private fun SpecialPersonsCard(
    specialPersons: List<SpecialPersonEntity>,
    onAddPersonClick: () -> Unit,
    onRemovePerson: (Long) -> Unit,
    onEditPerson: (SpecialPersonEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("special_persons_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, ElegantDarkOutline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KeptSafeAmber.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = KeptSafeAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Special Persons",
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
                                    text = "${specialPersons.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                        Text(
                            text = "Reels from these users will always notify you",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onAddPersonClick,
                    modifier = Modifier.testTag("add_special_person_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Special Person",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Explanatory note
            Text(
                text = "When reels are suppressed, reels sent by your Special Persons will automatically bypass the filter so you never miss reels from close friends or family.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                lineHeight = 17.sp
            )

            // List of Persons or Empty State
            if (specialPersons.isEmpty()) {
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
                            text = "No Special Persons added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap '+ Add' above to enter an Instagram ID or nickname",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    specialPersons.forEach { person ->
                        SpecialPersonItemRow(
                            person = person,
                            onDelete = { onRemovePerson(person.id) },
                            onEdit = { onEditPerson(person) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpecialPersonItemRow(
    person: SpecialPersonEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ElegantDarkSurfaceVariant)
            .border(1.dp, ElegantDarkOutlineVariant, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onEdit
            )
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = person.nickname.ifBlank { person.instagramId }.take(1).uppercase()
                    Text(
                        text = initial.ifBlank { "★" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = person.nickname.ifBlank { "@${person.instagramId}" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ElegantDarkTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AllowedGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (person.reelsOnly) "REELS ALLOWED" else "ALL ALLOWED",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AllowedGreen
                            )
                        }
                    }
                    if (person.nickname.isNotBlank() && person.instagramId.isNotBlank()) {
                        Text(
                            text = "@${person.instagramId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove person",
                    tint = ElegantDarkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Dialog to add or edit a Special Person by Instagram ID and/or nickname.
 */
@Composable
private fun AddSpecialPersonDialog(
    initialPerson: SpecialPersonEntity? = null,
    onDismiss: () -> Unit,
    onAddPerson: (String, String, Boolean) -> Unit
) {
    var instaId by remember { mutableStateOf(initialPerson?.instagramId ?: "") }
    var nickname by remember { mutableStateOf(initialPerson?.nickname ?: "") }
    var reelsOnly by remember { mutableStateOf(initialPerson?.reelsOnly ?: true) }
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
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (initialPerson != null) "Edit Special Person" else "Add Special Person",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Enter the person's Instagram handle or nickname as it appears in notifications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextSecondary
                )

                OutlinedTextField(
                    value = instaId,
                    onValueChange = {
                        instaId = it
                        errorMessage = null
                    },
                    label = { Text("Instagram ID / Handle") },
                    placeholder = { Text("e.g. alex_smith or @alex") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextSecondary,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        nickname = it
                        errorMessage = null
                    },
                    label = { Text("Nickname / Display Name (Optional)") },
                    placeholder = { Text("e.g. Alex, Best Friend") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextSecondary,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Only Sent Reels Bypass",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkTextPrimary
                        )
                        Text(
                            text = "Only incoming reels from this user bypass filter",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = reelsOnly,
                        onCheckedChange = { reelsOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantDarkOnPrimary,
                            checkedTrackColor = ElegantDarkPrimary
                        )
                    )
                }

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = SuppressedRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (instaId.isBlank() && nickname.isBlank()) {
                        errorMessage = "Please enter either an Instagram ID or a Nickname."
                    } else {
                        onAddPerson(instaId, nickname, reelsOnly)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Person")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ElegantDarkTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun QuickModeCard(
    title: String,
    emoji: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ElegantDarkSurface)
            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("quick_mode_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElegantDarkPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkTextPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = ElegantDarkTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ProtectionStatusCard(
    stats: NotifierStats,
    isListenerConnected: Boolean,
    preferences: Map<InstagramCategory, FilterAction>
) {
    val suppressedCount = preferences.count { it.value == FilterAction.SUPPRESS }
    val savedMinutes = (stats.suppressedCount * 2).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("protection_status_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkPrimary
        )
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Instagram Protection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkOnPrimary
                    )
                    Text(
                        text = if (isListenerConnected) "Real-time filtering active" else "Awaiting listener access",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkOnPrimary.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkOnPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElegantDarkOnPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProtectionStatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = "${stats.suppressedCount}",
                    label = "Suppressed",
                    color = SuppressedRed
                )
                ProtectionStatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = "${stats.allowedCount}",
                    label = "Allowed",
                    color = AllowedGreen
                )
                ProtectionStatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = "${stats.keptSafeCount}",
                    label = "Kept Safe",
                    color = KeptSafeAmber
                )
            }
        }
    }
}

@Composable
private fun ProtectionStatMiniCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ElegantDarkOnPrimary.copy(alpha = 0.12f))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ElegantDarkOnPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = ElegantDarkOnPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PresetCard(
    preset: PresetType,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ElegantDarkSurface)
            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("preset_${preset.name.lowercase()}"),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = preset.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkPrimary
            )
            Text(
                text = when (preset) {
                    PresetType.ONLY_DMS -> "DMs & story replies only"
                    PresetType.ONLY_REELS -> "Reels only"
                    PresetType.ONLY_PHOTOS_AND_VIDEOS -> "Feed photos & videos only"
                    PresetType.SPECIAL_PERSONS_REELS -> "VIP reels & DMs"
                    PresetType.PERSONAL_ONLY -> "DMs & Story replies only"
                    PresetType.BALANCED -> "DMs, Comments & Mentions"
                    PresetType.ALLOW_ALL -> "No suppression"
                    PresetType.SUPPRESS_ALL_SOCIAL -> "Strict DM-only mode"
                },
                style = MaterialTheme.typography.labelSmall,
                color = ElegantDarkTextSecondary
            )
        }
    }
}

@Composable
private fun CategoryControlCard(
    category: InstagramCategory,
    isAllowed: Boolean,
    isUnknownCategory: Boolean,
    onToggle: () -> Unit
) {
    val cardBackground by animateColorAsState(
        targetValue = if (isAllowed) ElegantDarkSurface else ElegantDarkSurface.copy(alpha = 0.7f),
        label = "categoryCardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("category_card_${category.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = BorderStroke(
            1.dp,
            if (isAllowed) ElegantDarkOutlineVariant else ElegantDarkOutlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Emoji in Avatar Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkAvatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 18.sp
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkTextPrimary
                        )
                        if (isUnknownCategory) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElegantDarkPrimary.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SAFETY LOCK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isAllowed) AllowedGreen else SuppressedRed)
                        )
                        Text(
                            text = if (isAllowed) "SHOWING NOTIFICATIONS" else "SUPPRESSED / MUTED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAllowed) AllowedGreen else SuppressedRed
                        )
                    }
                }
            }

            Switch(
                checked = isAllowed,
                onCheckedChange = { onToggle() },
                enabled = !isUnknownCategory,
                modifier = Modifier.testTag("switch_${category.name.lowercase()}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElegantDarkOnPrimary,
                    checkedTrackColor = ElegantDarkPrimary,
                    uncheckedThumbColor = ElegantDarkTextSecondary,
                    uncheckedTrackColor = ElegantDarkOutline,
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SafeModeFallbackCard(
    isSafeModeEnabled: Boolean,
    onToggleSafeMode: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("safe_mode_fallback_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isSafeModeEnabled) ElegantDarkPrimary.copy(alpha = 0.5f) else ElegantDarkOutlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSafeModeEnabled) ElegantDarkPrimary.copy(alpha = 0.18f) else ElegantDarkAvatarBg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isSafeModeEnabled) ElegantDarkPrimary else ElegantDarkTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Safe Mode Fallback",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSafeModeEnabled) AllowedGreen.copy(alpha = 0.15f) else SuppressedRed.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSafeModeEnabled) "Active" else "Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSafeModeEnabled) AllowedGreen else SuppressedRed
                                )
                            }
                        }
                        Text(
                            text = "Preserve alerts if classification is uncertain",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isSafeModeEnabled,
                    onCheckedChange = onToggleSafeMode,
                    modifier = Modifier.testTag("safe_mode_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantDarkOnPrimary,
                        checkedTrackColor = ElegantDarkPrimary,
                        uncheckedThumbColor = ElegantDarkTextSecondary,
                        uncheckedTrackColor = ElegantDarkOutline,
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "When enabled, if Notifier cannot confidently classify an incoming Instagram notification (e.g. uncertain or ambiguous structure), it defaults to allowing the notification to be shown. This prevents the accidental loss of important personal communication.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
