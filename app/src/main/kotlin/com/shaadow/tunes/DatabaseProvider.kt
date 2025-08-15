package com.shaadow.tunes

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase

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
                DatabaseInitializer.From22To23Migration()
            )
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