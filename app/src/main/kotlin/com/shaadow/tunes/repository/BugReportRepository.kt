package com.shaadow.tunes.repository

import com.shaadow.tunes.models.BugReport
import com.shaadow.tunes.models.UserFeedback

interface BugReportRepository {
    /**
     * Submit a bug report. Will attempt to submit online first, queue locally if offline.
     * @param bugReport The bug report to submit
     * @return Result containing the submission ID on success or error on failure
     */
    suspend fun submitBugReport(bugReport: BugReport): Result<String>
    
    /**
     * Submit user feedback. Will attempt to submit online first, queue locally if offline.
     * @param feedback The user feedback to submit
     * @return Result containing the submission ID on success or error on failure
     */
    suspend fun submitFeedback(feedback: UserFeedback): Result<String>
    
    /**
     * Sync all pending reports and feedback to Firebase
     * @return Result containing the number of successfully synced items
     */
    suspend fun syncPendingReports(): Result<Int>
    
    /**
     * Get the count of pending reports waiting to be synced
     * @return The total number of pending bug reports and feedback
     */
    suspend fun getPendingReportsCount(): Int
    
    /**
     * Check if there are any pending reports
     * @return true if there are pending reports, false otherwise
     */
    suspend fun hasPendingReports(): Boolean
    
    /**
     * Clear old pending reports that are older than the specified time
     * @param olderThanMillis Time in milliseconds - reports older than this will be deleted
     * @return Number of reports deleted
     */
    suspend fun clearOldPendingReports(olderThanMillis: Long): Int
}