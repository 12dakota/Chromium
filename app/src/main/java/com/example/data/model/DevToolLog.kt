package com.example.data.model

enum class LogLevel {
    LOG,
    INFO,
    WARN,
    ERROR,
    DEBUG
}

data class DevToolLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: LogLevel = LogLevel.LOG,
    val message: String,
    val sourceId: String = "",
    val lineNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class DomNodeInfo(
    val tagName: String,
    val id: String = "",
    val className: String = "",
    val textContent: String = "",
    val childCount: Int = 0,
    val attributes: Map<String, String> = emptyMap()
)
