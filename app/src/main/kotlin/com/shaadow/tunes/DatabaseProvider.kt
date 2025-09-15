package com.shaadow.tunes

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

/**
 * Thread-safe singleton provider for the database instance.
 * This class manages the lifecycle of the database and ensures proper initialization
 * without circular dependencies.
 */
object DatabaseProvider {
    @Volatile
    private var INSTANCE: Database? = null
    
    @Volatile
    private var ROOM_INSTANCE: DatabaseInitializer? = null
    
    /**
     * Gets the database instance, creating it if necessary.
     * This method is thread-safe and uses double-checked locking.
     */
    fun getInstance(context: Context): Database {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: createDatabase(context.applicationContext).also { 
                INSTANCE = it
            }
        }
    }
    
    /**
     * Gets the RoomDatabase instance for utility functions.
     * This method is thread-safe and uses double-checked locking.
     */
    internal fun getRoomDatabase(context: Context): DatabaseInitializer {
        return ROOM_INSTANCE ?: synchronized(this) {
            ROOM_INSTANCE ?: createRoomDatabase(context.applicationContext).also { 
                ROOM_INSTANCE = it
                INSTANCE = it.database
            }
        }
    }
    
    /**
     * Creates a new database instance with all necessary configurations.
     */
    private fun createDatabase(context: Context): Database {
        return getRoomDatabase(context).database
    }
    
    /**
     * Creates the RoomDatabase instance with all necessary configurations.
     */
    private fun createRoomDatabase(context: Context): DatabaseInitializer {
        return Room.databaseBuilder(context, DatabaseInitializer::class.java, "data.db")
            .addMigrations(
                DatabaseInitializer.From8To9Migration(),
                DatabaseInitializer.From10To11Migration(),
                DatabaseInitializer.From14To15Migration(),
                DatabaseInitializer.From22To23Migration(),
                PerformanceOptimizationMigration()
            )
            .addCallback(DatabaseOptimizationCallback())
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Clears the database instance. Used primarily for testing.
     */
    @Synchronized
    fun clearInstance() {
        INSTANCE = null
        ROOM_INSTANCE = null
    }

}

/**
 * Migration to add performance indexes and optimizations
 */
internal class PerformanceOptimizationMigration : androidx.room.migration.Migration(24, 25) {
    companion object {
        private const val TAG = "PerformanceMigration"
    }

    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d(TAG, "Applying performance optimization migration...")

        try {
            // Create indexes for frequently queried columns
            createPerformanceIndexes(db)

            // Optimize existing data
            optimizeExistingData(db)

            Log.d(TAG, "Performance optimization migration completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Performance optimization migration failed", e)
            throw e
        }
    }

    private fun createPerformanceIndexes(db: SupportSQLiteDatabase) {
        // Create indexes for frequently queried columns
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_song_title ON Song(title)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_song_artists ON Song(artistsText)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_song_total_play_time ON Song(totalPlayTimeMs)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_song_liked_at ON Song(likedAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_event_song_id ON Event(songId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_event_timestamp ON Event(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_format_song_id ON Format(songId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lyrics_song_id ON Lyrics(songId)")

        Log.d(TAG, "Performance indexes created successfully")
    }

    private fun optimizeExistingData(db: SupportSQLiteDatabase) {
        // Analyze tables for query optimization
        db.execSQL("ANALYZE Song")
        db.execSQL("ANALYZE Event")
        db.execSQL("ANALYZE Format")

        // Vacuum database to reclaim space
        db.execSQL("VACUUM")

        Log.d(TAG, "Database optimization completed")
    }
}

/**
 * Database callback for performance optimizations
 */
private class DatabaseOptimizationCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("DatabaseOptimization", "Database created, applying performance optimizations...")
        applyPerformanceOptimizations(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d("DatabaseOptimization", "Database opened, ensuring optimizations are applied...")
        applyPerformanceOptimizations(db)
    }

    private fun applyPerformanceOptimizations(db: SupportSQLiteDatabase) {
        try {
            // Enable Write-Ahead Logging for better concurrent read/write performance
            db.execSQL("PRAGMA journal_mode = WAL")

            // Set synchronous mode to NORMAL for better performance (still safe)
            db.execSQL("PRAGMA synchronous = NORMAL")

            // Increase cache size for better performance
            db.execSQL("PRAGMA cache_size = -2000") // 2MB cache

            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON")

            // Optimize for faster queries
            db.execSQL("PRAGMA temp_store = MEMORY")
            db.execSQL("PRAGMA mmap_size = 268435456") // 256MB memory map

            Log.d("DatabaseOptimization", "Performance optimizations applied successfully")
        } catch (e: Exception) {
            Log.e("DatabaseOptimization", "Failed to apply performance optimizations", e)
        }
    }
}