package com.shaadow.tunes.models

data class ScreenContext(
    val screenName: String,
    val suggestedCategory: BugCategory,
    val timestamp: Long = System.currentTimeMillis()
)