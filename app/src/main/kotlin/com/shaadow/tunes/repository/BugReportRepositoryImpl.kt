package com.shaadow.tunes.repository

import android.content.Context
import android.util.Log
import com.shaadow.tunes.Database
import com.shaadow.tunes.models.BugReport
import com.shaadow.tunes.models.UserFeedback
import com.shaadow.tunes.models.PendingBugReportEntity
import com.shaadow.tunes.models.PendingFeedbackEntity
import com.shaadow.tunes.utils.FirestoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class BugReportRepositoryImpl(
    private val context: Context
) : BugReportRepository {
    
    private val firestoreHelper = FirestoreHelper
    private val bugReportDao = Database.bugReportDao()
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val TAG = "BugReportRepository"
        private const val MAX_RETRY_COUNT = 5
        private const val OLD_REPORT_THRESHOLD_DAYS = 30
    }
    
    override suspend fun submitBugReport(bugReport: BugReport): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate the bug report
            if (!bugReport.isValid()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid bug report data"))
            }
            
            // Try to submit online first
            if (isNetworkAvailable()) {
                val result = firestoreHelper.submitBugReport(bugReport)
                if (result.isSuccess) {
                    Log.d(TAG, "Bug report submitted successfully online")
                    return@withContext result
                } else {
                    Log.w(TAG, "Failed to submit bug report online, queuing locally", result.exceptionOrNull())
                }
            }
            
            // Queue locally if online submission failed or no network
            val localId = UUID.randomUUID().toString()
            val pendingReport = PendingBugReportEntity(
                localId = localId,
                reportData = json.encodeToString(bugReport),
                createdAt = System.currentTimeMillis(),
                retryCount = 0
            )
            
            bugReportDao.insertPendingBugReport(pendingReport)
            Log.d(TAG, "Bug report queued locally with ID: $localId")
            
            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit bug report", e)
            Result.failure(e)
        }
    }
    
    override suspend fun submitFeedback(feedback: UserFeedback): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate the feedback
            if (!feedback.isValid()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid feedback data"))
            }
            
            // Try to submit online first
            if (isNetworkAvailable()) {
                val result = firestoreHelper.submitUserFeedback(feedback)
                if (result.isSuccess) {
                    Log.d(TAG, "Feedback submitted successfully online")
                    return@withContext result
                } else {
                    Log.w(TAG, "Failed to submit feedback online, queuing locally", result.exceptionOrNull())
                }
            }
            
            // Queue locally if online submission failed or no network
            val localId = UUID.randomUUID().toString()
            val pendingFeedback = PendingFeedbackEntity(
                localId = localId,
                feedbackData = json.encodeToString(feedback),
                createdAt = System.currentTimeMillis(),
                retryCount = 0
            )
            
            bugReportDao.insertPendingFeedback(pendingFeedback)
            Log.d(TAG, "Feedback queued locally with ID: $localId")
            
            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit feedback", e)
            Result.failure(e)
        }
    }
    
    override suspend fun syncPendingReports(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "No network available for sync")
                return@withContext Result.success(0)
            }
            
            var syncedCount = 0
            
            // Sync pending bug reports
            val pendingBugReports = bugReportDao.getAllPendingBugReports()
            for (pendingReport in pendingBugReports) {
                try {
                    if (pendingReport.retryCount >= MAX_RETRY_COUNT) {
                        Log.w(TAG, "Removing bug report after max retries: ${pendingReport.localId}")
                        bugReportDao.deletePendingBugReportById(pendingReport.localId)
                        continue
                    }
                    
                    val bugReport = json.decodeFromString<BugReport>(pendingReport.reportData)
                    val result = firestoreHelper.submitBugReport(bugReport)
                    
                    if (result.isSuccess) {
                        bugReportDao.deletePendingBugReportById(pendingReport.localId)
                        syncedCount++
                        Log.d(TAG, "Synced bug report: ${pendingReport.localId}")
                    } else {
                        // Increment retry count
                        bugReportDao.updateRetryCount(pendingReport.localId, pendingReport.retryCount + 1)
                        Log.w(TAG, "Failed to sync bug report: ${pendingReport.localId}, retry count: ${pendingReport.retryCount + 1}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing bug report: ${pendingReport.localId}", e)
                    bugReportDao.updateRetryCount(pendingReport.localId, pendingReport.retryCount + 1)
                }
            }
            
            // Sync pending feedback
            val pendingFeedback = bugReportDao.getAllPendingFeedback()
            for (pendingFeedbackItem in pendingFeedback) {
                try {
                    if (pendingFeedbackItem.retryCount >= MAX_RETRY_COUNT) {
                        Log.w(TAG, "Removing feedback after max retries: ${pendingFeedbackItem.localId}")
                        bugReportDao.deletePendingFeedbackById(pendingFeedbackItem.localId)
                        continue
                    }
                    
                    val feedback = json.decodeFromString<UserFeedback>(pendingFeedbackItem.feedbackData)
                    val result = firestoreHelper.submitUserFeedback(feedback)
                    
                    if (result.isSuccess) {
                        bugReportDao.deletePendingFeedbackById(pendingFeedbackItem.localId)
                        syncedCount++
                        Log.d(TAG, "Synced feedback: ${pendingFeedbackItem.localId}")
                    } else {
                        // Increment retry count
                        bugReportDao.updateFeedbackRetryCount(pendingFeedbackItem.localId, pendingFeedbackItem.retryCount + 1)
                        Log.w(TAG, "Failed to sync feedback: ${pendingFeedbackItem.localId}, retry count: ${pendingFeedbackItem.retryCount + 1}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing feedback: ${pendingFeedbackItem.localId}", e)
                    bugReportDao.updateFeedbackRetryCount(pendingFeedbackItem.localId, pendingFeedbackItem.retryCount + 1)
                }
            }
            
            Log.d(TAG, "Sync completed. Synced $syncedCount items")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync pending reports", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingReportsCount(): Int = withContext(Dispatchers.IO) {
        try {
            val bugReportsCount = bugReportDao.getPendingBugReportsCount()
            val feedbackCount = bugReportDao.getPendingFeedbackCount()
            bugReportsCount + feedbackCount
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get pending reports count", e)
            0
        }
    }
    
    override suspend fun hasPendingReports(): Boolean {
        return getPendingReportsCount() > 0
    }
    
    override suspend fun clearOldPendingReports(olderThanMillis: Long): Int = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - olderThanMillis
            
            val initialBugReportsCount = bugReportDao.getPendingBugReportsCount()
            val initialFeedbackCount = bugReportDao.getPendingFeedbackCount()
            
            bugReportDao.deleteOldBugReports(cutoffTime)
            bugReportDao.deleteOldFeedback(cutoffTime)
            
            val finalBugReportsCount = bugReportDao.getPendingBugReportsCount()
            val finalFeedbackCount = bugReportDao.getPendingFeedbackCount()
            
            val deletedCount = (initialBugReportsCount - finalBugReportsCount) + (initialFeedbackCount - finalFeedbackCount)
            
            if (deletedCount > 0) {
                Log.d(TAG, "Cleared $deletedCount old pending reports")
            }
            
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear old pending reports", e)
            0
        }
    }
    
    /**
     * Check if network is available for Firebase operations
     * This is a simplified check - in a real implementation you might want to use
     * ConnectivityManager or a more sophisticated network monitoring solution
     */
    private suspend fun isNetworkAvailable(): Boolean {
        return try {
            firestoreHelper.isFirestoreAvailable()
        } catch (e: Exception) {
            Log.w(TAG, "Network availability check failed", e)
            false
        }
    }
}