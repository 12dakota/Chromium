package com.example.data.model

import java.util.UUID

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val fileUrl: String,
    val fileSize: String,
    val mimeType: String = "application/octet-stream",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)

data class SearchEngine(
    val id: String,
    val name: String,
    val queryUrl: String,
    val iconName: String
)
