package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.EngineType
import com.example.data.model.SecurityLevel
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GeckoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun Omnibox(
    activeTab: BrowserTab,
    tabsCount: Int,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onOpenEngineInspector: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenDevTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember(activeTab.url) {
        mutableStateOf(if (activeTab.url == "about:home") "" else activeTab.url)
    }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val animatedProgress by animateFloatAsState(
        targetValue = if (activeTab.isLoading) activeTab.progress.coerceIn(0.1f, 1.0f) else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Engine Badge Pill (Chromium / Gecko)
                EngineBadge(
                    engineType = activeTab.engineType,
                    onClick = onOpenEngineInspector,
                    modifier = Modifier.testTag("engine_badge_button")
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Omnibox URL Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceCardDark)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // SSL Lock or Search Icon
                        if (activeTab.url == "about:home" || isFocused) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ChromiumPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            val (lockColor, lockIcon) = when (activeTab.securityLevel) {
                                SecurityLevel.SECURE -> SuccessGreen to Icons.Default.Lock
                                SecurityLevel.INSECURE -> DangerRed to Icons.Default.Warning
                                SecurityLevel.LOCAL_PAGE -> ChromiumPrimary to Icons.Default.Security
                            }
                            Icon(
                                imageVector = lockIcon,
                                contentDescription = "Security Status",
                                tint = lockColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // URL / Search Input Field
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(if (activeTab.engineType == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    if (textInput.isNotBlank()) {
                                        onNavigate(textInput)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused }
                                .testTag("omnibox_text_input"),
                            decorationBox = { innerTextField ->
                                if (textInput.isEmpty() && !isFocused) {
                                    Text(
                                        text = "Search or type URL...",
                                        color = TextSecondaryDark,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Clear or Reload Button
                        if (isFocused && textInput.isNotEmpty()) {
                            IconButton(
                                onClick = { textInput = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (activeTab.url != "about:home") {
                            IconButton(
                                onClick = onReload,
                                modifier = Modifier.size(28.dp).testTag("reload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Page",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tabs Counter Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .clickable { onOpenTabs() }
                        .testTag("tabs_counter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabsCount.toString(),
                        color = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // High-Tech Neon Progress Indicator
            if (activeTab.isLoading && animatedProgress > 0f) {
                val progressBrush = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) {
                    Brush.horizontalGradient(listOf(ChromiumPrimary, Color(0xFF80E5FF)))
                } else {
                    Brush.horizontalGradient(listOf(GeckoPrimary, Color(0xFFFF9100)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(SurfaceCardDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(2.5.dp)
                            .background(progressBrush)
                    )
                }
            }
        }
    }
}

@Composable
fun EngineBadge(
    engineType: EngineType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChromium = engineType == EngineType.CHROMIUM_BLINK
    val badgeBg = if (isChromium) Color(0xFF003852) else Color(0xFF4A1F00)
    val badgeTextColor = if (isChromium) ChromiumPrimary else GeckoPrimary
    val label = if (isChromium) "Blink" else "Gecko"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg)
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeTextColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = badgeTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
