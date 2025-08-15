package com.shaadow.tunes.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import com.shaadow.tunes.Database
import com.shaadow.tunes.DatabaseProvider

/**
 * Extension functions and utilities for database access in ViewModels and other components.
 */

/**
 * Gets the database instance from the application context.
 * This is the preferred way to access the database in ViewModels.
 */
fun Context.getDatabase(): Database {
    return DatabaseProvider.getInstance(this.applicationContext)
}

/**
 * Helper class for ViewModels that need database access.
 * ViewModels can extend this class or use it as a delegate.
 * Note: This should only be used with Application context to avoid memory leaks.
 */
abstract class DatabaseViewModel(application: android.app.Application) : ViewModel() {
    protected val database: Database by lazy {
        application.getDatabase()
    }
}

/**
 * Extension function to safely access database operations with error handling.
 */
inline fun <T> Database.safeOperation(operation: Database.() -> T): Result<T> {
    return try {
        Result.success(operation())
    } catch (e: Exception) {
        Result.failure(e)
    }
}