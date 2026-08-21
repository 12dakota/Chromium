package com.example.data.model

enum class EngineType(
    val displayName: String,
    val engineCore: String,
    val jsEngine: String,
    val renderingPipeline: String,
    val defaultUserAgent: String,
    val vendorPrefix: String
) {
    CHROMIUM_BLINK(
        displayName = "Chromium Engine",
        engineCore = "Blink v126.0 (V8)",
        jsEngine = "V8 Engine v12.6.415",
        renderingPipeline = "Chromium Skia / Hardware Rasterizer",
        defaultUserAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36",
        vendorPrefix = "-webkit-"
    ),
    FIREFOX_GECKO(
        displayName = "Firefox Gecko",
        engineCore = "Gecko / Quantum v128.0",
        jsEngine = "SpiderMonkey JIT 128.0",
        renderingPipeline = "WebRender / Quantum CSS Pipeline",
        defaultUserAgent = "Mozilla/5.0 (Android 14; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0",
        vendorPrefix = "-moz-"
    );

    fun getDesktopUserAgent(): String {
        return when (this) {
            CHROMIUM_BLINK -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
            FIREFOX_GECKO -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
        }
    }
}

data class EngineDiagnostics(
    val activeEngine: EngineType = EngineType.CHROMIUM_BLINK,
    val domNodesCount: Int = 0,
    val domParseLatencyMs: Long = 18,
    val layoutPassLatencyMs: Long = 12,
    val memoryUsageMb: Float = 42.5f,
    val fps: Int = 60,
    val webGlSupported: Boolean = true,
    val serviceWorkerEnabled: Boolean = true,
    val httpVersion: String = "HTTP/2 + TLS 1.3",
    val adBlockedCount: Int = 0,
    val trackersBlockedCount: Int = 0,
    val isSecure: Boolean = true
)
