package com.shaadow.tunes.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BugReport(
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val title: String,
    val description: String,
    val severity: BugSeverity,
    val category: BugCategory,
    val deviceInfo: DeviceInfo,
    val appVersion: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<String> = emptyList(),
    val status: BugStatus = BugStatus.OPEN,
    val reproductionSteps: List<String> = emptyList()
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               description.isNotBlank() && 
               appVersion.isNotBlank()
    }
    
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to (userId ?: ""),
            "title" to title,
            "description" to description,
            "severity" to severity.name,
            "category" to category.name,
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
            "attachments" to attachments,
            "status" to status.name,
            "reproductionSteps" to reproductionSteps
        )
    }
}