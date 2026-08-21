package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DevToolLog
import com.example.data.model.DomNodeInfo
import com.example.data.model.LogLevel
import com.example.ui.theme.ChromiumAccent
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
import com.example.ui.theme.WarningAmber

@Composable
fun DevToolsDialog(
    devLogs: List<DevToolLog>,
    domNodes: List<DomNodeInfo>,
    jsEvalResult: String?,
    onExecuteJs: (String) -> Unit,
    onRequestDomInspect: () -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var jsCode by remember { mutableStateOf("document.title") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceDark)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCardDark)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = ChromiumPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DualEngine DevTools",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_devtools_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceCardDark,
                    contentColor = ChromiumPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ChromiumPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Console (${devLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onRequestDomInspect()
                        },
                        text = { Text("DOM Tree", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("JS Exec", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Tab Content
                when (selectedTab) {
                    0 -> ConsoleTabContent(
                        logs = devLogs,
                        onClearLogs = onClearLogs
                    )
                    1 -> DomTreeTabContent(
                        nodes = domNodes,
                        onRefresh = onRequestDomInspect
                    )
                    2 -> JsExecTabContent(
                        jsCode = jsCode,
                        onCodeChange = { jsCode = it },
                        jsEvalResult = jsEvalResult,
                        onExecute = { onExecuteJs(jsCode) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConsoleTabContent(
    logs: List<DevToolLog>,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Console Output Log",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            TextButton(onClick = onClearLogs) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Clear", color = DangerRed, fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardDark)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No console output yet. Page console.log messages will appear here.",
                        color = TextTertiaryDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            items(logs) { log ->
                val (color, prefix) = when (log.level) {
                    LogLevel.LOG -> TextPrimaryDark to "[LOG]"
                    LogLevel.INFO -> ChromiumPrimary to "[INFO]"
                    LogLevel.WARN -> WarningAmber to "[WARN]"
                    LogLevel.ERROR -> DangerRed to "[ERR]"
                    LogLevel.DEBUG -> ChromiumAccent to "[DBG]"
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = prefix,
                        color = color,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.message,
                        color = color,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DomTreeTabContent(
    nodes: List<DomNodeInfo>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Extracted Elements (${nodes.size})",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Inspect DOM", tint = ChromiumPrimary, modifier = Modifier.size(16.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardDark)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (nodes.isEmpty()) {
                item {
                    Text(
                        text = "Tap refresh icon to inspect DOM tree of current page.",
                        color = TextTertiaryDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            items(nodes) { node ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "<${node.tagName}>",
                                color = GeckoPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (node.id.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "#${node.id}",
                                    color = ChromiumAccent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (node.className.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ".${node.className}",
                                    color = WarningAmber,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (node.textContent.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${node.textContent}\"",
                                color = TextSecondaryDark,
                                fontSize = 10.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JsExecTabContent(
    jsCode: String,
    onCodeChange: (String) -> Unit,
    jsEvalResult: String?,
    onExecute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            text = "Interactive JavaScript Runner (Runs in Active WebContext)",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Editor Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardDark)
                .padding(10.dp)
        ) {
            BasicTextField(
                value = jsCode,
                onValueChange = onCodeChange,
                textStyle = TextStyle(
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(ChromiumPrimary),
                modifier = Modifier.fillMaxSize().testTag("js_eval_input")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onExecute,
            colors = ButtonDefaults.buttonColors(containerColor = ChromiumPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("js_eval_run_button")
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Execute Expression", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Evaluation Output:", color = TextSecondaryDark, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardDark)
                .padding(10.dp)
        ) {
            Text(
                text = jsEvalResult ?: "undefined",
                color = SuccessGreen,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
