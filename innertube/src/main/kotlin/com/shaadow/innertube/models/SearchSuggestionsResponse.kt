package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsResponse(
    val contents: List<com.shaadow.innertube.models.SearchSuggestionsResponse.Content>?
) {
    @Serializable
    data class Content(
        val searchSuggestionsSectionRenderer: com.shaadow.innertube.models.SearchSuggestionsResponse.Content.SearchSuggestionsSectionRenderer?
    ) {
        @Serializable
        data class SearchSuggestionsSectionRenderer(
            val contents: List<com.shaadow.innertube.models.SearchSuggestionsResponse.Content.SearchSuggestionsSectionRenderer.Content>?
        ) {
            @Serializable
            data class Content(
                val searchSuggestionRenderer: com.shaadow.innertube.models.SearchSuggestionsResponse.Content.SearchSuggestionsSectionRenderer.Content.SearchSuggestionRenderer?
            ) {
                @Serializable
                data class SearchSuggestionRenderer(
                    val navigationEndpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint?,
                )
            }
        }
    }
}
