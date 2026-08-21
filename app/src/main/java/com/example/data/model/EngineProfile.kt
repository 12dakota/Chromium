package com.example.data.model

import java.net.URI

enum class EngineType(
    val displayName: String,
    val shortName: String,
    val engineCore: String,
    val jsEngine: String,
    val renderingPipeline: String,
    val defaultUserAgent: String,
    val vendorPrefix: String
) {
    CHROMIUM_BLINK(
        displayName = "Chromium Blink Core",
        shortName = "Chromium",
        engineCore = "Blink v126.0 (V8)",
        jsEngine = "V8 Engine v12.6.415",
        renderingPipeline = "Chromium Skia / Hardware Rasterizer",
        defaultUserAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36",
        vendorPrefix = "-webkit-"
    ),
    FIREFOX_GECKO(
        displayName = "Firefox Gecko Quantum",
        shortName = "Gecko",
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

enum class EngineSelectionMode(val title: String, val subtitle: String) {
    AUTO_DETECT("Auto-Detect (Smart Routing)", "Automatically selects the best engine based on website architecture, WebGL, and standard compliance"),
    ALWAYS_CHROMIUM("Always Chromium Blink", "Forces Google Chromium Blink & V8 engine for all sites"),
    ALWAYS_GECKO("Always Firefox Gecko", "Forces Mozilla Gecko / Quantum engine & SpiderMonkey for all sites")
}

data class EngineDetectionResult(
    val recommendedEngine: EngineType,
    val reason: String,
    val matchedPattern: String? = null
)

object AutoEngineDetector {
    // Domains specifically optimized for Chromium Blink (e.g. Google PWA, heavy WebGL, video streaming, canvas apps)
    private val CHROMIUM_PATTERNS = listOf(
        "google.", "youtube.", "youtu.be", "gmail.", "maps.google.", "drive.google.",
        "docs.google.", "meet.google.", "spotify.com", "netflix.com", "twitch.tv",
        "figma.com", "canva.com", "webglreport.com", "shadertoy.com", "threejs.org",
        "codepen.io", "jsfiddle.net", "stackblitz.com", "codesandbox.io", "disneyplus.com",
        "primevideo.com", "tiktok.com", "instagram.com", "facebook.com", "meta.com"
    )

    // Domains specifically optimized for Firefox Gecko (e.g. standards, privacy-first, developer documentation, Wikipedia, GitHub, open web)
    private val GECKO_PATTERNS = listOf(
        "mozilla.org", "developer.mozilla.org", "firefox.com", "w3.org", "wikipedia.org",
        "wikimedia.org", "github.com", "gitlab.com", "stackoverflow.com", "stackexchange.com",
        "news.ycombinator.com", "reddit.com", "medium.com", "substack.com", "dev.to",
        "nytimes.com", "theverge.com", "arstechnica.com", "eff.org", "duckduckgo.com",
        "brave.com", "archive.org", "gnu.org", "linux.org", "ietf.org"
    )

    fun detect(url: String, customRules: Map<String, EngineType> = emptyMap()): EngineDetectionResult {
        if (url.isBlank() || url == "about:home" || url.startsWith("about:")) {
            return EngineDetectionResult(EngineType.CHROMIUM_BLINK, "Default Home Dashboard", null)
        }

        val cleanHost = try {
            val uri = URI(if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url)
            uri.host?.lowercase() ?: url.lowercase()
        } catch (_: Exception) {
            url.lowercase()
        }

        // 1. Check custom user-defined domain rules first
        for ((pattern, engine) in customRules) {
            if (cleanHost.contains(pattern.lowercase()) || url.contains(pattern.lowercase())) {
                return EngineDetectionResult(
                    recommendedEngine = engine,
                    reason = "User Defined Rule: $pattern",
                    matchedPattern = pattern
                )
            }
        }

        // 2. Check Gecko patterns (Standards, documentation, typography, privacy)
        for (pattern in GECKO_PATTERNS) {
            if (cleanHost.contains(pattern)) {
                return EngineDetectionResult(
                    recommendedEngine = EngineType.FIREFOX_GECKO,
                    reason = "Optimized for Gecko Web Standards & Privacy ($pattern)",
                    matchedPattern = pattern
                )
            }
        }

        // 3. Check Chromium patterns (PWAs, WebGL, rich media, Google apps)
        for (pattern in CHROMIUM_PATTERNS) {
            if (cleanHost.contains(pattern)) {
                return EngineDetectionResult(
                    recommendedEngine = EngineType.CHROMIUM_BLINK,
                    reason = "Optimized for Blink V8 & Hardware Media ($pattern)",
                    matchedPattern = pattern
                )
            }
        }

        // 4. Heuristics based on URL keywords
        if (cleanHost.contains("docs") || cleanHost.contains("blog") || cleanHost.contains("wiki") || cleanHost.contains("news")) {
            return EngineDetectionResult(
                recommendedEngine = EngineType.FIREFOX_GECKO,
                reason = "Reading & Content-Centric Page",
                matchedPattern = "content_heuristic"
            )
        }

        if (cleanHost.contains("play") || cleanHost.contains("video") || cleanHost.contains("app") || cleanHost.contains("game")) {
            return EngineDetectionResult(
                recommendedEngine = EngineType.CHROMIUM_BLINK,
                reason = "Rich Interactive Application",
                matchedPattern = "app_heuristic"
            )
        }

        // Default fallback to Chromium
        return EngineDetectionResult(
            recommendedEngine = EngineType.CHROMIUM_BLINK,
            reason = "Standard Web Compatibility Mode",
            matchedPattern = "default"
        )
    }
}

data class EngineDiagnostics(
    val activeEngine: EngineType = EngineType.CHROMIUM_BLINK,
    val selectionMode: EngineSelectionMode = EngineSelectionMode.AUTO_DETECT,
    val autoDetectionReason: String = "Smart Auto-Routing",
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

