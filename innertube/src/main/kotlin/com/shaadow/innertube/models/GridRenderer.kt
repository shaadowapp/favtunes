package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class GridRenderer(
    val items: List<com.shaadow.innertube.models.GridRenderer.Item>?,
) {
    @Serializable
    data class Item(
        val musicTwoRowItemRenderer: com.shaadow.innertube.models.MusicTwoRowItemRenderer?
    )
}
