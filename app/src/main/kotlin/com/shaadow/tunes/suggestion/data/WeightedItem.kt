package com.shaadow.tunes.suggestion.data

data class WeightedItem(
    val id: String,
    val weight: Float,
    val timestamp: Long
)

data class PlayEvent(
    val songId: String,
    val duration: Long,
    val timestamp: Long
)