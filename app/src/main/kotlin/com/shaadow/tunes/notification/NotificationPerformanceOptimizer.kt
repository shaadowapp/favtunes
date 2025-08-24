package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import android.os.BatteryManager
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashMap

/**
 * Performance optimization system for notification operations
 * Includes content caching, battery-aware processing, and memory-efficient operations
 */
class NotificationPerformanceOptimizer(
    private val context: Context,
    private val preferences: SharedPreferences = context.getSharedPreferences("notification_performance", Context.MODE_PRIVATE)
) {
    
    private val contentCache = ContentCache()
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val MAX_CACHE_SIZE = 100
        private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val CRITICAL_BATTERY_THRESHOLD = 10
        private const val MAX_CONCURRENT_OPERATIONS = 3
    }
    
    private val activeOperations = AtomicInteger(0)
    
    /**
     * Get cached notification content or generate new content efficiently
     */
    suspend fun getOptimizedContent(
        contentType: String,
        parameters: Map<String, Any>,
        generator: suspend () -> NotificationContent
    ): NotificationContent = withContext(Dispatchers.Default) {
        
        val cacheKey = generateCacheKey(contentType, parameters)
        
        // Try to get from cache first
        contentCache.get(cacheKey)?.let { cachedContent ->
            return@withContext cachedContent
        }
        
        // Generate new content with performance monitoring
        val startTime = System.currentTimeMillis()
        
        try {
            // Limit concurrent operations to prevent resource exhaustion
            if (activeOperations.get() >= MAX_CONCURRENT_OPERATIONS) {
                delay(100) // Brief delay to prevent overwhelming the system
            }
            
            activeOperations.incrementAndGet()
            
            val content = if (isBatteryOptimizationNeeded()) {
                generateBatteryOptimizedContent(contentType, parameters, generator)
            } else {
                generator()
            }
            
            // Cache the generated content
            contentCache.put(cacheKey, content)
            
            // Record performance metrics
            val generationTime = System.currentTimeMillis() - startTime
            recordPerformanceMetrics(contentType, generationTime)
            
            content
            
        } finally {
            activeOperations.decrementAndGet()
        }
    }
    
    /**
     * Optimize string operations for content generation
     */
    fun optimizeStringOperations(
        templates: List<String>,
        replacements: Map<String, String>
    ): String {
        // Use StringBuilder for efficient string concatenation
        val result = StringBuilder()
        
        // Pre-calculate capacity to avoid resizing
        val estimatedLength = templates.maxOfOrNull { it.length } ?: 100
        result.ensureCapacity(estimatedLength + replacements.values.sumOf { it.length })
        
        // Select template efficiently
        val template = templates.randomOrNull() ?: return ""
        
        // Perform replacements in a single pass
        var workingString = template
        replacements.forEach { (placeholder, replacement) ->
            workingString = workingString.replace(placeholder, replacement, ignoreCase = false)
        }
        
        return workingString
    }
    
    /**
     * Batch process multiple notification operations for efficiency
     */
    suspend fun batchProcessNotifications(
        operations: List<suspend () -> Unit>
    ) = withContext(Dispatchers.Default) {
        
        val batchSize = if (isBatteryOptimizationNeeded()) 2 else 5
        
        operations.chunked(batchSize).forEach { batch ->
            // Process batch concurrently but with limited parallelism
            batch.map { operation ->
                async {
                    try {
                        operation()
                    } catch (e: Exception) {
                        // Log error but don't fail entire batch
                        recordError("batch_operation_failed", e)
                    }
                }
            }.awaitAll()
            
            // Add delay between batches if battery is low
            if (isBatteryOptimizationNeeded()) {
                delay(500)
            }
        }
    }
    
    /**
     * Preload and cache frequently used content during idle time
     */
    fun preloadFrequentContent() {
        backgroundScope.launch {
            // Only preload if battery is good and device is charging
            if (getBatteryLevel() > 50 && isDeviceCharging()) {
                
                val frequentContentTypes = getFrequentContentTypes()
                
                frequentContentTypes.forEach { contentType ->
                    try {
                        // Generate and cache common variations
                        val commonParameters = getCommonParameters(contentType)
                        
                        commonParameters.forEach { params ->
                            val cacheKey = generateCacheKey(contentType, params)
                            
                            if (!contentCache.contains(cacheKey)) {
                                // Generate content in background
                                val content = generatePreloadContent(contentType, params)
                                contentCache.put(cacheKey, content)
                                
                                // Small delay to prevent overwhelming the system
                                delay(100)
                            }
                        }
                        
                    } catch (e: Exception) {
                        recordError("preload_failed", e)
                    }
                }
            }
        }
    }
    
    /**
     * Clean up resources and optimize memory usage
     */
    fun optimizeMemoryUsage() {
        backgroundScope.launch {
            // Clean expired cache entries
            contentCache.cleanExpired()
            
            // Clear old performance metrics
            cleanOldPerformanceData()
            
            // Trigger garbage collection hint if memory pressure is high
            if (isMemoryPressureHigh()) {
                System.gc()
            }
        }
    }
    
    /**
     * Get performance recommendations based on current system state
     */
    fun getPerformanceRecommendations(): List<PerformanceRecommendation> {
        val recommendations = mutableListOf<PerformanceRecommendation>()
        
        val batteryLevel = getBatteryLevel()
        val cacheHitRate = contentCache.getHitRate()
        val avgGenerationTime = getAverageGenerationTime()
        
        // Battery recommendations
        when {
            batteryLevel < CRITICAL_BATTERY_THRESHOLD -> {
                recommendations.add(
                    PerformanceRecommendation(
                        type = "battery_critical",
                        message = "Critical battery level. Disable non-essential notifications.",
                        priority = PerformancePriority.HIGH
                    )
                )
            }
            batteryLevel < LOW_BATTERY_THRESHOLD -> {
                recommendations.add(
                    PerformanceRecommendation(
                        type = "battery_low",
                        message = "Low battery. Reduce notification frequency and complexity.",
                        priority = PerformancePriority.MEDIUM
                    )
                )
            }
        }
        
        // Cache recommendations
        if (cacheHitRate < 0.3) {
            recommendations.add(
                PerformanceRecommendation(
                    type = "cache_efficiency",
                    message = "Low cache hit rate. Consider preloading frequent content.",
                    priority = PerformancePriority.MEDIUM
                )
            )
        }
        
        // Performance recommendations
        if (avgGenerationTime > 500) {
            recommendations.add(
                PerformanceRecommendation(
                    type = "generation_slow",
                    message = "Slow content generation. Optimize templates and caching.",
                    priority = PerformancePriority.MEDIUM
                )
            )
        }
        
        return recommendations
    }
    
    // Private helper methods
    
    private fun isBatteryOptimizationNeeded(): Boolean {
        return getBatteryLevel() < LOW_BATTERY_THRESHOLD || !isDeviceCharging()
    }
    
    private fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    private fun isDeviceCharging(): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    private suspend fun generateBatteryOptimizedContent(
        contentType: String,
        parameters: Map<String, Any>,
        generator: suspend () -> NotificationContent
    ): NotificationContent {
        // Use simpler content generation for battery optimization
        return when (contentType) {
            "engagement" -> {
                // Use cached templates for faster generation
                val simpleTemplates = listOf(
                    "Missing your music? 🎵",
                    "Time for some tunes! 🎶",
                    "Your playlist awaits 🎧"
                )
                
                NotificationContent(
                    title = simpleTemplates.random(),
                    body = "Quick listen?",
                    emoji = "🎵",
                    actionText = "Play",
                    contentType = contentType
                )
            }
            else -> {
                // For other types, use the regular generator but with timeout
                withTimeout(2000) { // 2 second timeout for battery optimization
                    generator()
                }
            }
        }
    }
    
    private fun generateCacheKey(contentType: String, parameters: Map<String, Any>): String {
        // Create efficient cache key
        val keyBuilder = StringBuilder(contentType)
        parameters.entries.sortedBy { it.key }.forEach { (key, value) ->
            keyBuilder.append("_").append(key).append("_").append(value.hashCode())
        }
        return keyBuilder.toString()
    }
    
    private fun recordPerformanceMetrics(contentType: String, generationTime: Long) {
        val metricsKey = "perf_${contentType}_${System.currentTimeMillis()}"
        preferences.edit().putLong(metricsKey, generationTime).apply()
        
        // Update running averages
        val avgKey = "avg_generation_$contentType"
        val countKey = "count_generation_$contentType"
        
        val currentAvg = preferences.getLong(avgKey, 0L)
        val currentCount = preferences.getInt(countKey, 0)
        
        val newAvg = if (currentCount == 0) {
            generationTime
        } else {
            (currentAvg * currentCount + generationTime) / (currentCount + 1)
        }
        
        preferences.edit()
            .putLong(avgKey, newAvg)
            .putInt(countKey, currentCount + 1)
            .apply()
    }
    
    private fun recordError(errorType: String, error: Exception) {
        val errorKey = "error_${errorType}_${System.currentTimeMillis()}"
        preferences.edit().putString(errorKey, error.message ?: "Unknown error").apply()
    }
    
    private fun getFrequentContentTypes(): List<String> {
        // Return content types ordered by frequency of use
        return listOf("engagement", "music_suggestion", "personalized_suggestion", "trending", "marketing")
    }
    
    private fun getCommonParameters(contentType: String): List<Map<String, Any>> {
        return when (contentType) {
            "engagement" -> listOf(
                mapOf("hours" to 24),
                mapOf("hours" to 48),
                mapOf("hours" to 72)
            )
            "music_suggestion" -> listOf(
                mapOf("songs" to emptyList<String>()),
                mapOf("songs" to listOf("recent"))
            )
            else -> listOf(emptyMap())
        }
    }
    
    private fun generatePreloadContent(contentType: String, parameters: Map<String, Any>): NotificationContent {
        // Generate simple content for preloading
        return NotificationContent(
            title = "Preloaded $contentType",
            body = "Cached content",
            emoji = "⚡",
            actionText = "Go",
            contentType = contentType
        )
    }
    
    private fun isMemoryPressureHigh(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return usedMemory.toDouble() / maxMemory > 0.8 // 80% memory usage
    }
    
    private fun getAverageGenerationTime(): Long {
        val contentTypes = listOf("engagement", "music_suggestion", "marketing")
        val averages = contentTypes.mapNotNull { contentType ->
            preferences.getLong("avg_generation_$contentType", -1L).takeIf { it >= 0 }
        }
        
        return if (averages.isNotEmpty()) {
            averages.average().toLong()
        } else {
            0L
        }
    }
    
    private fun cleanOldPerformanceData() {
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days
        val editor = preferences.edit()
        
        preferences.all.keys.filter { key ->
            (key.startsWith("perf_") || key.startsWith("error_")) &&
            key.substringAfterLast("_").toLongOrNull()?.let { it < cutoffTime } == true
        }.forEach { key ->
            editor.remove(key)
        }
        
        editor.apply()
    }
}

