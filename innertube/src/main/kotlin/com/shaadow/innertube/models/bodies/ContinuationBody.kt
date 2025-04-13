package com.shaadow.innertube.models.bodies

import com.shaadow.innertube.models.Context
import com.shaadow.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = YouTubeClient.WEB_REMIX.toContext(),
    val continuation: String,
)
