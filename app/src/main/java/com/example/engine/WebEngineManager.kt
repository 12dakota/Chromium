package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.data.model.DevToolLog
import com.example.data.model.DomNodeInfo
import com.example.data.model.EngineType
import com.example.data.model.LogLevel
import org.json.JSONArray
import org.json.JSONObject

class WebEngineManager(
    private val onLogReceived: (DevToolLog) -> Unit = {},
    private val onDomExtracted: (List<DomNodeInfo>) -> Unit = {},
    private val onReaderContentExtracted: (String) -> Unit = {}
) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebView(webView: WebView, engineType: EngineType, isDesktop: Boolean = false) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }

        applyEngineCharacteristics(webView, engineType, isDesktop)
        attachJavascriptBridges(webView)
    }

    fun applyEngineCharacteristics(webView: WebView, engineType: EngineType, isDesktop: Boolean) {
        val settings = webView.settings
        val userAgent = if (isDesktop) {
            engineType.getDesktopUserAgent()
        } else {
            engineType.defaultUserAgent
        }
        settings.userAgentString = userAgent

        when (engineType) {
            EngineType.CHROMIUM_BLINK -> {
                // Chromium Blink specific configurations
                settings.standardFontFamily = "sans-serif"
                settings.serifFontFamily = "serif"
                settings.fixedFontFamily = "monospace"
                settings.minimumFontSize = 8
                settings.textZoom = 100
                injectChromiumBlinkPolyfills(webView)
            }
            EngineType.FIREFOX_GECKO -> {
                // Firefox Gecko / Quantum specific configurations
                settings.standardFontFamily = "Roboto, Arial, sans-serif"
                settings.serifFontFamily = "Charis SIL, Georgia, serif"
                settings.fixedFontFamily = "Fira Code, monospace"
                settings.minimumFontSize = 9
                settings.textZoom = 100
                injectGeckoQuantumPolyfills(webView)
            }
        }
    }

    private fun attachJavascriptBridges(webView: WebView) {
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onConsoleLog(levelStr: String, message: String, source: String, line: Int) {
                val level = when (levelStr.uppercase()) {
                    "INFO" -> LogLevel.INFO
                    "WARN" -> LogLevel.WARN
                    "ERROR" -> LogLevel.ERROR
                    "DEBUG" -> LogLevel.DEBUG
                    else -> LogLevel.LOG
                }
                onLogReceived(
                    DevToolLog(
                        level = level,
                        message = message,
                        sourceId = source,
                        lineNumber = line,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            @JavascriptInterface
            fun onDomInspectionResult(jsonString: String) {
                try {
                    val jsonArray = JSONArray(jsonString)
                    val list = mutableListOf<DomNodeInfo>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            DomNodeInfo(
                                tagName = obj.optString("tag", "div"),
                                id = obj.optString("id", ""),
                                className = obj.optString("class", ""),
                                textContent = obj.optString("text", "").take(120),
                                childCount = obj.optInt("children", 0)
                            )
                        )
                    }
                    onDomExtracted(list)
                } catch (_: Exception) {}
            }

            @JavascriptInterface
            fun onReaderModeExtracted(htmlContent: String) {
                onReaderContentExtracted(htmlContent)
            }
        }, "DualEngineBridge")
    }

    private fun injectChromiumBlinkPolyfills(webView: WebView) {
        val js = """
            (function() {
                try {
                    window.__DUAL_ENGINE__ = { name: "Chromium Blink Core", version: "126.0", jsEngine: "V8" };
                    if (!window.chrome) {
                        window.chrome = { runtime: {}, app: {}, csi: function(){ return {}; } };
                    }
                } catch(e){}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun injectGeckoQuantumPolyfills(webView: WebView) {
        val js = """
            (function() {
                try {
                    window.__DUAL_ENGINE__ = { name: "Mozilla Gecko Engine", version: "128.0", jsEngine: "SpiderMonkey" };
                    if (!window.InstallTrigger) {
                        window.InstallTrigger = {};
                    }
                    // Inject CSS Quantum rendering quirks & font subpixel hints
                    const style = document.createElement('style');
                    style.id = 'dual-gecko-quirks';
                    style.innerHTML = `
                        * { -moz-osx-font-smoothing: grayscale; text-rendering: optimizeLegibility; }
                        ::-moz-selection { background: #FF7A00; color: #ffffff; }
                    `;
                    if (!document.getElementById('dual-gecko-quirks') && document.head) {
                        document.head.appendChild(style);
                    }
                } catch(e){}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun requestDomInspection(webView: WebView) {
        val js = """
            (function() {
                try {
                    const nodes = [];
                    const all = document.querySelectorAll('body, header, nav, main, article, section, h1, h2, h3, p, a, button, img, input, footer');
                    const limit = Math.min(all.length, 35);
                    for (let i = 0; i < limit; i++) {
                        const el = all[i];
                        nodes.push({
                            tag: el.tagName.toLowerCase(),
                            id: el.id || '',
                            class: (typeof el.className === 'string' ? el.className.split(' ').slice(0, 3).join(' ') : ''),
                            text: (el.innerText || el.textContent || '').trim().replace(/\s+/g, ' ').slice(0, 100),
                            children: el.children.length
                        });
                    }
                    DualEngineBridge.onDomInspectionResult(JSON.stringify(nodes));
                } catch(e) {
                    console.error("DOM inspection error", e);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun extractReaderMode(webView: WebView) {
        val js = """
            (function() {
                try {
                    let article = document.querySelector('article') || document.querySelector('main') || document.querySelector('.content') || document.body;
                    let title = document.title || 'Reader View';
                    let h1 = document.querySelector('h1');
                    let headline = h1 ? h1.innerText : title;
                    
                    let paragraphs = [];
                    let pNodes = article.querySelectorAll('p, h2, h3, blockquote');
                    pNodes.forEach(p => {
                        let text = p.innerText.trim();
                        if (text.length > 20) {
                            if (p.tagName === 'H2') {
                                paragraphs.push('<h2>' + text + '</h2>');
                            } else if (p.tagName === 'H3') {
                                paragraphs.push('<h3>' + text + '</h3>');
                            } else if (p.tagName === 'BLOCKQUOTE') {
                                paragraphs.push('<blockquote>' + text + '</blockquote>');
                            } else {
                                paragraphs.push('<p>' + text + '</p>');
                            }
                        }
                    });
                    
                    if (paragraphs.length === 0) {
                        paragraphs.push('<p>' + (article.innerText || 'No text could be extracted for Reader Mode.').replace(/\n\n+/g, '</p><p>') + '</p>');
                    }
                    
                    let result = '<h1>' + headline + '</h1>' + paragraphs.join('');
                    DualEngineBridge.onReaderModeExtracted(result);
                } catch(e) {
                    DualEngineBridge.onReaderModeExtracted('<p>Error rendering reader view: ' + e.message + '</p>');
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun executeCustomScript(webView: WebView, script: String, onResult: (String) -> Unit) {
        webView.evaluateJavascript(script) { result ->
            onResult(result ?: "undefined")
        }
    }
}