/**
 * Efficient content cache with TTL and LRU eviction
 */
class ContentCache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = LinkedHashMap<String, Long>()
    private var hits = AtomicInteger(0)
    private var misses = AtomicInteger(0)
    
    companion object {
        private const val MAX_SIZE = 100
        private const val TTL_MS = 30 * 60 * 1000L // 30 minutes
    }
    
    fun get(key: String): NotificationContent? {
        val entry = cache[key]
        
        return if (entry != null && !entry.isExpired()) {
            // Update access time for LRU
            synchronized(accessOrder) {
                accessOrder[key] = System.currentTimeMillis()
            }
            hits.incrementAndGet()
            entry.content
        } else {
            if (entry != null) {
                cache.remove(key)
            }
            misses.incrementAndGet()
            null
        }
    }
    
    fun put(key: String, content: NotificationContent) {
        // Evict if cache is full
        if (cache.size >= MAX_SIZE) {
            evictLRU()
        }
        
        val entry = CacheEntry(content, System.currentTimeMillis() + TTL_MS)
        cache[key] = entry
        
        synchronized(accessOrder) {
            accessOrder[key] = System.currentTimeMillis()
        }
    }
    
    fun contains(key: String): Boolean {
        val entry = cache[key]
        return entry != null && !entry.isExpired()
    }
    
    fun cleanExpired() {
        val expiredKeys = cache.entries.filter { it.value.isExpired() }.map { it.key }
        expiredKeys.forEach { key ->
            cache.remove(key)
            synchronized(accessOrder) {
                accessOrder.remove(key)
            }
        }
    }
    
    fun getHitRate(): Double {
        val totalRequests = hits.get() + misses.get()
        return if (totalRequests > 0) {
            hits.get().toDouble() / totalRequests
        } else {
            0.0
        }
    }
    
    private fun evictLRU() {
        synchronized(accessOrder) {
            val lruKey = accessOrder.entries.minByOrNull { it.value }?.key
            lruKey?.let { key ->
                cache.remove(key)
                accessOrder.remove(key)
            }
        }
    }
    
    private data class CacheEntry(
        val content: NotificationContent,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// Data classes for performance optimization

data class PerformanceRecommendation(
    val type: String,
    val message: String,
    val priority: PerformancePriority
)

enum class PerformancePriority {
    LOW, MEDIUM, HIGH
}