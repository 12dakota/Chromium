package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.EngineType
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.GeckoPrimary
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun BottomBrowserBar(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onToggleEngine: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarksHistory: () -> Unit,
    onOpenDevTools: () -> Unit,
    onOpenEngineInspector: () -> Unit,
    onOpenAuthCloud: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleReader: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = SurfaceDark,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onGoBack,
                enabled = activeTab.canGoBack || activeTab.url != "about:home",
                modifier = Modifier.testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (activeTab.canGoBack || activeTab.url != "about:home") TextPrimaryDark else TextTertiaryDark
                )
            }

            // Forward Button
            IconButton(
                onClick = onGoForward,
                enabled = activeTab.canGoForward,
                modifier = Modifier.testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (activeTab.canGoForward) TextPrimaryDark else TextTertiaryDark
                )
            }

            // Central Dual-Engine Fast Switcher Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceVariantDark)
                    .clickable { onToggleEngine() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("quick_engine_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Engine",
                        tint = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) "Chromium" else "Gecko",
                        color = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) ChromiumPrimary else GeckoPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Home Button
            IconButton(
                onClick = onGoHome,
                modifier = Modifier.testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (activeTab.url == "about:home") ChromiumPrimary else TextPrimaryDark
                )
            }

            // Bookmarks & History
            IconButton(
                onClick = onOpenBookmarksHistory,
                modifier = Modifier.testTag("bookmarks_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarks and History",
                    tint = TextPrimaryDark
                )
            }

            // More Menu Button with Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("browser_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = TextPrimaryDark
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceCardDark)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isBookmarked) "Remove Bookmark" else "Bookmark Page",
                                color = TextPrimaryDark
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = ChromiumPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleBookmark()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Engine Diagnostics & Inspector", color = TextPrimaryDark) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GeckoPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenEngineInspector()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Developer Tools & Console", color = TextPrimaryDark) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                tint = ChromiumPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenDevTools()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Firebase Cloud & Account", color = TextPrimaryDark) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFFFFCA28)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenAuthCloud()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                if (activeTab.isDesktopMode) "Request Mobile Site" else "Request Desktop Site",
                                color = TextPrimaryDark
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleDesktop()
                        }
                    )

                    if (activeTab.url != "about:home") {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (activeTab.isReaderMode) "Exit Reader Mode" else "Reader Mode",
                                    color = TextPrimaryDark
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleReader()
                            }
                        )
                    }
                }
            }
        }
    }
}
