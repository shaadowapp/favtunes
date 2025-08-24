package com.shaadow.tunes.models

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val locale: String,
    val screenResolution: String,
    val availableMemory: Long,
    val networkType: String
) {
    companion object {
        fun empty() = DeviceInfo(
            deviceModel = "",
            osVersion = "",
            appVersion = "",
            locale = "",
            screenResolution = "",
            availableMemory = 0L,
            networkType = ""
        )
    }
}