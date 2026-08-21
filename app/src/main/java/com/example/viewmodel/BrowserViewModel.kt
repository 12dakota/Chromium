package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthEngine
import com.example.data.local.BrowserDatabase
import com.example.data.model.AutoEngineDetector
import com.example.data.model.Bookmark
import com.example.data.model.BrowserTab
import com.example.data.model.DevToolLog
import com.example.data.model.DomNodeInfo
import com.example.data.model.DownloadItem
import com.example.data.model.EngineDiagnostics
import com.example.data.model.EngineSelectionMode
import com.example.data.model.EngineType
import com.example.data.model.HistoryItem
import com.example.data.model.LogLevel
import com.example.data.model.SearchEngine
import com.example.data.model.SecurityLevel
import com.example.data.model.UserProfile
import com.example.engine.AdBlockFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ActiveSheet {
    NONE,
    TAB_MANAGER,
    BOOKMARKS_HISTORY,
    ENGINE_INSPECTOR,
    DEV_TOOLS,
    AUTH_CLOUD,
    SETTINGS,
    DOWNLOADS
}

data class SpeedDialItem(
    val title: String,
    val url: String,
    val iconCategory: String,
    val engineNote: String
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BrowserDatabase.getInstance(application)
    private val bookmarkDao = database.bookmarkDao()
    private val historyDao = database.historyDao()
    val authEngine = FirebaseAuthEngine(application)

    val currentUser: StateFlow<UserProfile> = authEngine.currentUser
    val isSyncing: StateFlow<Boolean> = authEngine.isSyncing
    val syncMessage: StateFlow<String?> = authEngine.syncMessage

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryItem>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Engine Selection Mode (Global default)
    private val _engineSelectionMode = MutableStateFlow(EngineSelectionMode.AUTO_DETECT)
    val engineSelectionMode: StateFlow<EngineSelectionMode> = _engineSelectionMode.asStateFlow()

    // User-Defined Custom Domain Rules (e.g., "mycorp.com" -> FIREFOX_GECKO)
    private val _customDomainRules = MutableStateFlow<Map<String, EngineType>>(
        mapOf(
            "developer.android.com" to EngineType.FIREFOX_GECKO,
            "webglsamples.org" to EngineType.CHROMIUM_BLINK
        )
    )
    val customDomainRules: StateFlow<Map<String, EngineType>> = _customDomainRules.asStateFlow()

    // Tabs State
    private val defaultInitialTab = BrowserTab(
        title = "DualEngine Home",
        url = "about:home",
        engineType = EngineType.CHROMIUM_BLINK,
        engineSelectionMode = EngineSelectionMode.AUTO_DETECT,
        engineDetectionReason = "Auto-Detect Dashboard"
    )

    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(defaultInitialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(defaultInitialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: BrowserTab
        get() = _tabs.value.find { it.id == _activeTabId.value } ?: _tabs.value.firstOrNull() ?: defaultInitialTab

    // UI Sheets State
    private val _activeSheet = MutableStateFlow<ActiveSheet>(ActiveSheet.NONE)
    val activeSheet: StateFlow<ActiveSheet> = _activeSheet.asStateFlow()

    // Engine Diagnostics
    private val _engineDiagnostics = MutableStateFlow(
        EngineDiagnostics(
            activeEngine = EngineType.CHROMIUM_BLINK,
            selectionMode = EngineSelectionMode.AUTO_DETECT,
            autoDetectionReason = "Default Home Dashboard"
        )
    )
    val engineDiagnostics: StateFlow<EngineDiagnostics> = _engineDiagnostics.asStateFlow()

    // DevTools State
    private val _devLogs = MutableStateFlow<List<DevToolLog>>(emptyList())
    val devLogs: StateFlow<List<DevToolLog>> = _devLogs.asStateFlow()

    private val _domNodes = MutableStateFlow<List<DomNodeInfo>>(emptyList())
    val domNodes: StateFlow<List<DomNodeInfo>> = _domNodes.asStateFlow()

    private val _jsEvalResult = MutableStateFlow<String?>(null)
    val jsEvalResult: StateFlow<String?> = _jsEvalResult.asStateFlow()

    // Search Engines
    val availableSearchEngines = listOf(
        SearchEngine("google", "Google", "https://www.google.com/search?q=", "google"),
        SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=", "privacy"),
        SearchEngine("bing", "Microsoft Bing", "https://www.bing.com/search?q=", "search"),
        SearchEngine("brave", "Brave Search", "https://search.brave.com/search?q=", "shield"),
        SearchEngine("ecosia", "Ecosia", "https://www.ecosia.org/search?q=", "nature"),
        SearchEngine("startpage", "Startpage", "https://www.startpage.com/sp/search?query=", "security")
    )

    private val _selectedSearchEngine = MutableStateFlow(availableSearchEngines[0])
    val selectedSearchEngine: StateFlow<SearchEngine> = _selectedSearchEngine.asStateFlow()

    // Settings States
    private val _adBlockEnabled = MutableStateFlow(true)
    val adBlockEnabled: StateFlow<Boolean> = _adBlockEnabled.asStateFlow()

    private val _adsBlockedCount = MutableStateFlow(0)
    val adsBlockedCount: StateFlow<Int> = _adsBlockedCount.asStateFlow()

    private val _javascriptEnabled = MutableStateFlow(true)
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled.asStateFlow()

    private val _desktopByDefault = MutableStateFlow(false)
    val desktopByDefault: StateFlow<Boolean> = _desktopByDefault.asStateFlow()

    private val _doNotTrack = MutableStateFlow(true)
    val doNotTrack: StateFlow<Boolean> = _doNotTrack.asStateFlow()

    private val _forceHttps = MutableStateFlow(true)
    val forceHttps: StateFlow<Boolean> = _forceHttps.asStateFlow()

    private val _blockThirdPartyCookies = MutableStateFlow(true)
    val blockThirdPartyCookies: StateFlow<Boolean> = _blockThirdPartyCookies.asStateFlow()

    private val _customUserAgent = MutableStateFlow<String?>(null)
    val customUserAgent: StateFlow<String?> = _customUserAgent.asStateFlow()

    // Sync Preferences
    private val _syncBookmarks = MutableStateFlow(true)
    val syncBookmarks: StateFlow<Boolean> = _syncBookmarks.asStateFlow()

    private val _syncHistory = MutableStateFlow(true)
    val syncHistory: StateFlow<Boolean> = _syncHistory.asStateFlow()

    private val _syncTabs = MutableStateFlow(true)
    val syncTabs: StateFlow<Boolean> = _syncTabs.asStateFlow()

    // Find in Page
    private val _isFindInPageVisible = MutableStateFlow(false)
    val isFindInPageVisible: StateFlow<Boolean> = _isFindInPageVisible.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    // Downloads
    private val _downloads = MutableStateFlow<List<DownloadItem>>(
        listOf(
            DownloadItem(fileName = "DualEngine-v1.0.0.apk", fileUrl = "https://github.com/12dakota/Chromium/releases", fileSize = "22.3 MB", mimeType = "application/vnd.android.package-archive"),
            DownloadItem(fileName = "WebStandards_Cheatsheet.pdf", fileUrl = "https://www.w3.org/standards", fileSize = "1.8 MB", mimeType = "application/pdf")
        )
    )
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    val speedDialShortcuts = listOf(
        SpeedDialItem("Chromium Project", "https://www.chromium.org", "chromium", "Blink/V8 Architecture"),
        SpeedDialItem("Mozilla MDN", "https://developer.mozilla.org", "gecko", "Gecko/Web Standards"),
        SpeedDialItem("GitHub", "https://github.com", "code", "Open Source Repositories"),
        SpeedDialItem("Wikipedia", "https://en.wikipedia.org", "wiki", "Free Encyclopedia"),
        SpeedDialItem("Google Search", "https://www.google.com", "search", "Search Engine"),
        SpeedDialItem("Reddit", "https://www.reddit.com", "community", "Tech Discussions"),
        SpeedDialItem("Hacker News", "https://news.ycombinator.com", "news", "Tech & Developer News"),
        SpeedDialItem("W3C Standards", "https://www.w3.org", "standards", "Web Specifications")
    )

    init {
        addDevLog(
            DevToolLog(
                level = LogLevel.INFO,
                message = "DualEngine Kernel Initialized: Auto-Detect Smart Engine routing active",
                sourceId = "EngineDispatcher",
                lineNumber = 1
            )
        )
    }

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.NONE
    }

    // --- Tab Management ---
    fun createNewTab(url: String = "about:home", isIncognito: Boolean = false) {
        val detection = AutoEngineDetector.detect(url, _customDomainRules.value)
        val initialEngine = when (_engineSelectionMode.value) {
            EngineSelectionMode.ALWAYS_CHROMIUM -> EngineType.CHROMIUM_BLINK
            EngineSelectionMode.ALWAYS_GECKO -> EngineType.FIREFOX_GECKO
            EngineSelectionMode.AUTO_DETECT -> detection.recommendedEngine
        }

        val newTab = BrowserTab(
            title = if (url == "about:home") "DualEngine Home" else "Loading...",
            url = url,
            engineType = initialEngine,
            engineSelectionMode = _engineSelectionMode.value,
            engineDetectionReason = detection.reason,
            isDesktopMode = _desktopByDefault.value,
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        closeSheet()
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        val tab = _tabs.value.find { it.id == tabId }
        if (tab != null) {
            _engineDiagnostics.value = _engineDiagnostics.value.copy(
                activeEngine = tab.engineType,
                selectionMode = tab.engineSelectionMode,
                autoDetectionReason = tab.engineDetectionReason
            )
        }
        closeSheet()
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            val freshTab = BrowserTab(
                title = "DualEngine Home",
                url = "about:home",
                engineType = activeTab.engineType,
                engineSelectionMode = _engineSelectionMode.value
            )
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            return
        }

        val updated = currentList.filter { it.id != tabId }
        _tabs.value = updated

        if (_activeTabId.value == tabId) {
            _activeTabId.value = updated.last().id
        }
    }

    fun closeAllTabs() {
        val freshTab = BrowserTab(
            title = "DualEngine Home",
            url = "about:home",
            engineType = EngineType.CHROMIUM_BLINK,
            engineSelectionMode = _engineSelectionMode.value
        )
        _tabs.value = listOf(freshTab)
        _activeTabId.value = freshTab.id
        closeSheet()
    }

    // --- Navigation & Auto Engine Resolution ---
    fun formatUrlOrQuery(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed == "about:home") return "about:home"
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.startsWith("about:")) {
            return trimmed
        }
        if (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3) {
            return if (_forceHttps.value) "https://$trimmed" else "http://$trimmed"
        }
        return _selectedSearchEngine.value.queryUrl + java.net.URLEncoder.encode(trimmed, "UTF-8")
    }

    fun loadUrl(urlOrQuery: String) {
        val formatted = formatUrlOrQuery(urlOrQuery)
        val detection = AutoEngineDetector.detect(formatted, _customDomainRules.value)

        val resolvedEngine = when {
            activeTab.isEngineManuallyOverridden -> activeTab.engineType
            _engineSelectionMode.value == EngineSelectionMode.ALWAYS_CHROMIUM -> EngineType.CHROMIUM_BLINK
            _engineSelectionMode.value == EngineSelectionMode.ALWAYS_GECKO -> EngineType.FIREFOX_GECKO
            else -> detection.recommendedEngine
        }

        val reason = if (activeTab.isEngineManuallyOverridden) {
            "Manual Override (${resolvedEngine.shortName})"
        } else {
            detection.reason
        }

        updateActiveTab {
            it.copy(
                url = formatted,
                engineType = resolvedEngine,
                engineDetectionReason = reason,
                isLoading = true,
                progress = 0.15f
            )
        }

        _engineDiagnostics.value = _engineDiagnostics.value.copy(
            activeEngine = resolvedEngine,
            selectionMode = _engineSelectionMode.value,
            autoDetectionReason = reason
        )
    }

    fun updateActiveTab(transform: (BrowserTab) -> BrowserTab) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) transform(tab) else tab
        }
    }

    fun setTabLoading(isLoading: Boolean, progress: Float = 0f) {
        updateActiveTab {
            it.copy(
                isLoading = isLoading,
                progress = progress
            )
        }
    }

    fun onPageStarted(url: String) {
        val secLevel = when {
            url.startsWith("https://") -> SecurityLevel.SECURE
            url.startsWith("http://") -> SecurityLevel.INSECURE
            else -> SecurityLevel.LOCAL_PAGE
        }

        // Check if auto-detect needs updating on redirected URLs
        val detection = AutoEngineDetector.detect(url, _customDomainRules.value)
        val shouldAutoSwitch = _engineSelectionMode.value == EngineSelectionMode.AUTO_DETECT &&
                !activeTab.isEngineManuallyOverridden &&
                detection.recommendedEngine != activeTab.engineType

        val effectiveEngine = if (shouldAutoSwitch) detection.recommendedEngine else activeTab.engineType
        val effectiveReason = if (shouldAutoSwitch) detection.reason else activeTab.engineDetectionReason

        updateActiveTab {
            it.copy(
                url = url,
                engineType = effectiveEngine,
                engineDetectionReason = effectiveReason,
                isLoading = true,
                progress = 0.2f,
                securityLevel = secLevel
            )
        }

        _engineDiagnostics.value = _engineDiagnostics.value.copy(
            activeEngine = effectiveEngine,
            selectionMode = _engineSelectionMode.value,
            autoDetectionReason = effectiveReason,
            isSecure = url.startsWith("https://")
        )
    }

    fun onPageFinished(url: String, title: String?, faviconUrl: String? = null) {
        val tabTitle = if (!title.isNullOrBlank()) title else if (url == "about:home") "DualEngine Home" else url
        val currentEngine = activeTab.engineType
        val isIncognito = activeTab.isIncognito

        updateActiveTab {
            it.copy(
                url = url,
                title = tabTitle,
                faviconUrl = faviconUrl ?: it.faviconUrl,
                isLoading = false,
                progress = 1.0f
            )
        }

        if (!isIncognito && url != "about:home" && !url.startsWith("about:")) {
            viewModelScope.launch {
                historyDao.insertHistory(
                    HistoryItem(
                        title = tabTitle,
                        url = url,
                        faviconUrl = faviconUrl,
                        engineUsed = currentEngine.name,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        val mockParseTime = (10L..25L).random()
        val mockLayoutTime = (6L..16L).random()
        _engineDiagnostics.value = _engineDiagnostics.value.copy(
            activeEngine = currentEngine,
            domParseLatencyMs = mockParseTime,
            layoutPassLatencyMs = mockLayoutTime,
            isSecure = url.startsWith("https://")
        )
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        updateActiveTab {
            it.copy(canGoBack = canGoBack, canGoForward = canGoForward)
        }
    }

    // --- Engine Selection & Switching ---
    fun setEngineSelectionMode(mode: EngineSelectionMode) {
        _engineSelectionMode.value = mode
        if (mode != EngineSelectionMode.AUTO_DETECT) {
            val targetEngine = if (mode == EngineSelectionMode.ALWAYS_CHROMIUM) EngineType.CHROMIUM_BLINK else EngineType.FIREFOX_GECKO
            setEngine(targetEngine, isManualOverride = false)
        } else {
            // Re-evaluate active tab under auto-detect
            val detection = AutoEngineDetector.detect(activeTab.url, _customDomainRules.value)
            setEngine(detection.recommendedEngine, isManualOverride = false)
        }
    }

    fun toggleEngine() {
        val newEngine = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) {
            EngineType.FIREFOX_GECKO
        } else {
            EngineType.CHROMIUM_BLINK
        }
        setEngine(newEngine, isManualOverride = true)
    }

    fun setEngine(engineType: EngineType, isManualOverride: Boolean = true) {
        val reason = if (isManualOverride) "Manual User Selection (${engineType.shortName})" else "Auto-Detected: ${engineType.displayName}"
        updateActiveTab {
            it.copy(
                engineType = engineType,
                isEngineManuallyOverridden = isManualOverride,
                engineDetectionReason = reason
            )
        }
        addDevLog(
            DevToolLog(
                level = LogLevel.INFO,
                message = "Engine switched to ${engineType.displayName} | Mode: $reason",
                sourceId = "EngineDispatcher",
                lineNumber = 42
            )
        )
        _engineDiagnostics.value = _engineDiagnostics.value.copy(
            activeEngine = engineType,
            selectionMode = _engineSelectionMode.value,
            autoDetectionReason = reason
        )
    }

    fun setCustomDomainRule(domain: String, engine: EngineType) {
        val updated = _customDomainRules.value.toMutableMap()
        updated[domain.trim().lowercase()] = engine
        _customDomainRules.value = updated
    }

    fun removeCustomDomainRule(domain: String) {
        val updated = _customDomainRules.value.toMutableMap()
        updated.remove(domain)
        _customDomainRules.value = updated
    }

    fun toggleDesktopMode() {
        updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
    }

    fun toggleReaderMode() {
        updateActiveTab { it.copy(isReaderMode = !it.isReaderMode) }
    }

    fun setReaderContent(content: String) {
        updateActiveTab { it.copy(readerContent = content) }
    }

    // --- Search Engine Selection ---
    fun selectSearchEngine(searchEngine: SearchEngine) {
        _selectedSearchEngine.value = searchEngine
    }

    // --- Settings Toggles ---
    fun setJavascriptEnabled(enabled: Boolean) {
        _javascriptEnabled.value = enabled
    }

    fun setDesktopByDefault(enabled: Boolean) {
        _desktopByDefault.value = enabled
    }

    fun setDoNotTrack(enabled: Boolean) {
        _doNotTrack.value = enabled
    }

    fun setForceHttps(enabled: Boolean) {
        _forceHttps.value = enabled
    }

    fun setBlockThirdPartyCookies(enabled: Boolean) {
        _blockThirdPartyCookies.value = enabled
    }

    fun setCustomUserAgent(ua: String?) {
        _customUserAgent.value = if (ua.isNullOrBlank()) null else ua
    }

    fun setSyncBookmarks(enabled: Boolean) {
        _syncBookmarks.value = enabled
    }

    fun setSyncHistory(enabled: Boolean) {
        _syncHistory.value = enabled
    }

    fun setSyncTabs(enabled: Boolean) {
        _syncTabs.value = enabled
    }

    // --- Clear Browsing Data ---
    fun clearBrowsingData(clearHistory: Boolean, clearBookmarks: Boolean, clearDevLogs: Boolean) {
        viewModelScope.launch {
            if (clearHistory) {
                historyDao.clearAllHistory()
            }
            if (clearBookmarks) {
                bookmarkDao.clearAll()
            }
            if (clearDevLogs) {
                _devLogs.value = emptyList()
            }
            _adsBlockedCount.value = 0
            _engineDiagnostics.value = _engineDiagnostics.value.copy(adBlockedCount = 0, trackersBlockedCount = 0)
        }
    }

    // --- Find in Page ---
    fun showFindInPage() {
        _isFindInPageVisible.value = true
    }

    fun hideFindInPage() {
        _isFindInPageVisible.value = false
        _findQuery.value = ""
    }

    fun setFindQuery(query: String) {
        _findQuery.value = query
    }

    // --- Bookmarks & History ---
    fun toggleBookmark() {
        val currentTab = activeTab
        if (currentTab.url == "about:home" || currentTab.url.isEmpty()) return

        viewModelScope.launch {
            val existing = bookmarkDao.getBookmarkByUrl(currentTab.url)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
            } else {
                bookmarkDao.insertBookmark(
                    Bookmark(
                        title = currentTab.title,
                        url = currentTab.url,
                        faviconUrl = currentTab.faviconUrl,
                        enginePreferred = currentTab.engineType.name
                    )
                )
            }
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch {
            historyDao.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAllHistory()
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            bookmarkDao.clearAll()
        }
    }

    // --- DevTools & Diagnostics ---
    fun addDevLog(log: DevToolLog) {
        _devLogs.value = (_devLogs.value + log).takeLast(120)
    }

    fun clearDevLogs() {
        _devLogs.value = emptyList()
    }

    fun setDomNodes(nodes: List<DomNodeInfo>) {
        _domNodes.value = nodes
        _engineDiagnostics.value = _engineDiagnostics.value.copy(domNodesCount = nodes.size)
    }

    fun setJsEvalResult(result: String) {
        _jsEvalResult.value = result
        addDevLog(
            DevToolLog(
                level = LogLevel.LOG,
                message = "Eval Output: $result",
                sourceId = "DevToolsConsole",
                lineNumber = 0
            )
        )
    }

    fun registerBlockedAd(url: String) {
        _adsBlockedCount.value += 1
        _engineDiagnostics.value = _engineDiagnostics.value.copy(
            adBlockedCount = _adsBlockedCount.value,
            trackersBlockedCount = _adsBlockedCount.value / 2
        )
        addDevLog(
            DevToolLog(
                level = LogLevel.WARN,
                message = "Blocked Ad/Tracker: $url",
                sourceId = "ShieldFilter",
                lineNumber = 0
            )
        )
    }

    fun toggleAdBlock() {
        _adBlockEnabled.value = !_adBlockEnabled.value
    }

    fun addDownload(fileName: String, fileSize: String, fileUrl: String, mimeType: String = "") {
        val item = DownloadItem(fileName = fileName, fileSize = fileSize, fileUrl = fileUrl, mimeType = mimeType)
        _downloads.value = listOf(item) + _downloads.value
        addDevLog(
            DevToolLog(
                level = LogLevel.INFO,
                message = "Download recorded: $fileName ($fileSize)",
                sourceId = "DownloadManager",
                lineNumber = 0
            )
        )
    }

    fun clearSyncMessage() {
        authEngine.clearSyncMessage()
    }
}

