package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BrowserTab
import com.example.ui.theme.ChromiumPrimary
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ReaderView(
    activeTab: BrowserTab,
    onExitReaderMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var themeMode by remember { mutableIntStateOf(0) } // 0: Dark, 1: Sepia, 2: Light

    val (bgColor, textColor) = when (themeMode) {
        1 -> Color(0xFFF4ECD8) to Color(0xFF433422) // Sepia
        2 -> Color(0xFFFFFFFF) to Color(0xFF1E293B) // Light
        else -> SurfaceDark to TextPrimaryDark // Dark
    }

    Surface(
        color = bgColor,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Reader Controls Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (themeMode == 0) SurfaceCardDark else bgColor.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = ChromiumPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reader Mode",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font Size Controls
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeMode == 0) SurfaceVariantDark else Color.Gray.copy(alpha = 0.2f))
                            .clickable { if (fontSizeSp > 12f) fontSizeSp -= 2f }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "A-", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeMode == 0) SurfaceVariantDark else Color.Gray.copy(alpha = 0.2f))
                            .clickable { if (fontSizeSp < 26f) fontSizeSp += 2f }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "A+", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Theme selector (Dark / Sepia / Light)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0B132B))
                            .clickable { themeMode = 0 }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF4ECD8))
                            .clickable { themeMode = 1 }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFFFF))
                            .clickable { themeMode = 2 }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onExitReaderMode,
                        modifier = Modifier.size(28.dp).testTag("exit_reader_mode_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Reader Mode", tint = textColor)
                    }
                }
            }

            // Article Content Area
            val styledHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        background-color: ${if (themeMode == 1) "#F4ECD8" else if (themeMode == 2) "#FFFFFF" else "#0B132B"};
                        color: ${if (themeMode == 1) "#433422" else if (themeMode == 2) "#1E293B" else "#F0F4F8"};
                        font-family: Georgia, 'Times New Roman', serif;
                        font-size: ${fontSizeSp}px;
                        line-height: 1.7;
                        padding: 16px;
                        margin: 0 auto;
                        max-width: 680px;
                    }
                    h1 { font-size: 1.6em; line-height: 1.25; margin-bottom: 16px; font-family: sans-serif; font-weight: 700; color: #00B4D8; }
                    h2 { font-size: 1.3em; margin-top: 20px; font-family: sans-serif; }
                    p { margin-bottom: 16px; }
                    blockquote { border-left: 3px solid #00B4D8; padding-left: 12px; margin-left: 0; font-style: italic; opacity: 0.85; }
                </style>
                </head>
                <body>
                    ${if (activeTab.readerContent.isNotBlank()) activeTab.readerContent else "<h1>" + activeTab.title + "</h1><p>Extracting optimized clean reading content from page...</p>"}
                </body>
                </html>
            """.trimIndent()

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        setBackgroundColor(0)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(activeTab.url, styledHtml, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
