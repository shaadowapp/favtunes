package com.shaadow.tunes.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

class PerformanceMonitor {
    
    private val operationTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val activeOperations = ConcurrentHashMap<String, Long>()
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        
        // Performance thresholds (in milliseconds)
        private const val SUBMISSION_THRESHOLD = 5000L // 5 seconds
        private const val VALIDATION_THRESHOLD = 100L // 100ms
        private const val SYNC_THRESHOLD = 10000L // 10 seconds
        private const val UI_RESPONSE_THRESHOLD = 16L // 16ms (60fps)
        
        // Singleton instance
        @Volatile
        private var INSTANCE: PerformanceMonitor? = null
        
        fun getInstance(): PerformanceMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerformanceMonitor().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Start monitoring an operation
     */
    fun startOperation(operationName: String): String {
        val operationId = "${operationName}_${System.currentTimeMillis()}_${(1000..9999).random()}"
        activeOperations[operationId] = System.currentTimeMillis()
        
        Log.d(TAG, "Started monitoring operation: $operationId")
        return operationId
    }
    
    /**
     * End monitoring an operation and log performance
     */
    fun endOperation(operationId: String, additionalMetrics: Map<String, Any> = emptyMap()) {
        val startTime = activeOperations.remove(operationId)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            val operationName = operationId.substringBefore("_")
            
            // Store timing data
            operationTimes.getOrPut(operationName) { mutableListOf() }.add(duration)
            
            // Check against thresholds
            val threshold = getThresholdForOperation(operationName)
            if (duration > threshold) {
                BugReportLogger.logPerformanceIssue(
                    operation = operationName,
                    duration = duration,
                    threshold = threshold,
                    additionalMetrics = additionalMetrics + mapOf("operation_id" to operationId)
                )
            } else {
                Log.d(TAG, "Operation completed: $operationName (${duration}ms)")
            }
            
            // Clean up old data periodically
            cleanupOldData(operationName)
        } else {
            Log.w(TAG, "No start time found for operation: $operationId")
        }
    }
    
    /**
     * Monitor a suspend function's performance
     */
    suspend inline fun <T> monitorSuspend(
        operationName: String,
        additionalMetrics: Map<String, Any> = emptyMap(),
        crossinline operation: suspend () -> T
    ): T {
        val operationId = startOperation(operationName)
        return try {
            operation()
        } finally {
            endOperation(operationId, additionalMetrics)
        }
    }
    
    /**
     * Monitor a regular function's performance
     */
    inline fun <T> monitor(
        operationName: String,
        additionalMetrics: Map<String, Any> = emptyMap(),
        operation: () -> T
    ): T {
        val operationId = startOperation(operationName)
        return try {
            operation()
        } finally {
            endOperation(operationId, additionalMetrics)
        }
    }
    
    /**
     * Get performance statistics for an operation
     */
    fun getOperationStats(operationName: String): OperationStats? {
        val times = operationTimes[operationName] ?: return null
        
        if (times.isEmpty()) return null
        
        val sortedTimes = times.sorted()
        return OperationStats(
            operationName = operationName,
            totalExecutions = times.size,
            averageTime = times.average(),
            minTime = sortedTimes.first(),
            maxTime = sortedTimes.last(),
            medianTime = if (sortedTimes.size % 2 == 0) {
                (sortedTimes[sortedTimes.size / 2 - 1] + sortedTimes[sortedTimes.size / 2]) / 2.0
            } else {
                sortedTimes[sortedTimes.size / 2].toDouble()
            },
            p95Time = sortedTimes[(sortedTimes.size * 0.95).toInt().coerceAtMost(sortedTimes.size - 1)],
            threshold = getThresholdForOperation(operationName)
        )
    }
    
    /**
     * Get all performance statistics
     */
    fun getAllStats(): Map<String, OperationStats> {
        return operationTimes.keys.mapNotNull { operationName ->
            getOperationStats(operationName)?.let { operationName to it }
        }.toMap()
    }
    
    /**
     * Log performance summary
     */
    fun logPerformanceSummary() {
        CoroutineScope(Dispatchers.IO).launch {
            val allStats = getAllStats()
            
            Log.i(TAG, "=== Performance Summary ===")
            Log.i(TAG, "Active operations: ${activeOperations.size}")
            Log.i(TAG, "Tracked operations: ${allStats.size}")
            
            allStats.forEach { (name, stats) ->
                Log.i(TAG, "$name: avg=${stats.averageTime.toInt()}ms, " +
                        "p95=${stats.p95Time}ms, executions=${stats.totalExecutions}")
                
                if (stats.averageTime > stats.threshold) {
                    Log.w(TAG, "⚠️ $name exceeds threshold (${stats.threshold}ms)")
                }
            }
            
            // Log to Crashlytics for monitoring
            BugReportLogger.addBreadcrumb(
                "Performance summary: ${allStats.size} operations tracked, " +
                        "${activeOperations.size} active",
                "performance"
            )
        }
    }
    
    /**
     * Get threshold for specific operation type
     */
    private fun getThresholdForOperation(operationName: String): Long {
        return when {
            operationName.contains("submit", ignoreCase = true) -> SUBMISSION_THRESHOLD
            operationName.contains("validate", ignoreCase = true) -> VALIDATION_THRESHOLD
            operationName.contains("sync", ignoreCase = true) -> SYNC_THRESHOLD
            operationName.contains("ui", ignoreCase = true) -> UI_RESPONSE_THRESHOLD
            else -> SUBMISSION_THRESHOLD // Default threshold
        }
    }
    
    /**
     * Clean up old performance data to prevent memory leaks
     */
    private fun cleanupOldData(operationName: String) {
        operationTimes[operationName]?.let { times ->
            if (times.size > 100) { // Keep only last 100 measurements
                val toRemove = times.size - 100
                repeat(toRemove) {
                    times.removeFirstOrNull()
                }
                Log.d(TAG, "Cleaned up $toRemove old measurements for $operationName")
            }
        }
    }
    
    /**
     * Clear all performance data
     */
    fun clearAllData() {
        operationTimes.clear()
        activeOperations.clear()
        Log.i(TAG, "Cleared all performance data")
    }
    
    /**
     * Get current active operations
     */
    fun getActiveOperations(): Map<String, Long> {
        val currentTime = System.currentTimeMillis()
        return activeOperations.mapValues { (_, startTime) ->
            currentTime - startTime
        }
    }
}

/**
 * Data class for operation performance statistics
 */
data class OperationStats(
    val operationName: String,
    val totalExecutions: Int,
    val averageTime: Double,
    val minTime: Long,
    val maxTime: Long,
    val medianTime: Double,
    val p95Time: Long,
    val threshold: Long
) {
    val isPerformant: Boolean
        get() = averageTime <= threshold
    
    val performanceRating: String
        get() = when {
            averageTime <= threshold * 0.5 -> "Excellent"
            averageTime <= threshold * 0.8 -> "Good"
            averageTime <= threshold -> "Acceptable"
            averageTime <= threshold * 1.5 -> "Poor"
            else -> "Critical"
        }
}