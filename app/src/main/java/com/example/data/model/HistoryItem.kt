package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val engineUsed: String = "CHROMIUM_BLINK",
    val visitCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
