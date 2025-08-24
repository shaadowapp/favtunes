package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import kotlin.random.Random

/**
 * Comprehensive analytics system for notification tracking and optimization
 * Includes engagement metrics, A/B testing, and user feedback collection
 */
class NotificationAnalytics(
    private val context: Context,
    private val preferences: SharedPreferences = context.getSharedPreferences("notification_analytics", Context.MODE_PRIVATE)
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val EVENT_NOTIFICATION_DELIVERED = "notification_delivered"
        private const val EVENT_NOTIFICATION_CLICKED = "notification_clicked"
        private const val EVENT_NOTIFICATION_DISMISSED = "notification_dismissed"
        private const val EVENT_NOTIFICATION_IGNORED = "notification_ignored"
        private const val EVENT_NOTIFICATION_ACTION_TAKEN = "notification_action_taken"
        
        private const val AB_TEST_VARIANTS = 4 // Number of A/B test variants
    }
    
    /**
     * Track notification delivery event
     */
    fun trackNotificationDelivered(
        contentType: String,
        personalityTone: PersonalityTone,
        deliveryMethod: String = "immediate",
        abTestVariant: String? = null
    ) {
        scope.launch {
            val event = AnalyticsEvent(
                eventType = EVENT_NOTIFICATION_DELIVERED,
                contentType = contentType,
                personalityTone = personalityTone.name,
                deliveryMethod = deliveryMethod,
                abTestVariant = abTestVariant ?: getABTestVariant(contentType),
                timestamp = System.currentTimeMillis()
            )
            
            recordEvent(event)
            updateDeliveryMetrics(contentType, personalityTone)
        }
    }
    
    /**
     * Track notification click event
     */
    fun trackNotificationClicked(
        contentType: String,
        personalityTone: PersonalityTone,
        deliveryTime: Long,
        clickLatency: Long = System.currentTimeMillis() - deliveryTime
    ) {
        scope.launch {
            val event = AnalyticsEvent(
                eventType = EVENT_NOTIFICATION_CLICKED,
                contentType = contentType,
                personalityTone = personalityTone.name,
                clickLatency = clickLatency,
                timestamp = System.currentTimeMillis()
            )
            
            recordEvent(event)
            updateEngagementMetrics(contentType, personalityTone, true)
        }
    }
    
    /**
     * Track notification dismissal event
     */
    fun trackNotificationDismissed(
        contentType: String,
        personalityTone: PersonalityTone,
        deliveryTime: Long,
        dismissalLatency: Long = System.currentTimeMillis() - deliveryTime
    ) {
        scope.launch {
            val event = AnalyticsEvent(
                eventType = EVENT_NOTIFICATION_DISMISSED,
                contentType = contentType,
                personalityTone = personalityTone.name,
                dismissalLatency = dismissalLatency,
                timestamp = System.currentTimeMillis()
            )
            
            recordEvent(event)
            updateEngagementMetrics(contentType, personalityTone, false)
        }
    }
    
    /**
     * Track notification ignored event (no interaction after timeout)
     */
    fun trackNotificationIgnored(
        contentType: String,
        personalityTone: PersonalityTone,
        deliveryTime: Long
    ) {
        scope.launch {
            val event = AnalyticsEvent(
                eventType = EVENT_NOTIFICATION_IGNORED,
                contentType = contentType,
                personalityTone = personalityTone.name,
                timestamp = System.currentTimeMillis()
            )
            
            recordEvent(event)
            updateIgnoreMetrics(contentType, personalityTone)
        }
    }
    
    /**
     * Track notification action taken (e.g., song played from notification)
     */
    fun trackNotificationActionTaken(
        contentType: String,
        personalityTone: PersonalityTone,
        actionType: String,
        deliveryTime: Long
    ) {
        scope.launch {
            val event = AnalyticsEvent(
                eventType = EVENT_NOTIFICATION_ACTION_TAKEN,
                contentType = contentType,
                personalityTone = personalityTone.name,
                actionType = actionType,
                timestamp = System.currentTimeMillis()
            )
            
            recordEvent(event)
            updateActionMetrics(contentType, personalityTone, actionType)
        }
    }
    
    /**
     * Get engagement metrics for a specific content type
     */
    fun getEngagementMetrics(contentType: String): EngagementMetrics {
        val delivered = preferences.getInt("delivered_$contentType", 0)
        val clicked = preferences.getInt("clicked_$contentType", 0)
        val dismissed = preferences.getInt("dismissed_$contentType", 0)
        val ignored = preferences.getInt("ignored_$contentType", 0)
        val actions = preferences.getInt("actions_$contentType", 0)
        
        val clickThroughRate = if (delivered > 0) clicked.toDouble() / delivered else 0.0
        val dismissalRate = if (delivered > 0) dismissed.toDouble() / delivered else 0.0
        val ignoreRate = if (delivered > 0) ignored.toDouble() / delivered else 0.0
        val actionRate = if (clicked > 0) actions.toDouble() / clicked else 0.0
        
        return EngagementMetrics(
            contentType = contentType,
            totalDelivered = delivered,
            totalClicked = clicked,
            totalDismissed = dismissed,
            totalIgnored = ignored,
            totalActions = actions,
            clickThroughRate = clickThroughRate,
            dismissalRate = dismissalRate,
            ignoreRate = ignoreRate,
            actionRate = actionRate
        )
    }
    
    /**
     * Get A/B test results for content optimization
     */
    fun getABTestResults(contentType: String): List<ABTestResult> {
        val results = mutableListOf<ABTestResult>()
        
        for (variant in 0 until AB_TEST_VARIANTS) {
            val variantKey = "${contentType}_variant_$variant"
            val delivered = preferences.getInt("delivered_$variantKey", 0)
            val clicked = preferences.getInt("clicked_$variantKey", 0)
            
            if (delivered > 0) {
                val ctr = clicked.toDouble() / delivered
                results.add(
                    ABTestResult(
                        contentType = contentType,
                        variant = "variant_$variant",
                        delivered = delivered,
                        clicked = clicked,
                        clickThroughRate = ctr,
                        confidence = calculateConfidence(delivered, clicked)
                    )
                )
            }
        }
        
        return results.sortedByDescending { it.clickThroughRate }
    }
    
    /**
     * Get personality tone performance metrics
     */
    fun getPersonalityToneMetrics(): List<PersonalityToneMetrics> {
        val toneMetrics = mutableListOf<PersonalityToneMetrics>()
        
        PersonalityTone.values().forEach { tone ->
            val delivered = preferences.getInt("delivered_tone_${tone.name}", 0)
            val clicked = preferences.getInt("clicked_tone_${tone.name}", 0)
            val dismissed = preferences.getInt("dismissed_tone_${tone.name}", 0)
            
            if (delivered > 0) {
                toneMetrics.add(
                    PersonalityToneMetrics(
                        tone = tone,
                        delivered = delivered,
                        clicked = clicked,
                        dismissed = dismissed,
                        clickThroughRate = clicked.toDouble() / delivered,
                        dismissalRate = dismissed.toDouble() / delivered
                    )
                )
            }
        }
        
        return toneMetrics.sortedByDescending { it.clickThroughRate }
    }
    
    /**
     * Collect user feedback on notification quality
     */
    fun collectUserFeedback(
        contentType: String,
        personalityTone: PersonalityTone,
        rating: Int, // 1-5 scale
        feedback: String? = null
    ) {
        scope.launch {
            val feedbackEvent = UserFeedback(
                contentType = contentType,
                personalityTone = personalityTone,
                rating = rating,
                feedback = feedback,
                timestamp = System.currentTimeMillis()
            )
            
            recordUserFeedback(feedbackEvent)
            updateFeedbackMetrics(contentType, personalityTone, rating)
        }
    }
    
    /**
     * Get user feedback summary
     */
    fun getFeedbackSummary(contentType: String): FeedbackSummary {
        val totalRatings = preferences.getInt("feedback_count_$contentType", 0)
        val totalScore = preferences.getInt("feedback_total_$contentType", 0)
        val averageRating = if (totalRatings > 0) totalScore.toDouble() / totalRatings else 0.0
        
        val ratingDistribution = mutableMapOf<Int, Int>()
        for (rating in 1..5) {
            ratingDistribution[rating] = preferences.getInt("feedback_${contentType}_rating_$rating", 0)
        }
        
        return FeedbackSummary(
            contentType = contentType,
            totalRatings = totalRatings,
            averageRating = averageRating,
            ratingDistribution = ratingDistribution
        )
    }
    
    /**
     * Get optimal notification timing based on analytics
     */
    fun getOptimalTimingRecommendations(): List<TimingRecommendation> {
        val recommendations = mutableListOf<TimingRecommendation>()
        
        // Analyze hourly engagement patterns
        for (hour in 0..23) {
            val delivered = preferences.getInt("hourly_delivered_$hour", 0)
            val clicked = preferences.getInt("hourly_clicked_$hour", 0)
            
            if (delivered >= 10) { // Minimum sample size
                val ctr = clicked.toDouble() / delivered
                recommendations.add(
                    TimingRecommendation(
                        hour = hour,
                        delivered = delivered,
                        clicked = clicked,
                        clickThroughRate = ctr,
                        recommendation = when {
                            ctr > 0.4 -> "Optimal"
                            ctr > 0.2 -> "Good"
                            else -> "Avoid"
                        }
                    )
                )
            }
        }
        
        return recommendations.sortedByDescending { it.clickThroughRate }
    }
    
    /**
     * Export analytics data for external analysis
     */
    fun exportAnalyticsData(): String {
        val data = JSONObject()
        
        // Export engagement metrics
        val engagementData = JSONObject()
        val contentTypes = listOf("engagement", "music_suggestion", "marketing", "trending", "personalized_suggestion")
        
        contentTypes.forEach { contentType ->
            val metrics = getEngagementMetrics(contentType)
            engagementData.put(contentType, JSONObject().apply {
                put("delivered", metrics.totalDelivered)
                put("clicked", metrics.totalClicked)
                put("dismissed", metrics.totalDismissed)
                put("ctr", metrics.clickThroughRate)
                put("dismissal_rate", metrics.dismissalRate)
            })
        }
        
        data.put("engagement_metrics", engagementData)
        
        // Export A/B test results
        val abTestData = JSONObject()
        contentTypes.forEach { contentType ->
            val results = getABTestResults(contentType)
            if (results.isNotEmpty()) {
                abTestData.put(contentType, results.map { result ->
                    JSONObject().apply {
                        put("variant", result.variant)
                        put("delivered", result.delivered)
                        put("clicked", result.clicked)
                        put("ctr", result.clickThroughRate)
                    }
                })
            }
        }
        
        data.put("ab_test_results", abTestData)
        
        // Export timing recommendations
        val timingData = getOptimalTimingRecommendations().map { rec ->
            JSONObject().apply {
                put("hour", rec.hour)
                put("ctr", rec.clickThroughRate)
                put("recommendation", rec.recommendation)
            }
        }
        
        data.put("timing_recommendations", timingData)
        
        return data.toString(2)
    }
    
    // Private helper methods
    
    private fun recordEvent(event: AnalyticsEvent) {
        val eventKey = "event_${event.timestamp}_${Random.nextInt(1000)}"
        val eventJson = JSONObject().apply {
            put("type", event.eventType)
            put("content_type", event.contentType)
            put("personality_tone", event.personalityTone)
            put("delivery_method", event.deliveryMethod)
            put("ab_variant", event.abTestVariant)
            put("click_latency", event.clickLatency)
            put("dismissal_latency", event.dismissalLatency)
            put("action_type", event.actionType)
            put("timestamp", event.timestamp)
        }
        
        preferences.edit().putString(eventKey, eventJson.toString()).apply()
        
        // Also update hourly metrics
        val calendar = Calendar.getInstance().apply { timeInMillis = event.timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        when (event.eventType) {
            EVENT_NOTIFICATION_DELIVERED -> {
                val hourlyDelivered = preferences.getInt("hourly_delivered_$hour", 0)
                preferences.edit().putInt("hourly_delivered_$hour", hourlyDelivered + 1).apply()
            }
            EVENT_NOTIFICATION_CLICKED -> {
                val hourlyClicked = preferences.getInt("hourly_clicked_$hour", 0)
                preferences.edit().putInt("hourly_clicked_$hour", hourlyClicked + 1).apply()
            }
        }
    }
    
    private fun updateDeliveryMetrics(contentType: String, personalityTone: PersonalityTone) {
        val deliveredKey = "delivered_$contentType"
        val toneDeliveredKey = "delivered_tone_${personalityTone.name}"
        
        val currentDelivered = preferences.getInt(deliveredKey, 0)
        val currentToneDelivered = preferences.getInt(toneDeliveredKey, 0)
        
        preferences.edit()
            .putInt(deliveredKey, currentDelivered + 1)
            .putInt(toneDeliveredKey, currentToneDelivered + 1)
            .apply()
    }
    
    private fun updateEngagementMetrics(contentType: String, personalityTone: PersonalityTone, wasClicked: Boolean) {
        val key = if (wasClicked) "clicked_$contentType" else "dismissed_$contentType"
        val toneKey = if (wasClicked) "clicked_tone_${personalityTone.name}" else "dismissed_tone_${personalityTone.name}"
        
        val currentCount = preferences.getInt(key, 0)
        val currentToneCount = preferences.getInt(toneKey, 0)
        
        preferences.edit()
            .putInt(key, currentCount + 1)
            .putInt(toneKey, currentToneCount + 1)
            .apply()
    }
    
    private fun updateIgnoreMetrics(contentType: String, personalityTone: PersonalityTone) {
        val ignoredKey = "ignored_$contentType"
        val currentIgnored = preferences.getInt(ignoredKey, 0)
        preferences.edit().putInt(ignoredKey, currentIgnored + 1).apply()
    }
    
    private fun updateActionMetrics(contentType: String, personalityTone: PersonalityTone, actionType: String) {
        val actionsKey = "actions_$contentType"
        val actionTypeKey = "action_${contentType}_$actionType"
        
        val currentActions = preferences.getInt(actionsKey, 0)
        val currentActionType = preferences.getInt(actionTypeKey, 0)
        
        preferences.edit()
            .putInt(actionsKey, currentActions + 1)
            .putInt(actionTypeKey, currentActionType + 1)
            .apply()
    }
    
    private fun recordUserFeedback(feedback: UserFeedback) {
        val feedbackKey = "feedback_${feedback.timestamp}_${Random.nextInt(1000)}"
        val feedbackJson = JSONObject().apply {
            put("content_type", feedback.contentType)
            put("personality_tone", feedback.personalityTone.name)
            put("rating", feedback.rating)
            put("feedback", feedback.feedback)
            put("timestamp", feedback.timestamp)
        }
        
        preferences.edit().putString(feedbackKey, feedbackJson.toString()).apply()
    }
    
    private fun updateFeedbackMetrics(contentType: String, personalityTone: PersonalityTone, rating: Int) {
        val countKey = "feedback_count_$contentType"
        val totalKey = "feedback_total_$contentType"
        val ratingKey = "feedback_${contentType}_rating_$rating"
        
        val currentCount = preferences.getInt(countKey, 0)
        val currentTotal = preferences.getInt(totalKey, 0)
        val currentRatingCount = preferences.getInt(ratingKey, 0)
        
        preferences.edit()
            .putInt(countKey, currentCount + 1)
            .putInt(totalKey, currentTotal + rating)
            .putInt(ratingKey, currentRatingCount + 1)
            .apply()
    }
    
    private fun getABTestVariant(contentType: String): String {
        // Simple hash-based A/B test assignment
        val hash = contentType.hashCode()
        val variant = Math.abs(hash) % AB_TEST_VARIANTS
        return "variant_$variant"
    }
    
    private fun calculateConfidence(delivered: Int, clicked: Int): Double {
        // Simple confidence calculation based on sample size
        return when {
            delivered >= 1000 -> 0.95
            delivered >= 500 -> 0.90
            delivered >= 100 -> 0.80
            delivered >= 50 -> 0.70
            else -> 0.50
        }
    }
    
    /**
     * Clean up old analytics data to prevent storage bloat
     */
    fun cleanupOldData() {
        scope.launch {
            val cutoffTime = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L) // 90 days
            val editor = preferences.edit()
            
            preferences.all.keys.filter { key ->
                (key.startsWith("event_") || key.startsWith("feedback_")) &&
                key.substringAfter("_").substringBefore("_").toLongOrNull()?.let { it < cutoffTime } == true
            }.forEach { key ->
                editor.remove(key)
            }
            
            editor.apply()
        }
    }
}

