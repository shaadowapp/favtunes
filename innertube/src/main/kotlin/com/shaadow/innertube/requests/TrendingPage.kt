package com.shaadow.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.BrowseResponse
import com.shaadow.innertube.models.MusicCarouselShelfRenderer
import com.shaadow.innertube.models.MusicShelfRenderer
import com.shaadow.innertube.models.bodies.BrowseBody
import com.shaadow.innertube.models.Context
import com.shaadow.innertube.models.YouTubeClient
import com.shaadow.innertube.utils.findSectionByTitle
import com.shaadow.innertube.utils.from
import com.shaadow.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.languageBasedContent(languageCode: String = "en") = runCatchingNonCancellable {
    val context = YouTubeClient.WEB_REMIX.toContext().copy(
        client = YouTubeClient.WEB_REMIX.toContext().client.copy(
            hl = languageCode
        )
    )
    
    val response = client.post(BROWSE) {
        setBody(BrowseBody(
            context = context,
            browseId = "FEmusic_home"
        ))
        mask("contents.sectionListRenderer.contents.musicCarouselShelfRenderer(header.musicCarouselShelfBasicHeaderRenderer(title,strapline),contents($MUSIC_RESPONSIVE_LIST_ITEM_RENDERER_MASK,$MUSIC_TWO_ROW_ITEM_RENDERER_MASK))")
    }.body<BrowseResponse>()

    val sectionListRenderer = response.contents?.sectionListRenderer

    Innertube.RelatedPage(
        songs = sectionListRenderer
            ?.contents
            ?.firstOrNull()?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull { it.musicResponsiveListItemRenderer }
            ?.mapNotNull(Innertube.SongItem::from)
            ?.take(20),
        playlists = sectionListRenderer
            ?.contents
            ?.find { it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text?.contains("playlist", ignoreCase = true) == true }
            ?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
            ?.mapNotNull(Innertube.PlaylistItem::from),
        albums = sectionListRenderer
            ?.contents
            ?.find { it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text?.contains("album", ignoreCase = true) == true }
            ?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
            ?.mapNotNull(Innertube.AlbumItem::from),
        artists = sectionListRenderer
            ?.contents
            ?.find { it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text?.contains("artist", ignoreCase = true) == true }
            ?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
            ?.mapNotNull(Innertube.ArtistItem::from)
    )
}