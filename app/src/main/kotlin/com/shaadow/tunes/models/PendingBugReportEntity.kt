package com.shaadow.tunes.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_bug_reports")
data class PendingBugReportEntity(
    @PrimaryKey val localId: String,
    val reportData: String, // JSON serialized BugReport
    val createdAt: Long,
    val retryCount: Int = 0
)