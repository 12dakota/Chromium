package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EngineDiagnostics
import com.example.data.model.EngineType
import com.example.data.model.HistoryItem
import com.example.data.model.UserProfile
import com.example.ui.theme.ChromiumAccent
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.GeckoAccent
import com.example.ui.theme.GeckoPrimary
import com.example.ui.theme.GeckoPurple
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.viewmodel.SpeedDialItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedDialHome(
    activeEngine: EngineType,
    userProfile: UserProfile,
    isSyncing: Boolean,
    syncMessage: String?,
    engineDiagnostics: EngineDiagnostics,
    speedDialShortcuts: List<SpeedDialItem>,
    recentHistory: List<HistoryItem>,
    onNavigate: (String) -> Unit,
    onToggleEngine: () -> Unit,
    onOpenEngineInspector: () -> Unit,
    onOpenDevTools: () -> Unit,
    onOpenAuthCloud: () -> Unit,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Hero Dual-Engine Banner
        DualEngineHeroCard(
            activeEngine = activeEngine,
            onToggleEngine = onToggleEngine,
            onOpenInspector = onOpenEngineInspector
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Firebase Auth & Cloud Sync Strip
        FirebaseAuthCard(
            userProfile = userProfile,
            isSyncing = isSyncing,
            syncMessage = syncMessage,
            onOpenAuth = onOpenAuthCloud,
            onTriggerSync = onTriggerSync
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Engine Live Telemetry & Shield Card
        EngineTelemetryCard(
            diagnostics = engineDiagnostics,
            activeEngine = activeEngine,
            onOpenDevTools = onOpenDevTools,
            onOpenInspector = onOpenEngineInspector
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Speed Dial Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Launch Shortcuts",
                color = TextPrimaryDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${speedDialShortcuts.size} Curated Sites",
                color = TextTertiaryDark,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speed Dial Grid (2 Columns)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            speedDialShortcuts.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { item ->
                        SpeedDialCard(
                            item = item,
                            onClick = { onNavigate(item.url) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Recent History Section (if any)
        if (recentHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    color = TextPrimaryDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentHistory.take(6).forEach { historyItem ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceVariantDark)
                            .clickable { onNavigate(historyItem.url) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = ChromiumAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = historyItem.title.take(20),
                                color = TextPrimaryDark,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DualEngineHeroCard(
    activeEngine: EngineType,
    onToggleEngine: () -> Unit,
    onOpenInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChromium = activeEngine == EngineType.CHROMIUM_BLINK
    val gradientBrush = if (isChromium) {
        Brush.linearGradient(listOf(Color(0xFF003852), Color(0xFF0B1B3D)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF4A1800), Color(0xFF2A0845)))
    }
    val borderColor = if (isChromium) ChromiumPrimary else GeckoPrimary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(borderColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isChromium) "CHROMIUM BLINK ACTIVE" else "FIREFOX GECKO ACTIVE",
                            color = borderColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark.copy(alpha = 0.6f))
                            .clickable { onOpenInspector() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = borderColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Inspector",
                                color = borderColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isChromium) "Chromium Blink & V8 Engine" else "Firefox Gecko Quantum Engine",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isChromium)
                        "Hardware-accelerated Skia rendering, V8 JIT compiler, and high-concurrency ServiceWorker network layer."
                    else
                        "Quantum CSS multi-threaded selector parser, SpiderMonkey JS runtime, and Gecko WebRender pipeline.",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onToggleEngine,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = borderColor,
                            contentColor = if (isChromium) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("switch_engine_hero_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isChromium) "Switch to Gecko" else "Switch to Chromium",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseAuthCard(
    userProfile: UserProfile,
    isSyncing: Boolean,
    syncMessage: String?,
    onOpenAuth: () -> Unit,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFA000).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Firebase User",
                            tint = Color(0xFFFFCA28),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (userProfile.isAnonymous) "Guest Mode (Firebase Engine)" else userProfile.displayName,
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (userProfile.isAnonymous) "Tap to sign in & sync bookmarks" else userProfile.email ?: "Cloud Sync Enabled",
                            color = if (userProfile.isAnonymous) TextSecondaryDark else SuccessGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                if (userProfile.isAnonymous) {
                    OutlinedButton(
                        onClick = onOpenAuth,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("firebase_signin_button")
                    ) {
                        Text(text = "Sign In", fontSize = 11.sp, color = Color(0xFFFFCA28))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantDark)
                            .clickable { onTriggerSync() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ChromiumPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sync",
                                    color = SuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!syncMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = syncMessage,
                    color = ChromiumAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun EngineTelemetryCard(
    diagnostics: EngineDiagnostics,
    activeEngine: EngineType,
    onOpenDevTools: () -> Unit,
    onOpenInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ChromiumPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Engine Kernel Diagnostics",
                        color = TextPrimaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${diagnostics.fps} FPS · ${diagnostics.memoryUsageMb} MB",
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryMetricBox(
                    label = "JS Engine",
                    value = if (activeEngine == EngineType.CHROMIUM_BLINK) "V8 12.6" else "SpiderMonkey",
                    color = if (activeEngine == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricBox(
                    label = "DOM Parse",
                    value = "${diagnostics.domParseLatencyMs} ms",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricBox(
                    label = "Layout Pass",
                    value = "${diagnostics.layoutPassLatencyMs} ms",
                    color = ChromiumAccent,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricBox(
                    label = "Shield Block",
                    value = "${diagnostics.adBlockedCount + 14}",
                    color = ShieldBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TelemetryMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariantDark)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondaryDark,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SpeedDialCard(
    item: SpeedDialItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconColor, icon) = when (item.iconCategory) {
        "chromium" -> ChromiumPrimary to Icons.Default.Language
        "gecko" -> GeckoPrimary to Icons.Default.Public
        "code" -> Color(0xFF818CF8) to Icons.Default.Code
        "search" -> Color(0xFF38BDF8) to Icons.Default.Search
        "news" -> Color(0xFFF472B6) to Icons.Default.Language
        else -> ChromiumAccent to Icons.Default.Language
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("speed_dial_${item.title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.engineNote,
                    color = TextTertiaryDark,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
