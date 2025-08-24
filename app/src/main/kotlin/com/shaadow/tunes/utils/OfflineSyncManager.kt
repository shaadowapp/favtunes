package com.shaadow.tunes.utils

import android.content.Context
import android.util.Log
import com.shaadow.tunes.models.BugReport
import com.shaadow.tunes.models.UserFeedback
import com.shaadow.tunes.repository.BugReportRepository
import com.shaadow.tunes.repository.RepositoryProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlin.math.min
import kotlin.math.pow

class OfflineSyncManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    
    private val repository: BugReportRepository = RepositoryProvider.getBugReportRepository(context)
    private val networkMonitor = NetworkMonitor(context)
    
    private var syncJob: Job? = null
    private var isAutoSyncEnabled = true
    
    companion object {
        private const val TAG = "OfflineSyncManager"
        private const val INITIAL_RETRY_DELAY_MS = 1000L // 1 second
        private const val MAX_RETRY_DELAY_MS = 32000L // 32 seconds
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val BATCH_SIZE = 10
        private const val OLD_REPORTS_CLEANUP_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val OLD_REPORTS_THRESHOLD_MS = 30 * 24 * 60 * 60 * 1000L // 30 days
    }
    
    init {
        startNetworkMonitoring()
        schedulePeriodicCleanup()
    }
    
    /**
     * Queue a bug report for offline submission
     */
    suspend fun queueBugReport(bugReport: BugReport): Result<String> {
        return try {
            Log.d(TAG, "Queuing bug report: ${bugReport.id}")
            repository.submitBugReport(bugReport)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to queue bug report", e)
            Result.failure(e)
        }
    }
    
    /**
     * Queue user feedback for offline submission
     */
    suspend fun queueFeedback(feedback: UserFeedback): Result<String> {
        return try {
            Log.d(TAG, "Queuing feedback: ${feedback.id}")
            repository.submitFeedback(feedback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to queue feedback", e)
            Result.failure(e)
        }
    }
    
    /**
     * Manually trigger sync when online
     */
    suspend fun syncWhenOnline(): Result<Int> {
        return if (networkMonitor.isCurrentlyConnected()) {
            syncPendingReports()
        } else {
            Log.d(TAG, "Device is offline, cannot sync now")
            Result.success(0)
        }
    }
    
    /**
     * Retry failed submissions with exponential backoff
     */
    suspend fun retryFailedSubmissions(): Result<Int> {
        return syncPendingReports()
    }
    
    /**
     * Enable or disable automatic sync when network becomes available
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        isAutoSyncEnabled = enabled
        Log.d(TAG, "Auto sync ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Get the current count of pending reports
     */
    suspend fun getPendingReportsCount(): Int {
        return repository.getPendingReportsCount()
    }
    
    /**
     * Check if there are pending reports waiting to be synced
     */
    suspend fun hasPendingReports(): Boolean {
        return repository.hasPendingReports()
    }
    
    /**
     * Start monitoring network connectivity and auto-sync when connected
     */
    private fun startNetworkMonitoring() {
        scope.launch {
            networkMonitor.isConnected
                .filter { it && isAutoSyncEnabled } // Only proceed when connected and auto-sync is enabled
                .collectLatest { isConnected ->
                    if (isConnected) {
                        Log.d(TAG, "Network connected, checking for pending reports")
                        delay(2000) // Wait a bit for network to stabilize
                        
                        if (repository.hasPendingReports()) {
                            Log.d(TAG, "Found pending reports, starting sync")
                            syncWithRetry()
                        }
                    }
                }
        }
    }
    
    /**
     * Sync pending reports with exponential backoff retry logic
     */
    private suspend fun syncWithRetry() {
        syncJob?.cancel()
        syncJob = scope.launch {
            var attempt = 0
            var delay = INITIAL_RETRY_DELAY_MS
            
            while (attempt < MAX_RETRY_ATTEMPTS && isActive) {
                try {
                    val result = syncPendingReports()
                    if (result.isSuccess) {
                        val syncedCount = result.getOrDefault(0)
                        if (syncedCount > 0) {
                            Log.d(TAG, "Successfully synced $syncedCount reports")
                        }
                        break // Success, exit retry loop
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Unknown sync error")
                    }
                } catch (e: Exception) {
                    attempt++
                    Log.w(TAG, "Sync attempt $attempt failed", e)
                    
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        Log.d(TAG, "Retrying sync in ${delay}ms")
                        delay(delay)
                        delay = min(delay * 2, MAX_RETRY_DELAY_MS) // Exponential backoff with cap
                    } else {
                        Log.e(TAG, "Max retry attempts reached, giving up sync")
                    }
                }
            }
        }
    }
    
    /**
     * Perform the actual sync operation
     */
    private suspend fun syncPendingReports(): Result<Int> {
        return try {
            if (!networkMonitor.isCurrentlyConnected()) {
                return Result.failure(Exception("No network connection available"))
            }
            
            Log.d(TAG, "Starting sync of pending reports")
            val result = repository.syncPendingReports()
            
            if (result.isSuccess) {
                val syncedCount = result.getOrDefault(0)
                Log.d(TAG, "Sync completed successfully, synced $syncedCount items")
            } else {
                Log.e(TAG, "Sync failed", result.exceptionOrNull())
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Schedule periodic cleanup of old pending reports
     */
    private fun schedulePeriodicCleanup() {
        scope.launch {
            while (isActive) {
                try {
                    delay(OLD_REPORTS_CLEANUP_INTERVAL_MS)
                    
                    val deletedCount = repository.clearOldPendingReports(OLD_REPORTS_THRESHOLD_MS)
                    if (deletedCount > 0) {
                        Log.d(TAG, "Cleaned up $deletedCount old pending reports")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during periodic cleanup", e)
                }
            }
        }
    }
    
    /**
     * Get sync status information
     */
    suspend fun getSyncStatus(): SyncStatus {
        val pendingCount = repository.getPendingReportsCount()
        val isConnected = networkMonitor.isCurrentlyConnected()
        val isSyncing = syncJob?.isActive == true
        
        return SyncStatus(
            pendingReportsCount = pendingCount,
            isConnected = isConnected,
            isSyncing = isSyncing,
            isAutoSyncEnabled = isAutoSyncEnabled,
            networkType = networkMonitor.getCurrentNetworkType()
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        syncJob?.cancel()
        scope.cancel()
        Log.d(TAG, "OfflineSyncManager cleaned up")
    }
}

/**
 * Data class representing the current sync status
 */
data class SyncStatus(
    val pendingReportsCount: Int,
    val isConnected: Boolean,
    val isSyncing: Boolean,
    val isAutoSyncEnabled: Boolean,
    val networkType: String
)