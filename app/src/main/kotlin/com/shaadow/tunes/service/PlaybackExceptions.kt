package com.shaadow.tunes.service

import androidx.media3.common.PlaybackException
import com.shaadow.tunes.utils.AdvancedRemoteConfig
import com.shaadow.tunes.utils.EmergencyConfigTrigger
import android.util.Log

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlayableFormatNotFoundException(customMessage: String? = null) :
    PlaybackException(
        customMessage ?: AdvancedRemoteConfig.getCustomErrorMessage("PlayableFormatNotFound") ?: "Playable format not found", 
        null, 
        ERROR_CODE_REMOTE_ERROR
    ) {
    init {
        if (AdvancedRemoteConfig.shouldLogErrors()) {
            Log.w("PlaybackExceptions", "PlayableFormatNotFoundException: $message")
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class UnplayableException(customMessage: String? = null, val videoId: String? = null) :
    PlaybackException(
        customMessage ?: AdvancedRemoteConfig.getCustomErrorMessage("Unplayable") ?: "Unplayable", 
        null, 
        ERROR_CODE_REMOTE_ERROR
    ) {
    init {
        if (AdvancedRemoteConfig.shouldLogErrors()) {
            Log.w("PlaybackExceptions", "UnplayableException: $message")
        }
        
        // Report to emergency trigger system
        videoId?.let { id ->
            EmergencyConfigTrigger.reportUnplayableException(id)
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class LoginRequiredException(customMessage: String? = null, val videoId: String? = null) :
    PlaybackException(
        customMessage ?: AdvancedRemoteConfig.getCustomErrorMessage("LoginRequired") ?: "Login required", 
        null, 
        ERROR_CODE_REMOTE_ERROR
    ) {
    init {
        if (AdvancedRemoteConfig.shouldLogErrors()) {
            Log.w("PlaybackExceptions", "LoginRequiredException: $message")
        }
        
        // Report to emergency trigger system
        videoId?.let { id ->
            EmergencyConfigTrigger.reportLoginRequired(id)
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoIdMismatchException(customMessage: String? = null, val videoId: String? = null) :
    PlaybackException(
        customMessage ?: AdvancedRemoteConfig.getCustomErrorMessage("VideoIdMismatch") ?: "Video id mismatch", 
        null, 
        ERROR_CODE_REMOTE_ERROR
    ) {
    init {
        if (AdvancedRemoteConfig.shouldLogErrors()) {
            Log.w("PlaybackExceptions", "VideoIdMismatchException: $message")
        }
        
        // Report to emergency trigger system
        videoId?.let { id ->
            EmergencyConfigTrigger.reportVideoIdMismatch(id)
        }
    }
}

/**
 * Enhanced error handler that uses remote config for all error handling decisions
 */
object RemoteConfigErrorHandler {
    private const val TAG = "RemoteConfigErrorHandler"
    
    /**
     * Determines if an error should be retried based on remote config
     */
    fun shouldRetryError(exception: Throwable, currentRetryCount: Int): Boolean {
        val errorType = when (exception) {
            is VideoIdMismatchException -> "VideoIdMismatch"
            is PlayableFormatNotFoundException -> "PlayableFormatNotFound"
            is UnplayableException -> "Unplayable"
            is LoginRequiredException -> "LoginRequired"
            else -> "Generic"
        }
        
        val shouldRetry = AdvancedRemoteConfig.shouldRetryError(errorType)
        val maxRetries = AdvancedRemoteConfig.getMaxRetries(errorType)
        
        val result = shouldRetry && currentRetryCount < maxRetries
        
        if (AdvancedRemoteConfig.isLoggingEnabled()) {
            Log.d(TAG, "Error: $errorType, Retry: $result, Count: $currentRetryCount/$maxRetries")
        }
        
        return result
    }
    
    /**
     * Gets retry delay for specific error type
     */
    fun getRetryDelay(exception: Throwable): Long {
        val errorType = when (exception) {
            is VideoIdMismatchException -> "VideoIdMismatch"
            is PlayableFormatNotFoundException -> "PlayableFormatNotFound"
            is UnplayableException -> "Unplayable"
            is LoginRequiredException -> "LoginRequired"
            else -> "Generic"
        }
        
        return AdvancedRemoteConfig.getRetryDelay(errorType)
    }
    
    /**
     * Determines if song should be skipped after all retries failed
     */
    fun shouldSkipOnFailure(exception: Throwable): Boolean {
        val errorType = when (exception) {
            is VideoIdMismatchException -> "VideoIdMismatch"
            is PlayableFormatNotFoundException -> "PlayableFormatNotFound"
            is UnplayableException -> "Unplayable"
            is LoginRequiredException -> "LoginRequired"
            else -> "Generic"
        }
        
        return AdvancedRemoteConfig.shouldSkipOnFailure(errorType)
    }
    
    /**
     * Handles error with remote config logic
     */
    suspend fun handleError(
        exception: Throwable, 
        videoId: String, 
        currentRetryCount: Int,
        onRetry: suspend (Long) -> Unit,
        onSkip: () -> Unit,
        onFail: (Throwable) -> Unit
    ) {
        if (AdvancedRemoteConfig.isLoggingEnabled()) {
            Log.d(TAG, "Handling error for video $videoId: ${exception.message}")
        }
        
        if (shouldRetryError(exception, currentRetryCount)) {
            val delay = getRetryDelay(exception)
            if (AdvancedRemoteConfig.isLoggingEnabled()) {
                Log.d(TAG, "Retrying in ${delay}ms for video $videoId")
            }
            onRetry(delay)
        } else if (shouldSkipOnFailure(exception)) {
            if (AdvancedRemoteConfig.isLoggingEnabled()) {
                Log.d(TAG, "Skipping failed video $videoId")
            }
            onSkip()
        } else {
            if (AdvancedRemoteConfig.isLoggingEnabled()) {
                Log.d(TAG, "Failing video $videoId")
            }
            onFail(exception)
        }
    }
}