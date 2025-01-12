package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPanelVideoRenderer(
    val title: Runs?,
    val longBylineText: Runs?,
    val shortBylineText: Runs?,
    val lengthText: Runs?,
    val navigationEndpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint?,
    val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail?,
)
