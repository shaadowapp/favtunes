package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: com.shaadow.innertube.models.NavigationEndpoint?,
    val thumbnailRenderer: ThumbnailRenderer?,
    val title: Runs?,
    val subtitle: Runs?,
)
