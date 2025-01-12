package com.shaadow.innertube.models

import kotlinx.serialization.Serializable

/**
 * watchPlaylistEndpoint: params, playlistId
 * watchEndpoint: params, playlistId, videoId, index
 * browseEndpoint: params, browseId
 * searchEndpoint: params, query
 */
//@Serializable
//data class NavigationEndpoint(
//    @JsonNames("watchEndpoint", "watchPlaylistEndpoint", "navigationEndpoint", "browseEndpoint", "searchEndpoint")
//    val endpoint: Endpoint
//) {
//    @Serializable
//    data class Endpoint(
//        val params: String?,
//        val playlistId: String?,
//        val videoId: String?,
//        val index: Int?,
//        val browseId: String?,
//        val query: String?,
//        val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs?,
//        val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
//    ) {
//        @Serializable
//        data class WatchEndpointMusicSupportedConfigs(
//            val watchEndpointMusicConfig: WatchEndpointMusicConfig
//        ) {
//            @Serializable
//            data class WatchEndpointMusicConfig(
//                val musicVideoType: String
//            )
//        }
//
//        @Serializable
//        data class BrowseEndpointContextSupportedConfigs(
//            val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
//        ) {
//            @Serializable
//            data class BrowseEndpointContextMusicConfig(
//                val pageType: String
//            )
//        }
//    }
//}

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch?,
    val watchPlaylistEndpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.WatchPlaylist?,
    val browseEndpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Browse?,
    val searchEndpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Search?,
) {
    val endpoint: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint?
        get() = watchEndpoint ?: browseEndpoint ?: watchPlaylistEndpoint ?: searchEndpoint

    @Serializable
    sealed class Endpoint {
        @Serializable
        data class Watch(
            val params: String? = null,
            val playlistId: String? = null,
            val videoId: String? = null,
            val index: Int? = null,
            val playlistSetVideoId: String? = null,
            val watchEndpointMusicSupportedConfigs: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch.WatchEndpointMusicSupportedConfigs? = null,
        ) : _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint() {
            val type: String?
                get() = watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType

            @Serializable
            data class WatchEndpointMusicSupportedConfigs(
                val watchEndpointMusicConfig: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig?
            ) {

                @Serializable
                data class WatchEndpointMusicConfig(
                    val musicVideoType: String?
                )
            }
        }

        @Serializable
        data class WatchPlaylist(
            val params: String?,
            val playlistId: String?,
        ) : _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint()

        @Serializable
        data class Browse(
            val params: String? = null,
            val browseId: String? = null,
            val browseEndpointContextSupportedConfigs: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Browse.BrowseEndpointContextSupportedConfigs? = null,
        ) : _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint() {
            val type: String?
                get() = browseEndpointContextSupportedConfigs
                    ?.browseEndpointContextMusicConfig
                    ?.pageType

            @Serializable
            data class BrowseEndpointContextSupportedConfigs(
                val browseEndpointContextMusicConfig: _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Browse.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig
            ) {

                @Serializable
                data class BrowseEndpointContextMusicConfig(
                    val pageType: String
                )
            }
        }

        @Serializable
        data class Search(
            val params: String?,
            val query: String,
        ) : _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint()
    }
}

//@Serializable(with = NavigationEndpoint.Serializer::class)
//sealed class NavigationEndpoint {
//    @Serializable
//    data class Watch(
//        val watchEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val playlistId: String,
//            val videoId: String,
////            val index: Int?
//            val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs,
//        )
//
//        @Serializable
//        data class WatchEndpointMusicSupportedConfigs(
//            val watchEndpointMusicConfig: WatchEndpointMusicConfig
//        ) {
//            @Serializable
//            data class WatchEndpointMusicConfig(
//                val musicVideoType: String
//            )
//        }
//    }
//
//    @Serializable
//    data class WatchPlaylist(
//        val watchPlaylistEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val playlistId: String,
//        )
//    }
//
//    @Serializable
//    data class Browse(
//        val browseEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val browseId: String,
//            val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs,
//        )
//
//        @Serializable
//        data class BrowseEndpointContextSupportedConfigs(
//            val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
//        ) {
//            @Serializable
//            data class BrowseEndpointContextMusicConfig(
//                val pageType: String
//            )
//        }
//    }
//
//    @Serializable
//    data class Search(
//        val searchEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val query: String,
//        )
//    }
//
//    object Serializer : JsonContentPolymorphicSerializer<NavigationEndpoint>(NavigationEndpoint::class) {
//        override fun selectDeserializer(element: JsonElement) = when {
//            "watchEndpoint" in element.jsonObject -> Watch.serializer()
//            "watchPlaylistEndpoint" in element.jsonObject -> WatchPlaylist.serializer()
//            "browseEndpoint" in element.jsonObject -> Browse.serializer()
//            "searchEndpoint" in element.jsonObject -> Search.serializer()
//            else -> TODO()
//        }
//    }
//}
