package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.innertube.Innertube

import com.shaadow.innertube.requests.relatedPage
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.QuickPicksSource
import com.shaadow.tunes.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class QuickPicksViewModel : ViewModel() {
    var trending: Song? by mutableStateOf(null)
    var relatedPageResult: Result<Innertube.RelatedPage?>? by mutableStateOf(null)
    var isLoading: Boolean by mutableStateOf(false)
    
    private var currentJob: Job? = null
    private val relatedPageCache = ConcurrentHashMap<String, Innertube.RelatedPage>()
    private val cacheExpiry = ConcurrentHashMap<String, Long>()
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes

    init {
        // Start loading immediately when ViewModel is created
        viewModelScope.launch {
            loadQuickPicks(QuickPicksSource.Trending)
        }
    }

    suspend fun loadQuickPicks(quickPicksSource: QuickPicksSource) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (relatedPageResult == null) {
                withContext(Dispatchers.Main) {
                    isLoading = true
                }
            }
            
            try {
                val flow = when (quickPicksSource) {
                    QuickPicksSource.Trending -> Database.trending()
                    QuickPicksSource.LastPlayed -> Database.lastPlayed()
                    QuickPicksSource.Random -> Database.randomSong()
                }

                // Use first() for faster initial load
                val song = flow.first()
                
                if (shouldFetchRelatedPage(song)) {
                    val songId = song?.id ?: "fJ9rUzIMcZQ"
                    relatedPageResult = getCachedOrFetchRelatedPage(songId)
                }

                withContext(Dispatchers.Main) {
                    trending = song
                }
                
                // Continue collecting for updates in background
                flow.distinctUntilChanged().drop(1).collect { updatedSong ->
                    if (quickPicksSource == QuickPicksSource.Random && updatedSong != null && trending != null) return@collect

                    if (shouldFetchRelatedPage(updatedSong)) {
                        val songId = updatedSong?.id ?: "fJ9rUzIMcZQ"
                        relatedPageResult = getCachedOrFetchRelatedPage(songId)
                    }

                    withContext(Dispatchers.Main) {
                        trending = updatedSong
                    }
                }
            } catch (e: Exception) {
                // Handle errors gracefully
                withContext(Dispatchers.Main) {
                    if (relatedPageResult == null) {
                        relatedPageResult = Result.failure(e)
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
    
    private fun shouldFetchRelatedPage(song: Song?): Boolean {
        return (song == null && relatedPageResult == null) || 
               trending?.id != song?.id || 
               relatedPageResult?.isSuccess != true
    }

    private suspend fun getCachedOrFetchRelatedPage(songId: String): Result<Innertube.RelatedPage?> {
        val now = System.currentTimeMillis()
        val cached = relatedPageCache[songId]
        val expiry = cacheExpiry[songId] ?: 0
        
        return if (cached != null && now < expiry) {
            Result.success(cached)
        } else {
            try {
                val result = Innertube.relatedPage(videoId = songId)
                result?.getOrNull()?.let { page ->
                    relatedPageCache[songId] = page
                    cacheExpiry[songId] = now + cacheTimeout
                }
                result ?: Result.failure(Exception("Failed to fetch related page"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        relatedPageCache.clear()
        cacheExpiry.clear()
    }
}