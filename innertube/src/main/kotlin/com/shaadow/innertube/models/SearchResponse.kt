package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val contents: com.shaadow.innertube.models.SearchResponse.Contents?,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: com.shaadow.innertube.models.Tabs?
    )
}
