package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DevToolLog
import com.example.data.model.EngineType
import com.example.data.model.LogLevel
import com.example.engine.AdBlockFilter
import com.example.engine.WebEngineManager
import com.example.ui.components.AuthDialog
import com.example.ui.components.BookmarksHistorySheet
import com.example.ui.components.BottomBrowserBar
import com.example.ui.components.DevToolsDialog
import com.example.ui.components.DownloadsSheet
import com.example.ui.components.EngineInspectorSheet
import com.example.ui.components.FindInPageBar
import com.example.ui.components.Omnibox
import com.example.ui.components.ReaderView
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SpeedDialHome
import com.example.ui.components.TabManagerSheet
import com.example.ui.theme.SurfaceDark
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserApp(
    viewModel: BrowserViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = viewModel.activeTab
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val engineDiagnostics by viewModel.engineDiagnostics.collectAsStateWithLifecycle()
    val devLogs by viewModel.devLogs.collectAsStateWithLifecycle()
    val domNodes by viewModel.domNodes.collectAsStateWithLifecycle()
    val jsEvalResult by viewModel.jsEvalResult.collectAsStateWithLifecycle()
    val adBlockEnabled by viewModel.adBlockEnabled.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val isFindInPageVisible by viewModel.isFindInPageVisible.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Sheet states
    val tabSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val engineSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bookmarksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val downloadsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentWebView by remember { mutableStateOf<WebView?>(null) }

    val engineManager = remember {
        WebEngineManager(
            onLogReceived = { log -> viewModel.addDevLog(log) },
            onDomExtracted = { nodes -> viewModel.setDomNodes(nodes) },
            onReaderContentExtracted = { content -> viewModel.setReaderContent(content) }
        )
    }

    // Sync message toast
    LaunchedEffect(syncMessage) {
        if (!syncMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(syncMessage ?: "")
            viewModel.clearSyncMessage()
        }
    }

    // Back handling
    BackHandler(enabled = true) {
        if (isFindInPageVisible) {
            viewModel.hideFindInPage()
            currentWebView?.clearMatches()
        } else if (activeSheet != ActiveSheet.NONE) {
            viewModel.closeSheet()
        } else if (activeTab.isReaderMode) {
            viewModel.toggleReaderMode()
        } else if (currentWebView != null && currentWebView?.canGoBack() == true) {
            currentWebView?.goBack()
        } else if (activeTab.url != "about:home") {
            viewModel.loadUrl("about:home")
        }
    }

    val isBookmarked = bookmarks.any { it.url == activeTab.url }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("browser_main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Omnibox(
                    activeTab = activeTab,
                    tabsCount = tabs.size,
                    onNavigate = { url -> viewModel.loadUrl(url) },
                    onReload = { currentWebView?.reload() },
                    onOpenEngineInspector = { viewModel.openSheet(ActiveSheet.ENGINE_INSPECTOR) },
                    onOpenTabs = { viewModel.openSheet(ActiveSheet.TAB_MANAGER) },
                    onOpenDevTools = { viewModel.openSheet(ActiveSheet.DEV_TOOLS) }
                )

                if (isFindInPageVisible) {
                    FindInPageBar(
                        query = findQuery,
                        onQueryChange = { q ->
                            viewModel.setFindQuery(q)
                            if (q.isNotBlank()) {
                                currentWebView?.findAllAsync(q)
                            } else {
                                currentWebView?.clearMatches()
                            }
                        },
                        onNext = { currentWebView?.findNext(true) },
                        onPrevious = { currentWebView?.findNext(false) },
                        onClose = {
                            viewModel.hideFindInPage()
                            currentWebView?.clearMatches()
                        }
                    )
                }
            }
        },
        bottomBar = {
            BottomBrowserBar(
                activeTab = activeTab,
                isBookmarked = isBookmarked,
                onGoBack = {
                    if (currentWebView?.canGoBack() == true) {
                        currentWebView?.goBack()
                    } else if (activeTab.url != "about:home") {
                        viewModel.loadUrl("about:home")
                    }
                },
                onGoForward = {
                    if (currentWebView?.canGoForward() == true) {
                        currentWebView?.goForward()
                    }
                },
                onGoHome = { viewModel.loadUrl("about:home") },
                onToggleEngine = {
                    viewModel.toggleEngine()
                    currentWebView?.let {
                        engineManager.switchEngineAndReload(it, viewModel.activeTab.engineType, viewModel.activeTab.isDesktopMode)
                    }
                },
                onToggleBookmark = { viewModel.toggleBookmark() },
                onOpenBookmarksHistory = { viewModel.openSheet(ActiveSheet.BOOKMARKS_HISTORY) },
                onOpenDevTools = { viewModel.openSheet(ActiveSheet.DEV_TOOLS) },
                onOpenEngineInspector = { viewModel.openSheet(ActiveSheet.ENGINE_INSPECTOR) },
                onOpenAuthCloud = { viewModel.openSheet(ActiveSheet.AUTH_CLOUD) },
                onOpenSettings = { viewModel.openSheet(ActiveSheet.SETTINGS) },
                onOpenDownloads = { viewModel.openSheet(ActiveSheet.DOWNLOADS) },
                onFindInPage = { viewModel.showFindInPage() },
                onToggleDesktop = {
                    viewModel.toggleDesktopMode()
                    currentWebView?.let {
                        engineManager.applyEngineCharacteristics(it, viewModel.activeTab.engineType, viewModel.activeTab.isDesktopMode)
                        it.reload()
                    }
                },
                onToggleReader = {
                    viewModel.toggleReaderMode()
                    if (viewModel.activeTab.isReaderMode && currentWebView != null) {
                        engineManager.extractReaderMode(currentWebView!!)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceDark)
        ) {
            if (activeTab.url == "about:home") {
                SpeedDialHome(
                    activeEngine = activeTab.engineType,
                    userProfile = userProfile,
                    isSyncing = isSyncing,
                    syncMessage = syncMessage,
                    engineDiagnostics = engineDiagnostics,
                    speedDialShortcuts = viewModel.speedDialShortcuts,
                    recentHistory = history,
                    onNavigate = { url -> viewModel.loadUrl(url) },
                    onToggleEngine = {
                        viewModel.toggleEngine()
                    },
                    onOpenEngineInspector = { viewModel.openSheet(ActiveSheet.ENGINE_INSPECTOR) },
                    onOpenDevTools = { viewModel.openSheet(ActiveSheet.DEV_TOOLS) },
                    onOpenAuthCloud = { viewModel.openSheet(ActiveSheet.AUTH_CLOUD) },
                    onTriggerSync = {
                        coroutineScope.launch {
                            viewModel.authEngine.triggerCloudSync()
                        }
                    }
                )
            } else if (activeTab.isReaderMode) {
                ReaderView(
                    activeTab = activeTab,
                    onExitReaderMode = { viewModel.toggleReaderMode() }
                )
            } else {
                // Active Web Rendering Canvas
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            engineManager.configureWebView(this, activeTab.engineType, activeTab.isDesktopMode)

                            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                                val sizeStr = if (contentLength > 0) {
                                    val mb = contentLength / (1024.0 * 1024.0)
                                    String.format("%.1f MB", mb)
                                } else "1.2 MB"
                                viewModel.addDownload(fileName, sizeStr, url)
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    viewModel.setTabLoading(newProgress < 100, newProgress / 100f)
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    if (view?.url != null) {
                                        viewModel.onPageFinished(view.url ?: "", title)
                                    }
                                }

                                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                                    // Icon updated
                                }

                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    if (consoleMessage != null) {
                                        val level = when (consoleMessage.messageLevel()) {
                                            ConsoleMessage.MessageLevel.ERROR -> LogLevel.ERROR
                                            ConsoleMessage.MessageLevel.WARNING -> LogLevel.WARN
                                            ConsoleMessage.MessageLevel.LOG -> LogLevel.LOG
                                            ConsoleMessage.MessageLevel.TIP -> LogLevel.INFO
                                            ConsoleMessage.MessageLevel.DEBUG -> LogLevel.DEBUG
                                            else -> LogLevel.LOG
                                        }
                                        viewModel.addDevLog(
                                            DevToolLog(
                                                level = level,
                                                message = consoleMessage.message() ?: "",
                                                sourceId = consoleMessage.sourceId() ?: "",
                                                lineNumber = consoleMessage.lineNumber()
                                            )
                                        )
                                    }
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val requestUrl = request?.url?.toString() ?: return false
                                    if (adBlockEnabled && AdBlockFilter.isAdOrTracker(requestUrl)) {
                                        viewModel.registerBlockedAd(requestUrl)
                                        return true
                                    }
                                    return false
                                }

                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    val requestUrl = request?.url?.toString()
                                    if (adBlockEnabled && requestUrl != null && AdBlockFilter.isAdOrTracker(requestUrl)) {
                                        viewModel.registerBlockedAd(requestUrl)
                                        // Return empty data to block request
                                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    if (url != null) {
                                        viewModel.onPageStarted(url)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (url != null) {
                                        viewModel.onPageFinished(url, view?.title)
                                        viewModel.updateNavigationState(view?.canGoBack() == true, view?.canGoForward() == true)
                                        // Inject polyfills
                                        engineManager.applyEngineCharacteristics(view!!, activeTab.engineType, activeTab.isDesktopMode)
                                    }
                                }

                                @SuppressLint("WebViewClientOnReceivedSslError")
                                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                    // Proceed gracefully or record
                                    handler?.proceed()
                                }
                            }

                            loadUrl(activeTab.url)
                            currentWebView = this
                        }
                    },
                    update = { webView ->
                        currentWebView = webView
                        if (webView.url != activeTab.url && activeTab.url != "about:home") {
                            engineManager.applyEngineCharacteristics(webView, activeTab.engineType, activeTab.isDesktopMode)
                            webView.loadUrl(activeTab.url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // --- Bottom Sheets & Modals ---
        if (activeSheet == ActiveSheet.TAB_MANAGER) {
            TabManagerSheet(
                tabs = tabs,
                activeTabId = activeTabId,
                sheetState = tabSheetState,
                onSelectTab = { id -> viewModel.selectTab(id) },
                onCloseTab = { id -> viewModel.closeTab(id) },
                onNewTab = { viewModel.createNewTab() },
                onCloseAll = { viewModel.closeAllTabs() },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        if (activeSheet == ActiveSheet.ENGINE_INSPECTOR) {
            EngineInspectorSheet(
                activeTab = activeTab,
                diagnostics = engineDiagnostics,
                sheetState = engineSheetState,
                onSetEngine = { engineType ->
                    viewModel.setEngine(engineType)
                    currentWebView?.let {
                        engineManager.switchEngineAndReload(it, engineType, activeTab.isDesktopMode)
                    }
                },
                onToggleDesktop = {
                    viewModel.toggleDesktopMode()
                    currentWebView?.let {
                        engineManager.applyEngineCharacteristics(it, activeTab.engineType, activeTab.isDesktopMode)
                        it.reload()
                    }
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        if (activeSheet == ActiveSheet.BOOKMARKS_HISTORY) {
            BookmarksHistorySheet(
                bookmarks = bookmarks,
                history = history,
                sheetState = bookmarksSheetState,
                onNavigate = { url -> viewModel.loadUrl(url) },
                onDeleteBookmark = { b -> viewModel.deleteBookmark(b) },
                onDeleteHistoryItem = { h -> viewModel.deleteHistoryItem(h) },
                onClearHistory = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        if (activeSheet == ActiveSheet.DEV_TOOLS) {
            DevToolsDialog(
                devLogs = devLogs,
                domNodes = domNodes,
                jsEvalResult = jsEvalResult,
                onExecuteJs = { script ->
                    currentWebView?.let {
                        engineManager.executeCustomScript(it, script) { result ->
                            viewModel.setJsEvalResult(result)
                        }
                    }
                },
                onRequestDomInspect = {
                    currentWebView?.let {
                        engineManager.requestDomInspection(it)
                    }
                },
                onClearLogs = { viewModel.clearDevLogs() },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        if (activeSheet == ActiveSheet.AUTH_CLOUD) {
            AuthDialog(
                authEngine = viewModel.authEngine,
                userProfile = userProfile,
                isSyncing = isSyncing,
                syncMessage = syncMessage,
                onDismiss = { viewModel.closeSheet() }
            )
        }

        if (activeSheet == ActiveSheet.SETTINGS) {
            SettingsSheet(
                viewModel = viewModel,
                sheetState = settingsSheetState,
                onDismiss = { viewModel.closeSheet() },
                onOpenAuthCloud = {
                    viewModel.openSheet(ActiveSheet.AUTH_CLOUD)
                }
            )
        }

        if (activeSheet == ActiveSheet.DOWNLOADS) {
            DownloadsSheet(
                downloads = downloads,
                sheetState = downloadsSheetState,
                onDismiss = { viewModel.closeSheet() }
            )
        }
    }
}

