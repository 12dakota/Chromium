package com.example.data.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "about:home",
    val faviconUrl: String? = null,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val engineType: EngineType = EngineType.CHROMIUM_BLINK,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val isReaderMode: Boolean = false,
    val readerContent: String = "",
    val securityLevel: SecurityLevel = SecurityLevel.SECURE,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SecurityLevel {
    SECURE,
    INSECURE,
    LOCAL_PAGE
}
