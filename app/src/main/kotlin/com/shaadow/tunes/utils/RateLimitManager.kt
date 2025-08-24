package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RateLimitManager(context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val inMemoryCache = ConcurrentHashMap<String, MutableList<Long>>()
    
    companion object {
        private const val TAG = "RateLimitManager"
        private const val PREFS_NAME = "rate_limit_prefs"
        
        // Rate limits
        private const val MAX_SUBMISSIONS_PER_HOUR = 10
        private const val MAX_SUBMISSIONS_PER_DAY = 50
        private const val MAX_SUBMISSIONS_PER_MINUTE = 3
        
        // Time windows
        private val HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1)
        private val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
        private val MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1)
        
        // Keys for different submission types
        private const val BUG_REPORT_KEY = "bug_reports"
        private const val FEEDBACK_KEY = "feedback"
        private const val GENERAL_KEY = "general"
    }
    
    /**
     * Check if a bug report submission is allowed
     */
    fun canSubmitBugReport(): RateLimitResult {
        return checkRateLimit(BUG_REPORT_KEY, "bug report")
    }
    
    /**
     * Check if a feedback submission is allowed
     */
    fun canSubmitFeedback(): RateLimitResult {
        return checkRateLimit(FEEDBACK_KEY, "feedback")
    }
    
    /**
     * Record a bug report submission
     */
    fun recordBugReportSubmission() {
        recordSubmission(BUG_REPORT_KEY)
    }
    
    /**
     * Record a feedback submission
     */
    fun recordFeedbackSubmission() {
        recordSubmission(FEEDBACK_KEY)
    }
    
    /**
     * Check rate limit for a specific key
     */
    private fun checkRateLimit(key: String, submissionType: String): RateLimitResult {
        val now = System.currentTimeMillis()
        val submissions = getSubmissions(key)
        
        // Clean old submissions
        cleanOldSubmissions(submissions, now)
        
        // Check minute limit
        val submissionsInLastMinute = submissions.count { now - it < MINUTE_IN_MILLIS }
        if (submissionsInLastMinute >= MAX_SUBMISSIONS_PER_MINUTE) {
            Log.w(TAG, "Rate limit exceeded for $submissionType: $submissionsInLastMinute submissions in last minute")
            return RateLimitResult(
                allowed = false,
                reason = "Too many submissions in the last minute. Please wait before submitting again.",
                retryAfterSeconds = 60
            )
        }
        
        // Check hourly limit
        val submissionsInLastHour = submissions.count { now - it < HOUR_IN_MILLIS }
        if (submissionsInLastHour >= MAX_SUBMISSIONS_PER_HOUR) {
            Log.w(TAG, "Rate limit exceeded for $submissionType: $submissionsInLastHour submissions in last hour")
            val oldestInHour = submissions.filter { now - it < HOUR_IN_MILLIS }.minOrNull() ?: now
            val retryAfter = ((oldestInHour + HOUR_IN_MILLIS - now) / 1000).toInt()
            return RateLimitResult(
                allowed = false,
                reason = "Too many submissions in the last hour. Please try again later.",
                retryAfterSeconds = retryAfter
            )
        }
        
        // Check daily limit
        val submissionsInLastDay = submissions.count { now - it < DAY_IN_MILLIS }
        if (submissionsInLastDay >= MAX_SUBMISSIONS_PER_DAY) {
            Log.w(TAG, "Rate limit exceeded for $submissionType: $submissionsInLastDay submissions in last day")
            val oldestInDay = submissions.filter { now - it < DAY_IN_MILLIS }.minOrNull() ?: now
            val retryAfter = ((oldestInDay + DAY_IN_MILLIS - now) / 1000).toInt()
            return RateLimitResult(
                allowed = false,
                reason = "Daily submission limit reached. Please try again tomorrow.",
                retryAfterSeconds = retryAfter
            )
        }
        
        return RateLimitResult(allowed = true)
    }
    
    /**
     * Record a submission timestamp
     */
    private fun recordSubmission(key: String) {
        val now = System.currentTimeMillis()
        val submissions = getSubmissions(key)
        
        submissions.add(now)
        cleanOldSubmissions(submissions, now)
        
        // Persist to SharedPreferences
        saveSubmissions(key, submissions)
        
        Log.d(TAG, "Recorded submission for $key at $now")
    }
    
    /**
     * Get submissions list for a key
     */
    private fun getSubmissions(key: String): MutableList<Long> {
        return inMemoryCache.getOrPut(key) {
            loadSubmissions(key).toMutableList()
        }
    }
    
    /**
     * Load submissions from SharedPreferences
     */
    private fun loadSubmissions(key: String): List<Long> {
        return try {
            val submissionsString = preferences.getString(key, "") ?: ""
            if (submissionsString.isBlank()) {
                emptyList()
            } else {
                submissionsString.split(",").mapNotNull { it.toLongOrNull() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading submissions for $key", e)
            emptyList()
        }
    }
    
    /**
     * Save submissions to SharedPreferences
     */
    private fun saveSubmissions(key: String, submissions: List<Long>) {
        try {
            val submissionsString = submissions.joinToString(",")
            preferences.edit().putString(key, submissionsString).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving submissions for $key", e)
        }
    }
    
    /**
     * Clean old submissions that are outside the tracking window
     */
    private fun cleanOldSubmissions(submissions: MutableList<Long>, now: Long) {
        val cutoff = now - DAY_IN_MILLIS
        submissions.removeAll { it < cutoff }
    }
    
    /**
     * Get current rate limit status
     */
    fun getRateLimitStatus(): RateLimitStatus {
        val now = System.currentTimeMillis()
        
        val bugReportSubmissions = getSubmissions(BUG_REPORT_KEY)
        val feedbackSubmissions = getSubmissions(FEEDBACK_KEY)
        
        cleanOldSubmissions(bugReportSubmissions, now)
        cleanOldSubmissions(feedbackSubmissions, now)
        
        return RateLimitStatus(
            bugReportsInLastHour = bugReportSubmissions.count { now - it < HOUR_IN_MILLIS },
            bugReportsInLastDay = bugReportSubmissions.count { now - it < DAY_IN_MILLIS },
            feedbackInLastHour = feedbackSubmissions.count { now - it < HOUR_IN_MILLIS },
            feedbackInLastDay = feedbackSubmissions.count { now - it < DAY_IN_MILLIS },
            maxPerHour = MAX_SUBMISSIONS_PER_HOUR,
            maxPerDay = MAX_SUBMISSIONS_PER_DAY
        )
    }
    
    /**
     * Clear all rate limit data (for testing or reset purposes)
     */
    fun clearRateLimitData() {
        inMemoryCache.clear()
        preferences.edit().clear().apply()
        Log.d(TAG, "Rate limit data cleared")
    }
}

/**
 * Result of a rate limit check
 */
data class RateLimitResult(
    val allowed: Boolean,
    val reason: String? = null,
    val retryAfterSeconds: Int = 0
)

/**
 * Current rate limit status
 */
data class RateLimitStatus(
    val bugReportsInLastHour: Int,
    val bugReportsInLastDay: Int,
    val feedbackInLastHour: Int,
    val feedbackInLastDay: Int,
    val maxPerHour: Int,
    val maxPerDay: Int
) {
    val canSubmitBugReport: Boolean
        get() = bugReportsInLastHour < maxPerHour && bugReportsInLastDay < maxPerDay
    
    val canSubmitFeedback: Boolean
        get() = feedbackInLastHour < maxPerHour && feedbackInLastDay < maxPerDay
}