package com.shaadow.tunes.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_feedback")
data class PendingFeedbackEntity(
    @PrimaryKey val localId: String,
    val feedbackData: String, // JSON serialized UserFeedback
    val createdAt: Long,
    val retryCount: Int = 0
)