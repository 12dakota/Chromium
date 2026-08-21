package com.example.engine

import android.net.Uri

object AdBlockFilter {
    private val blockedHosts = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adnxs.com",
        "scorecardresearch.com",
        "taboola.com",
        "outbrain.com",
        "criteo.com",
        "criteo.net",
        "adsrvr.org",
        "casalemedia.com",
        "rubiconproject.com",
        "popads.net",
        "adcolony.com",
        "unityads.unity3d.com",
        "vungle.com",
        "applovin.com",
        "chartboost.com",
        "inmobi.com",
        "facebook.com/tr",
        "analytics.google.com",
        "googletagmanager.com",
        "quantserve.com",
        "adsystem.com",
        "smartadserver.com"
    )

    fun isAdOrTracker(url: String): Boolean {
        return try {
            val host = Uri.parse(url).host?.lowercase() ?: return false
            blockedHosts.any { blocked -> host == blocked || host.endsWith(".$blocked") }
        } catch (_: Exception) {
            false
        }
    }
}
