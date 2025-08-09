package com.shaadow.innertube.requests

import io.ktor.http.Url
import com.shaadow.innertube.Innertube


suspend fun Innertube.albumPage(browseId: String, params: String? = null): Result<Innertube.PlaylistOrAlbumPage>? {
    return playlistPage(browseId = browseId, params = params)?.map { album ->
        album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
            playlistPage(browseId = "VL$playlistId")?.getOrNull()?.let { playlist ->
                album.copy(songsPage = playlist.songsPage)
            }
        } ?: album
    }?.map { album ->
        val albumInfo = Innertube.Info(
            name = album.title,
            endpoint = _root_ide_package_.com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Browse(
                browseId = browseId,
                params = params
            )
        )

        album.copy(
            songsPage = album.songsPage?.copy(
                items = album.songsPage.items?.map { song ->
                    song.copy(
                        authors = song.authors ?: album.authors,
                        album = albumInfo,
                        thumbnail = album.thumbnail
                    )
                }
            )
        )
    }
}
