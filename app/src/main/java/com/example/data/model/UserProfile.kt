package com.example.data.model

data class UserProfile(
    val uid: String = "guest_user",
    val email: String? = null,
    val displayName: String = "Guest Explorer",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = true,
    val syncBookmarks: Boolean = true,
    val syncHistory: Boolean = true,
    val syncTabs: Boolean = true,
    val defaultEngine: EngineType = EngineType.CHROMIUM_BLINK,
    val adBlockEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
