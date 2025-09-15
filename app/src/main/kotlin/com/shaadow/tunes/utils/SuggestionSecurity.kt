package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Security utility to protect suggestion system data from user manipulation
 * This ensures the integrity of the recommendation engine
 */
object SuggestionSecurity {
    
    private const val PREFS_NAME = "secure_suggestions"
    private const val KEY_ALIAS = "suggestion_master_key"
    
    /**
     * Get encrypted shared preferences for storing sensitive suggestion data
     */
    fun getSecurePreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Generate a secure hash for data integrity verification
     */
    fun generateDataHash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify data integrity using hash comparison
     */
    fun verifyDataIntegrity(data: String, expectedHash: String): Boolean {
        val actualHash = generateDataHash(data)
        return actualHash == expectedHash
    }
    
    /**
     * Store sensitive suggestion data with integrity protection
     */
    fun storeSecureData(
        context: Context,
        key: String,
        value: String
    ): Boolean {
        return try {
            val prefs = getSecurePreferences(context)
            val hash = generateDataHash(value)
            
            prefs.edit()
                .putString(key, value)
                .putString("${key}_hash", hash)
                .putLong("${key}_timestamp", System.currentTimeMillis())
                .apply()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieve and verify secure suggestion data
     */
    fun getSecureData(
        context: Context,
        key: String
    ): String? {
        return try {
            val prefs = getSecurePreferences(context)
            val value = prefs.getString(key, null) ?: return null
            val storedHash = prefs.getString("${key}_hash", null) ?: return null
            
            if (verifyDataIntegrity(value, storedHash)) {
                value
            } else {
                // Data has been tampered with, return null
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if suggestion data has been tampered with
     */
    fun isDataTampered(context: Context, key: String): Boolean {
        return try {
            val prefs = getSecurePreferences(context)
            val value = prefs.getString(key, null) ?: return false
            val storedHash = prefs.getString("${key}_hash", null) ?: return false
            
            !verifyDataIntegrity(value, storedHash)
        } catch (e: Exception) {
            true // Assume tampered if we can't verify
        }
    }
    
    /**
     * Secure the tracking data to prevent manipulation
     */
    fun secureTrackingData(
        context: Context,
        songId: String,
        playCount: Int,
        skipCount: Int,
        liked: Boolean
    ): Boolean {
        val trackingData = "$songId:$playCount:$skipCount:$liked:${System.currentTimeMillis()}"
        return storeSecureData(context, "track_$songId", trackingData)
    }
    
    /**
     * Get secure tracking data for a song
     */
    fun getSecureTrackingData(
        context: Context,
        songId: String
    ): Map<String, Any>? {
        val data = getSecureData(context, "track_$songId") ?: return null
        val parts = data.split(":")
        
        return if (parts.size >= 4) {
            mapOf<String, Any>(
                "songId" to parts[0],
                "playCount" to (parts[1].toIntOrNull() ?: 0),
                "skipCount" to (parts[2].toIntOrNull() ?: 0),
                "liked" to (parts[3].toBooleanStrictOrNull() ?: false),
                "timestamp" to (parts.getOrNull(4)?.toLongOrNull() ?: 0L)
            )
        } else null
    }
    
    /**
     * Validate that user preferences haven't been artificially inflated
     */
    fun validatePreferences(preferences: Set<String>): Set<String> {
        // Limit to reasonable number of preferences to prevent gaming
        val maxPreferences = 10
        return if (preferences.size > maxPreferences) {
            preferences.take(maxPreferences).toSet()
        } else {
            preferences
        }
    }
    
    /**
     * Rate limit preference updates to prevent rapid manipulation
     */
    fun canUpdatePreferences(context: Context): Boolean {
        val prefs = getSecurePreferences(context)
        val lastUpdate = prefs.getLong("last_pref_update", 0)
        val cooldownPeriod = 5 * 60 * 1000L // 5 minutes
        
        return (System.currentTimeMillis() - lastUpdate) > cooldownPeriod
    }
    
    /**
     * Record preference update timestamp
     */
    fun recordPreferenceUpdate(context: Context) {
        val prefs = getSecurePreferences(context)
        prefs.edit()
            .putLong("last_pref_update", System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Clear all secure data (for reset functionality)
     */
    fun clearSecureData(context: Context): Boolean {
        return try {
            val prefs = getSecurePreferences(context)
            prefs.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get data integrity report
     */
    fun getIntegrityReport(context: Context): Map<String, Any> {
        val prefs = getSecurePreferences(context)
        val allKeys = prefs.all.keys.filter { !it.endsWith("_hash") && !it.endsWith("_timestamp") }
        
        var tamperedCount = 0
        var validCount = 0
        
        allKeys.forEach { key ->
            if (isDataTampered(context, key)) {
                tamperedCount++
            } else {
                validCount++
            }
        }
        
        return mapOf(
            "totalEntries" to allKeys.size,
            "validEntries" to validCount,
            "tamperedEntries" to tamperedCount,
            "integrityScore" to if (allKeys.isNotEmpty()) (validCount.toFloat() / allKeys.size.toFloat()) else 1.0f
        )
    }
}