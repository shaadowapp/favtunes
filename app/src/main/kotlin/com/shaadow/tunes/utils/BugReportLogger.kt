package com.shaadow.tunes.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object BugReportLogger {
    
    private const val TAG = "BugReportLogger"
    
    // Performance monitoring
    private val activeTraces = ConcurrentHashMap<String, Trace>()
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val performance = FirebasePerformance.getInstance()
    
    // Log levels
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR, CRITICAL
    }
    
    // Error categories
    enum class ErrorCategory {
        SUBMISSION_ERROR,
        VALIDATION_ERROR,
        NETWORK_ERROR,
        SYNC_ERROR,
        AUTHENTICATION_ERROR,
        STORAGE_ERROR,
        PERFORMANCE_ERROR,
        UI_ERROR
    }
    
    init {
        // Enable Crashlytics collection
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Set custom keys for better error tracking
        crashlytics.setCustomKey("feature", "bug_report_system")
        crashlytics.setCustomKey("version", "1.0.0")
    }
    
    /**
     * Log a submission error with context
     */
    fun logSubmissionError(error: Throwable, context: String, additionalData: Map<String, Any> = emptyMap()) {
        val errorId = generateErrorId()
        
        Log.e(TAG, "Submission Error [$errorId]: $context", error)
        
        // Log to Crashlytics
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.SUBMISSION_ERROR.name)
            setCustomKey("context", context)
            setCustomKey("timestamp", System.currentTimeMillis())
            
            // Add additional data
            additionalData.forEach { (key, value) ->
                setCustomKey("data_$key", value.toString())
            }
            
            recordException(error)
        }
        
        // Log structured error for analytics
        logStructuredError(
            category = ErrorCategory.SUBMISSION_ERROR,
            level = LogLevel.ERROR,
            message = "Submission failed: $context",
            error = error,
            additionalData = additionalData
        )
    }
    
    /**
     * Log validation errors
     */
    fun logValidationError(field: String, value: Any?, error: String, additionalContext: Map<String, Any> = emptyMap()) {
        val errorId = generateErrorId()
        
        Log.w(TAG, "Validation Error [$errorId]: Field '$field' failed validation: $error")
        
        val errorData = mutableMapOf<String, Any>(
            "field" to field,
            "error_message" to error,
            "field_value_type" to (value?.javaClass?.simpleName ?: "null")
        ).apply {
            putAll(additionalContext)
            // Don't log actual field values for privacy
            if (value != null) {
                put("has_value", true)
                put("value_length", value.toString().length)
            } else {
                put("has_value", false)
            }
        }
        
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.VALIDATION_ERROR.name)
            setCustomKey("validation_field", field)
            setCustomKey("validation_error", error)
        }
        
        logStructuredError(
            category = ErrorCategory.VALIDATION_ERROR,
            level = LogLevel.WARNING,
            message = "Validation failed for field: $field",
            additionalData = errorData
        )
    }
    
    /**
     * Log sync operation errors
     */
    fun logSyncError(operation: String, error: Throwable, itemCount: Int = 0, retryAttempt: Int = 0) {
        val errorId = generateErrorId()
        
        Log.e(TAG, "Sync Error [$errorId]: Operation '$operation' failed (attempt $retryAttempt, items: $itemCount)", error)
        
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.SYNC_ERROR.name)
            setCustomKey("sync_operation", operation)
            setCustomKey("item_count", itemCount)
            setCustomKey("retry_attempt", retryAttempt)
            
            recordException(error)
        }
        
        logStructuredError(
            category = ErrorCategory.SYNC_ERROR,
            level = if (retryAttempt > 3) LogLevel.ERROR else LogLevel.WARNING,
            message = "Sync operation failed: $operation",
            error = error,
            additionalData = mapOf(
                "operation" to operation,
                "item_count" to itemCount,
                "retry_attempt" to retryAttempt
            )
        )
    }
    
    /**
     * Log network connectivity issues
     */
    fun logNetworkError(operation: String, error: Throwable, networkType: String = "unknown") {
        val errorId = generateErrorId()
        
        Log.w(TAG, "Network Error [$errorId]: $operation failed on $networkType", error)
        
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.NETWORK_ERROR.name)
            setCustomKey("network_operation", operation)
            setCustomKey("network_type", networkType)
            
            recordException(error)
        }
        
        logStructuredError(
            category = ErrorCategory.NETWORK_ERROR,
            level = LogLevel.WARNING,
            message = "Network operation failed: $operation",
            error = error,
            additionalData = mapOf(
                "operation" to operation,
                "network_type" to networkType
            )
        )
    }
    
    /**
     * Log authentication errors
     */
    fun logAuthenticationError(error: Throwable, context: String) {
        val errorId = generateErrorId()
        
        Log.e(TAG, "Authentication Error [$errorId]: $context", error)
        
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.AUTHENTICATION_ERROR.name)
            setCustomKey("auth_context", context)
            
            recordException(error)
        }
        
        logStructuredError(
            category = ErrorCategory.AUTHENTICATION_ERROR,
            level = LogLevel.ERROR,
            message = "Authentication failed: $context",
            error = error
        )
    }
    
    /**
     * Log performance issues
     */
    fun logPerformanceIssue(operation: String, duration: Long, threshold: Long, additionalMetrics: Map<String, Any> = emptyMap()) {
        val errorId = generateErrorId()
        
        Log.w(TAG, "Performance Issue [$errorId]: $operation took ${duration}ms (threshold: ${threshold}ms)")
        
        crashlytics.apply {
            setCustomKey("error_id", errorId)
            setCustomKey("error_category", ErrorCategory.PERFORMANCE_ERROR.name)
            setCustomKey("perf_operation", operation)
            setCustomKey("perf_duration", duration)
            setCustomKey("perf_threshold", threshold)
            
            additionalMetrics.forEach { (key, value) ->
                setCustomKey("perf_$key", value.toString())
            }
        }
        
        logStructuredError(
            category = ErrorCategory.PERFORMANCE_ERROR,
            level = LogLevel.WARNING,
            message = "Performance threshold exceeded: $operation",
            additionalData = mapOf(
                "operation" to operation,
                "duration_ms" to duration,
                "threshold_ms" to threshold
            ) + additionalMetrics
        )
    }
    
    /**
     * Start performance monitoring for an operation
     */
    fun startPerformanceTrace(traceName: String): String {
        val traceId = "${traceName}_${System.currentTimeMillis()}"
        
        try {
            val trace = performance.newTrace("bug_report_$traceName")
            trace.start()
            activeTraces[traceId] = trace
            
            Log.d(TAG, "Started performance trace: $traceId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start performance trace: $traceId", e)
        }
        
        return traceId
    }
    
    /**
     * Stop performance monitoring and log results
     */
    fun stopPerformanceTrace(traceId: String, additionalMetrics: Map<String, Long> = emptyMap()) {
        try {
            activeTraces.remove(traceId)?.let { trace ->
                // Add custom metrics
                additionalMetrics.forEach { (key, value) ->
                    trace.putMetric(key, value)
                }
                
                trace.stop()
                Log.d(TAG, "Stopped performance trace: $traceId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop performance trace: $traceId", e)
        }
    }
    
    /**
     * Log successful operations for monitoring
     */
    fun logSuccessfulOperation(operation: String, duration: Long, additionalData: Map<String, Any> = emptyMap()) {
        Log.i(TAG, "Successful operation: $operation (${duration}ms)")
        
        crashlytics.apply {
            setCustomKey("last_successful_operation", operation)
            setCustomKey("last_success_timestamp", System.currentTimeMillis())
            setCustomKey("last_success_duration", duration)
        }
        
        // Log to analytics for success rate monitoring
        logStructuredError(
            category = ErrorCategory.SUBMISSION_ERROR, // Reusing category for success tracking
            level = LogLevel.INFO,
            message = "Operation completed successfully: $operation",
            additionalData = mapOf(
                "operation" to operation,
                "duration_ms" to duration,
                "status" to "success"
            ) + additionalData
        )
    }
    
    /**
     * Log structured error for analytics
     */
    private fun logStructuredError(
        category: ErrorCategory,
        level: LogLevel,
        message: String,
        error: Throwable? = null,
        additionalData: Map<String, Any> = emptyMap()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logEntry = mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "category" to category.name,
                    "level" to level.name,
                    "message" to message,
                    "error_type" to (error?.javaClass?.simpleName ?: "none"),
                    "error_message" to (error?.message ?: ""),
                    "thread" to Thread.currentThread().name
                ) + additionalData
                
                // In a real implementation, you might send this to your analytics service
                Log.d(TAG, "Structured log: $logEntry")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log structured error", e)
            }
        }
    }
    
    /**
     * Generate unique error ID for tracking
     */
    private fun generateErrorId(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val random = (1000..9999).random()
        return "${timestamp}_$random"
    }
    
    /**
     * Set user context for error tracking
     */
    fun setUserContext(userId: String?, isAnonymous: Boolean = true) {
        crashlytics.apply {
            if (!isAnonymous && userId != null) {
                setUserId(userId)
            } else {
                setUserId("anonymous_user")
            }
            setCustomKey("user_anonymous", isAnonymous)
        }
    }
    
    /**
     * Add breadcrumb for debugging
     */
    fun addBreadcrumb(message: String, category: String = "bug_report") {
        Log.d(TAG, "Breadcrumb [$category]: $message")
        
        crashlytics.log("[$category] $message")
    }
    
    /**
     * Get error statistics for monitoring
     */
    fun getErrorStats(): Map<String, Any> {
        return mapOf(
            "active_traces" to activeTraces.size,
            "timestamp" to System.currentTimeMillis(),
            "logger_version" to "1.0.0"
        )
    }
}