package com.shaadow.tunes.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.shaadow.tunes.Database
import com.shaadow.tunes.models.Song
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ListeningDataExport(
    val exportDate: String,
    val totalSongs: Int,
    val likedSongs: List<ExportedSong>,
    val recentlyPlayed: List<ExportedSong>,
    val listeningEvents: Int,
    val preferences: List<String>
)

@Serializable
data class ExportedSong(
    val id: String,
    val title: String,
    val artist: String?,
    val totalPlayTime: String,
    val likedAt: String?
)

object DataExporter {
    
    suspend fun exportListeningData(
        context: Context,
        format: String,
        preferences: Set<String>
    ): Intent? {
        return try {
            val likedSongs = Database.favorites().first()
            val recentSongs = Database.recentlyPlayedSongs().first()
            val totalEvents = Database.eventsCount().first()
            val allSongs = Database.songs(
                com.shaadow.tunes.enums.SongSortBy.DateAdded,
                com.shaadow.tunes.enums.SortOrder.Descending
            ).first()
            
            val exportData = ListeningDataExport(
                exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                totalSongs = allSongs.size,
                likedSongs = likedSongs.map { it.toExportedSong() },
                recentlyPlayed = recentSongs.map { it.toExportedSong() },
                listeningEvents = totalEvents,
                preferences = preferences.toList()
            )
            
            val fileName = "tunes_listening_data_${System.currentTimeMillis()}"
            val file = when (format.uppercase()) {
                "JSON" -> createJsonFile(context, exportData, fileName)
                "CSV" -> createCsvFile(context, exportData, fileName)
                else -> null
            }
            
            file?.let { createShareIntent(context, it) }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun Song.toExportedSong(): ExportedSong {
        return ExportedSong(
            id = id,
            title = title,
            artist = artistsText,
            totalPlayTime = formattedTotalPlayTime,
            likedAt = likedAt?.let { 
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it))
            }
        )
    }
    
    private fun createJsonFile(
        context: Context,
        data: ListeningDataExport,
        fileName: String
    ): File {
        val file = File(context.cacheDir, "$fileName.json")
        val json = Json { prettyPrint = true }
        file.writeText(json.encodeToString(data))
        return file
    }
    
    private fun createCsvFile(
        context: Context,
        data: ListeningDataExport,
        fileName: String
    ): File {
        val file = File(context.cacheDir, "$fileName.csv")
        val csv = buildString {
            // Header
            appendLine("Export Date,${data.exportDate}")
            appendLine("Total Songs,${data.totalSongs}")
            appendLine("Listening Events,${data.listeningEvents}")
            appendLine("Preferences,\"${data.preferences.joinToString(", ")}\"")
            appendLine()
            
            // Liked Songs
            appendLine("LIKED SONGS")
            appendLine("Title,Artist,Total Play Time,Liked At")
            data.likedSongs.forEach { song ->
                appendLine("\"${song.title}\",\"${song.artist ?: ""}\",\"${song.totalPlayTime}\",\"${song.likedAt ?: ""}\"")
            }
            appendLine()
            
            // Recently Played
            appendLine("RECENTLY PLAYED")
            appendLine("Title,Artist,Total Play Time")
            data.recentlyPlayed.forEach { song ->
                appendLine("\"${song.title}\",\"${song.artist ?: ""}\",\"${song.totalPlayTime}\"")
            }
        }
        file.writeText(csv)
        return file
    }
    
    private fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension) {
                "json" -> "application/json"
                "csv" -> "text/csv"
                else -> "text/plain"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Tunes Listening Data Export")
            putExtra(Intent.EXTRA_TEXT, "Your music listening data from Tunes app")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}