// Data classes for analytics

data class AnalyticsEvent(
    val eventType: String,
    val contentType: String,
    val personalityTone: String,
    val deliveryMethod: String? = null,
    val abTestVariant: String? = null,
    val clickLatency: Long? = null,
    val dismissalLatency: Long? = null,
    val actionType: String? = null,
    val timestamp: Long
)

data class EngagementMetrics(
    val contentType: String,
    val totalDelivered: Int,
    val totalClicked: Int,
    val totalDismissed: Int,
    val totalIgnored: Int,
    val totalActions: Int,
    val clickThroughRate: Double,
    val dismissalRate: Double,
    val ignoreRate: Double,
    val actionRate: Double
)

data class ABTestResult(
    val contentType: String,
    val variant: String,
    val delivered: Int,
    val clicked: Int,
    val clickThroughRate: Double,
    val confidence: Double
)

data class PersonalityToneMetrics(
    val tone: PersonalityTone,
    val delivered: Int,
    val clicked: Int,
    val dismissed: Int,
    val clickThroughRate: Double,
    val dismissalRate: Double
)

data class UserFeedback(
    val contentType: String,
    val personalityTone: PersonalityTone,
    val rating: Int,
    val feedback: String?,
    val timestamp: Long
)

data class FeedbackSummary(
    val contentType: String,
    val totalRatings: Int,
    val averageRating: Double,
    val ratingDistribution: Map<Int, Int>
)

data class TimingRecommendation(
    val hour: Int,
    val delivered: Int,
    val clicked: Int,
    val clickThroughRate: Double,
    val recommendation: String
)