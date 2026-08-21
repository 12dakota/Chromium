package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.EngineDiagnostics
import com.example.data.model.EngineType
import com.example.ui.theme.ChromiumAccent
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GeckoAccent
import com.example.ui.theme.GeckoPrimary
import com.example.ui.theme.GeckoPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineInspectorSheet(
    activeTab: BrowserTab,
    diagnostics: EngineDiagnostics,
    sheetState: SheetState,
    onSetEngine: (EngineType) -> Unit,
    onToggleDesktop: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dual Engine Inspector",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Engine Selection Cards (Side-by-side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chromium Card
                EngineSelectorCard(
                    engineType = EngineType.CHROMIUM_BLINK,
                    isSelected = activeTab.engineType == EngineType.CHROMIUM_BLINK,
                    onClick = { onSetEngine(EngineType.CHROMIUM_BLINK) },
                    modifier = Modifier.weight(1f).testTag("select_chromium_engine")
                )

                // Gecko Card
                EngineSelectorCard(
                    engineType = EngineType.FIREFOX_GECKO,
                    isSelected = activeTab.engineType == EngineType.FIREFOX_GECKO,
                    onClick = { onSetEngine(EngineType.FIREFOX_GECKO) },
                    modifier = Modifier.weight(1f).testTag("select_gecko_engine")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Architecture Breakdown Table
            Text(
                text = "Kernel Specifications",
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SpecRow(label = "Rendering Pipeline", value = activeTab.engineType.renderingPipeline)
                    SpecRow(label = "JavaScript JIT", value = activeTab.engineType.jsEngine)
                    SpecRow(label = "Engine Core", value = activeTab.engineType.engineCore)
                    SpecRow(label = "CSS Vendor Prefix", value = activeTab.engineType.vendorPrefix)
                    SpecRow(label = "Security Protocol", value = diagnostics.httpVersion)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Hardware Performance Telemetry
            Text(
                text = "Live Benchmark Telemetry",
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "DOM Parse Latency", color = TextSecondaryDark, fontSize = 11.sp)
                            Text(text = "${diagnostics.domParseLatencyMs} ms", color = SuccessGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text(text = "Layout Pass Latency", color = TextSecondaryDark, fontSize = 11.sp)
                            Text(text = "${diagnostics.layoutPassLatencyMs} ms", color = ChromiumAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text(text = "Active FPS", color = TextSecondaryDark, fontSize = 11.sp)
                            Text(text = "${diagnostics.fps} FPS", color = ChromiumPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engine Feature Matrix
            Text(
                text = "Supported Web Standards",
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    FeatureCheckRow("WebGL 2.0 & WebGPU Canvas", true)
                    FeatureCheckRow("WebAssembly (WASM) Threads", true)
                    FeatureCheckRow("ServiceWorkers & CacheStorage", true)
                    FeatureCheckRow(if (activeTab.engineType == EngineType.CHROMIUM_BLINK) "Blink Shadow DOM v1" else "Gecko Quantum CSS Grid", true)
                    FeatureCheckRow("Content Security Policy (CSP v3)", true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User-Agent String Viewer
            Text(
                text = "Active User-Agent Header",
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark)
                    .padding(10.dp)
            ) {
                Text(
                    text = if (activeTab.isDesktopMode) activeTab.engineType.getDesktopUserAgent() else activeTab.engineType.defaultUserAgent,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun EngineSelectorCard(
    engineType: EngineType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChromium = engineType == EngineType.CHROMIUM_BLINK
    val accentColor = if (isChromium) ChromiumPrimary else GeckoPrimary

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, accentColor, RoundedCornerShape(14.dp))
                } else {
                    Modifier.border(1.dp, SurfaceVariantDark, RoundedCornerShape(14.dp))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) (if (isChromium) Color(0xFF00293D) else Color(0xFF3B1300)) else SurfaceCardDark
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChromium) "Chromium" else "Firefox Gecko",
                    color = TextPrimaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isChromium) "Blink / V8 Core" else "Gecko / Quantum",
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondaryDark, fontSize = 12.sp)
        Text(text = value, color = TextPrimaryDark, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FeatureCheckRow(name: String, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = TextPrimaryDark, fontSize = 12.sp)
        Icon(
            imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (enabled) SuccessGreen else DangerRed,
            modifier = Modifier.size(14.dp)
        )
    }
}
