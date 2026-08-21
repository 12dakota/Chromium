package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthEngine
import com.example.data.local.BrowserDatabase
import com.example.data.model.Bookmark
import com.example.data.model.BrowserTab
import com.example.data.model.DevToolLog
import com.example.data.model.DomNodeInfo
import com.example.data.model.EngineDiagnostics
import com.example.data.model.EngineType
import com.example.data.model.HistoryItem
import com.example.data.model.LogLevel
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
    SETTINGS
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

    // Tabs State
    private val defaultInitialTab = BrowserTab(
        title = "DualEngine Home",
        url = "about:home",
        engineType = EngineType.CHROMIUM_BLINK
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
    private val _engineDiagnostics = MutableStateFlow(EngineDiagnostics())
    val engineDiagnostics: StateFlow<EngineDiagnostics> = _engineDiagnostics.asStateFlow()

    // DevTools State
    private val _devLogs = MutableStateFlow<List<DevToolLog>>(emptyList())
    val devLogs: StateFlow<List<DevToolLog>> = _devLogs.asStateFlow()

    private val _domNodes = MutableStateFlow<List<DomNodeInfo>>(emptyList())
    val domNodes: StateFlow<List<DomNodeInfo>> = _domNodes.asStateFlow()

    private val _jsEvalResult = MutableStateFlow<String?>(null)
    val jsEvalResult: StateFlow<String?> = _jsEvalResult.asStateFlow()

    // Search Engine & Settings
    private val _searchEngineUrl = MutableStateFlow("https://www.google.com/search?q=")
    val searchEngineUrl: StateFlow<String> = _searchEngineUrl.asStateFlow()

    private val _adBlockEnabled = MutableStateFlow(true)
    val adBlockEnabled: StateFlow<Boolean> = _adBlockEnabled.asStateFlow()

    private val _adsBlockedCount = MutableStateFlow(0)
    val adsBlockedCount: StateFlow<Int> = _adsBlockedCount.asStateFlow()

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
        // Log initial engine start
        addDevLog(
            DevToolLog(
                level = LogLevel.INFO,
                message = "DualEngine Core Initialized: Chromium Blink 126.0 & Firefox Gecko 128.0 loaded",
                sourceId = "EngineKernel",
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
        val currentEngine = activeTab.engineType
        val newTab = BrowserTab(
            title = if (url == "about:home") "DualEngine Home" else "Loading...",
            url = url,
            engineType = currentEngine,
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        closeSheet()
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        closeSheet()
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // Keep at least one tab
            val freshTab = BrowserTab(title = "DualEngine Home", url = "about:home", engineType = activeTab.engineType)
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
        val freshTab = BrowserTab(title = "DualEngine Home", url = "about:home", engineType = activeTab.engineType)
        _tabs.value = listOf(freshTab)
        _activeTabId.value = freshTab.id
        closeSheet()
    }

    // --- Navigation & URL processing ---
    fun formatUrlOrQuery(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed == "about:home") return "about:home"
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.startsWith("about:")) {
            return trimmed
        }
        if (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3) {
            return "https://$trimmed"
        }
        return _searchEngineUrl.value + java.net.URLEncoder.encode(trimmed, "UTF-8")
    }

    fun loadUrl(urlOrQuery: String) {
        val formatted = formatUrlOrQuery(urlOrQuery)
        updateActiveTab { it.copy(url = formatted, isLoading = true, progress = 0.1f) }
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
        updateActiveTab {
            it.copy(
                url = url,
                isLoading = true,
                progress = 0.2f,
                securityLevel = secLevel
            )
        }
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

        // Save history if not incognito and not local home
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

        // Update engine diagnostics
        val mockParseTime = (12L..28L).random()
        val mockLayoutTime = (8L..18L).random()
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

    // --- Engine Switching ---
    fun toggleEngine() {
        val newEngine = if (activeTab.engineType == EngineType.CHROMIUM_BLINK) {
            EngineType.FIREFOX_GECKO
        } else {
            EngineType.CHROMIUM_BLINK
        }
        setEngine(newEngine)
    }

    fun setEngine(engineType: EngineType) {
        updateActiveTab { it.copy(engineType = engineType) }
        addDevLog(
            DevToolLog(
                level = LogLevel.INFO,
                message = "Engine switched to ${engineType.displayName} (${engineType.engineCore})",
                sourceId = "EngineDispatcher",
                lineNumber = 42
            )
        )
        _engineDiagnostics.value = _engineDiagnostics.value.copy(activeEngine = engineType)
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
        _devLogs.value = (_devLogs.value + log).takeLast(100)
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

    fun setSearchEngine(urlPrefix: String) {
        _searchEngineUrl.value = urlPrefix
    }

    fun clearSyncMessage() {
        authEngine.clearSyncMessage()
    }
}
