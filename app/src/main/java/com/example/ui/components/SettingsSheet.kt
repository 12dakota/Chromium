package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EngineSelectionMode
import com.example.data.model.EngineType
import com.example.data.model.SearchEngine
import com.example.data.model.UserProfile
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GeckoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: BrowserViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenAuthCloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("General", "Dual Engine", "Privacy & Shields", "Cloud Sync", "About")

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        modifier = modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChromiumPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ChromiumPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Browser Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Engine profiles, sync & preferences",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("settings_done_button")
                ) {
                    Text("Done", color = ChromiumPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceDark,
                contentColor = ChromiumPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = SurfaceVariantDark) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ChromiumPrimary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == index) TextPrimaryDark else TextSecondaryDark
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> GeneralSettingsTab(
                        viewModel = viewModel,
                        onShowClearData = { showClearDataDialog = true }
                    )
                    1 -> DualEngineSettingsTab(
                        viewModel = viewModel,
                        onShowAddRule = { showAddRuleDialog = true }
                    )
                    2 -> PrivacyShieldsTab(viewModel = viewModel)
                    3 -> CloudSyncSettingsTab(
                        viewModel = viewModel,
                        onOpenAuthDialog = {
                            onDismiss()
                            onOpenAuthCloud()
                        }
                    )
                    4 -> AboutTab()
                }
            }
        }
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        ClearBrowsingDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = { history, bookmarks, logs ->
                viewModel.clearBrowsingData(history, bookmarks, logs)
                showClearDataDialog = false
            }
        )
    }

    // Add Domain Rule Dialog
    if (showAddRuleDialog) {
        AddDomainRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onAddRule = { domain, engine ->
                viewModel.setCustomDomainRule(domain, engine)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun GeneralSettingsTab(
    viewModel: BrowserViewModel,
    onShowClearData: () -> Unit
) {
    val selectedSearchEngine = viewModel.selectedSearchEngine.value
    val jsEnabled = viewModel.javascriptEnabled.value
    val desktopDefault = viewModel.desktopByDefault.value

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsSectionHeader("Search Engine")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    viewModel.availableSearchEngines.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectSearchEngine(engine) }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (selectedSearchEngine.id == engine.id) ChromiumPrimary else TextTertiaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = engine.name,
                                    fontSize = 14.sp,
                                    color = TextPrimaryDark,
                                    fontWeight = if (selectedSearchEngine.id == engine.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            RadioButton(
                                selected = selectedSearchEngine.id == engine.id,
                                onClick = { viewModel.selectSearchEngine(engine) },
                                colors = RadioButtonDefaults.colors(selectedColor = ChromiumPrimary)
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionHeader("Web Browsing Preferences")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsSwitchRow(
                        title = "Enable JavaScript (V8 / SpiderMonkey)",
                        subtitle = "Allows dynamic scripting and interactive web apps",
                        icon = Icons.Default.Code,
                        checked = jsEnabled,
                        onCheckedChange = { viewModel.setJavascriptEnabled(it) }
                    )
                    HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                    SettingsSwitchRow(
                        title = "Always Request Desktop Site",
                        subtitle = "Forces desktop browser viewport and user agent",
                        icon = Icons.Default.DesktopWindows,
                        checked = desktopDefault,
                        onCheckedChange = { viewModel.setDesktopByDefault(it) }
                    )
                }
            }
        }

        item {
            SettingsSectionHeader("Data & Cache Management")
            Button(
                onClick = onShowClearData,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Browsing Data & Cache", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DualEngineSettingsTab(
    viewModel: BrowserViewModel,
    onShowAddRule: () -> Unit
) {
    val selectionMode = viewModel.engineSelectionMode.value
    val customRules = viewModel.customDomainRules.value

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsSectionHeader("Engine Selection Strategy")
            Text(
                text = "Configure how DualEngine routes websites to Chromium Blink vs Firefox Gecko.",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    EngineSelectionMode.values().forEach { mode ->
                        val isSelected = selectionMode == mode
                        val accentColor = when (mode) {
                            EngineSelectionMode.AUTO_DETECT -> Color(0xFF00E5FF)
                            EngineSelectionMode.ALWAYS_CHROMIUM -> ChromiumPrimary
                            EngineSelectionMode.ALWAYS_GECKO -> GeckoPrimary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { viewModel.setEngineSelectionMode(mode) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setEngineSelectionMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) accentColor else TextPrimaryDark
                                    )
                                    if (mode == EngineSelectionMode.AUTO_DETECT) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text("RECOMMENDED", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = mode.subtitle,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsSectionHeader("Smart Domain Routing Rules")
                OutlinedButton(
                    onClick = onShowAddRule,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Site Rule", fontSize = 12.sp)
                }
            }

            Text(
                text = "Map specific websites or domains directly to Blink or Gecko engines.",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (customRules.isEmpty()) {
                        Text(
                            text = "No custom domain rules added yet.",
                            fontSize = 13.sp,
                            color = TextTertiaryDark,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        customRules.forEach { (domain, engine) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = domain,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryDark,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (engine == EngineType.CHROMIUM_BLINK) "Chromium Blink (V8)" else "Firefox Gecko Quantum",
                                        fontSize = 11.sp,
                                        color = if (engine == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeCustomDomainRule(domain) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Rule",
                                        tint = TextTertiaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
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
fun PrivacyShieldsTab(viewModel: BrowserViewModel) {
    val adBlockEnabled = viewModel.adBlockEnabled.value
    val adsBlockedCount = viewModel.adsBlockedCount.value
    val doNotTrack = viewModel.doNotTrack.value
    val forceHttps = viewModel.forceHttps.value
    val blockThirdPartyCookies = viewModel.blockThirdPartyCookies.value

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Shield status banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (adBlockEnabled) Color(0xFF003828) else SurfaceCardDark
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (adBlockEnabled) SuccessGreen.copy(alpha = 0.2f) else SurfaceVariantDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (adBlockEnabled) SuccessGreen else TextTertiaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (adBlockEnabled) "DualShield Active" else "Shields Disabled",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "$adsBlockedCount trackers & invasive ads blocked",
                                fontSize = 12.sp,
                                color = if (adBlockEnabled) SuccessGreen else TextSecondaryDark
                            )
                        }
                    }

                    Switch(
                        checked = adBlockEnabled,
                        onCheckedChange = { viewModel.toggleAdBlock() },
                        colors = SwitchDefaults.colors(checkedThumbColor = SuccessGreen, checkedTrackColor = Color(0xFF004D36))
                    )
                }
            }
        }

        item {
            SettingsSectionHeader("Privacy Controls")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsSwitchRow(
                        title = "Send 'Do Not Track' (DNT) Header",
                        subtitle = "Requests websites not to profile or track your browsing activity",
                        icon = Icons.Default.Security,
                        checked = doNotTrack,
                        onCheckedChange = { viewModel.setDoNotTrack(it) }
                    )
                    HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                    SettingsSwitchRow(
                        title = "Automatic HTTPS Upgrade",
                        subtitle = "Automatically upgrades all insecure HTTP links to encrypted HTTPS",
                        icon = Icons.Default.Lock,
                        checked = forceHttps,
                        onCheckedChange = { viewModel.setForceHttps(it) }
                    )
                    HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                    SettingsSwitchRow(
                        title = "Block Third-Party Cookies",
                        subtitle = "Prevents cross-site advertising networks from tracking you",
                        icon = Icons.Default.Tune,
                        checked = blockThirdPartyCookies,
                        onCheckedChange = { viewModel.setBlockThirdPartyCookies(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun CloudSyncSettingsTab(
    viewModel: BrowserViewModel,
    onOpenAuthDialog: () -> Unit
) {
    val userProfile = viewModel.currentUser.value
    val isSyncing = viewModel.isSyncing.value
    val syncBookmarks = viewModel.syncBookmarks.value
    val syncHistory = viewModel.syncHistory.value
    val syncTabs = viewModel.syncTabs.value
    val coroutineScope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val lastSyncFormatted = remember(userProfile.lastSyncTimestamp) {
        dateFormat.format(Date(userProfile.lastSyncTimestamp))
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Firebase Project Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A0E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCA28).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFFFFCA28),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Project Details",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCA28)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("CONNECTED", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Firebase Project ID: com.aistudio.dualbrowser.cxgk\n• Cloud Sync Service: Firebase Firestore & Google Auth SSO\n• Database Architecture: Multi-Core Cloud Synchronization",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            SettingsSectionHeader("Active Account Profile")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = userProfile.email ?: "Guest Mode (Local Only)",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "Last Synced: $lastSyncFormatted",
                            fontSize = 11.sp,
                            color = TextTertiaryDark
                        )
                    }

                    Button(
                        onClick = onOpenAuthDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = ChromiumPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (userProfile.isAnonymous) "Sign In" else "Switch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            SettingsSectionHeader("Sync Preferences")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsSwitchRow(
                        title = "Sync Bookmarks",
                        subtitle = "Keep bookmarks saved across your devices",
                        icon = Icons.Default.CloudSync,
                        checked = syncBookmarks,
                        onCheckedChange = { viewModel.setSyncBookmarks(it) }
                    )
                    HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                    SettingsSwitchRow(
                        title = "Sync Browsing History",
                        subtitle = "Sync visited URLs and search history",
                        icon = Icons.Default.Sync,
                        checked = syncHistory,
                        onCheckedChange = { viewModel.setSyncHistory(it) }
                    )
                    HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                    SettingsSwitchRow(
                        title = "Sync Open Tabs & Sessions",
                        subtitle = "Seamlessly hand off active tabs to other browsers",
                        icon = Icons.Default.Extension,
                        checked = syncTabs,
                        onCheckedChange = { viewModel.setSyncTabs(it) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.authEngine.triggerCloudSync()
                    }
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCA28)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Syncing with Firebase Cloud...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Now with Firebase", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DualEngine Browser v1.1.0",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The world's premier dual-rendering web browser combining Google Chromium Blink and Mozilla Firefox Gecko Quantum pipelines with intelligent real-time engine auto-detection.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Engine Architecture Specifications",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Chromium Core: Blink 126.0 (V8 JIT v12.6.415)\n• Gecko Core: Mozilla Quantum 128.0 (SpiderMonkey JIT)\n• AdBlock: EasyList & Privacy Shield Filter Engine\n• UI: Jetpack Compose Material Design 3",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ChromiumPrimary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) ChromiumPrimary else TextTertiaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 14.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ChromiumPrimary,
                checkedTrackColor = Color(0xFF003852)
            )
        )
    }
}

@Composable
fun ClearBrowsingDataDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Boolean, Boolean) -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearBookmarks by remember { mutableStateOf(false) }
    var clearLogs by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("Clear Browsing Data", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Select the items you would like to permanently remove:", color = TextSecondaryDark, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = clearHistory,
                        onCheckedChange = { clearHistory = it },
                        colors = CheckboxDefaults.colors(checkedColor = ChromiumPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Browsing History & Visited URLs", color = TextPrimaryDark, fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = clearLogs,
                        onCheckedChange = { clearLogs = it },
                        colors = CheckboxDefaults.colors(checkedColor = ChromiumPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DevTools Logs & Web Cache", color = TextPrimaryDark, fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = clearBookmarks,
                        onCheckedChange = { clearBookmarks = it },
                        colors = CheckboxDefaults.colors(checkedColor = DangerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All Saved Bookmarks", color = TextPrimaryDark, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(clearHistory, clearBookmarks, clearLogs) },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) {
                Text("Clear Selected", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun AddDomainRuleDialog(
    onDismiss: () -> Unit,
    onAddRule: (String, EngineType) -> Unit
) {
    var domainInput by remember { mutableStateOf("") }
    var selectedEngine by remember { mutableStateOf(EngineType.FIREFOX_GECKO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("Add Domain Routing Rule", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Specify a domain name or host keyword to map to a specific rendering engine:", color = TextSecondaryDark, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = domainInput,
                    onValueChange = { domainInput = it },
                    placeholder = { Text("e.g. reddit.com or github.com", color = TextTertiaryDark) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChromiumPrimary,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Preferred Engine:", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedEngine = EngineType.CHROMIUM_BLINK }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedEngine == EngineType.CHROMIUM_BLINK,
                        onClick = { selectedEngine = EngineType.CHROMIUM_BLINK },
                        colors = RadioButtonDefaults.colors(selectedColor = ChromiumPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chromium Blink (V8 Engine)", color = ChromiumPrimary, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedEngine = EngineType.FIREFOX_GECKO }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedEngine == EngineType.FIREFOX_GECKO,
                        onClick = { selectedEngine = EngineType.FIREFOX_GECKO },
                        colors = RadioButtonDefaults.colors(selectedColor = GeckoPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Firefox Gecko (SpiderMonkey)", color = GeckoPrimary, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (domainInput.isNotBlank()) {
                        onAddRule(domainInput.trim(), selectedEngine)
                    }
                },
                enabled = domainInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ChromiumPrimary)
            ) {
                Text("Save Rule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
