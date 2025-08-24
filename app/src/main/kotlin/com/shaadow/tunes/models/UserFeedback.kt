package com.shaadow.tunes.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserFeedback(
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val rating: Int, // 1-5 stars
    val category: FeedbackCategory,
    val message: String,
    val deviceInfo: DeviceInfo,
    val appVersion: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAnonymous: Boolean = true
) {
    fun isValid(): Boolean {
        return rating in 1..5 && 
               message.isNotBlank() && 
               appVersion.isNotBlank()
    }
    
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to (if (isAnonymous) "" else (userId ?: "")),
            "rating" to rating,
            "category" to category.name,
            "message" to message,
            "deviceInfo" to mapOf(
                "deviceModel" to deviceInfo.deviceModel,
                "osVersion" to deviceInfo.osVersion,
                "appVersion" to deviceInfo.appVersion,
                "locale" to deviceInfo.locale,
                "screenResolution" to deviceInfo.screenResolution,
                "availableMemory" to deviceInfo.availableMemory,
                "networkType" to deviceInfo.networkType
            ),
            "appVersion" to appVersion,
            "timestamp" to timestamp,
            "isAnonymous" to isAnonymous
        )
    }
}