package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.bodies.NextBody
import com.shaadow.innertube.requests.relatedPage
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.QuickPicksSource
import com.shaadow.tunes.models.Song
import kotlinx.coroutines.flow.distinctUntilChanged

class QuickPicksViewModel : ViewModel() {
    var trending: Song? by mutableStateOf(null)
    var relatedPageResult: Result<Innertube.RelatedPage?>? by mutableStateOf(null)

    suspend fun loadQuickPicks(quickPicksSource: QuickPicksSource) {
        val flow = when (quickPicksSource) {
            QuickPicksSource.Trending -> Database.trending()
            QuickPicksSource.LastPlayed -> Database.lastPlayed()
            QuickPicksSource.Random -> Database.randomSong()
        }

        flow.distinctUntilChanged().collect { song ->
            if (quickPicksSource == QuickPicksSource.Random && song != null && trending != null) return@collect

            if ((song == null && relatedPageResult == null) || trending?.id != song?.id || relatedPageResult?.isSuccess != true) {
                relatedPageResult =
                    Innertube.relatedPage(NextBody(videoId = (song?.id ?: "fJ9rUzIMcZQ")))
            }

            trending = song
        }
    }
}