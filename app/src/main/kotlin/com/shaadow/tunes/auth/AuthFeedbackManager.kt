package com.shaadow.tunes.auth

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Manages user feedback for authentication operations
 */
class AuthFeedbackManager(private val context: Context) {
    
    /**
     * Show authentication success message
     * @param userId User ID that was authenticated
     */
    fun showAuthenticationSuccess(userId: String) {
        val message = "Welcome back! Authentication successful."
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show authentication failure message
     * @param error Authentication error
     */
    fun showAuthenticationFailure(error: AuthError) {
        val message = when (error.type) {
            AuthErrorType.TOKEN_EXPIRED -> "Your session has expired. Please log in again."
            AuthErrorType.TOKEN_INVALID -> "Authentication failed. Please try logging in again."
            AuthErrorType.DEVICE_NOT_RECOGNIZED -> "This device is not recognized. Please verify your identity."
            AuthErrorType.NETWORK_ERROR -> "Network error. Please check your connection and try again."
            AuthErrorType.ENCRYPTION_ERROR -> "Security error occurred. Please try again."
            AuthErrorType.UNKNOWN_ERROR -> "An unexpected error occurred. Please try again."
        }
        
        showToast(message, Toast.LENGTH_LONG)
    }
    
    /**
     * Show token refresh notification
     * @param isSuccess Whether refresh was successful
     */
    fun showTokenRefreshFeedback(isSuccess: Boolean) {
        val message = if (isSuccess) {
            "Session refreshed successfully."
        } else {
            "Failed to refresh session. Please log in again."
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show sync code generation feedback
     * @param syncCode Generated sync code or null if failed
     */
    fun showSyncCodeFeedback(syncCode: String?) {
        val message = if (syncCode != null) {
            "Sync code generated. Share with your other device."
        } else {
            "Failed to generate sync code. Please try again."
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show sync import feedback
     * @param isSuccess Whether import was successful
     */
    fun showSyncImportFeedback(isSuccess: Boolean) {
        val message = if (isSuccess) {
            "Successfully synced with other device!"
        } else {
            "Failed to sync with other device. Please check the sync code."
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show logout feedback
     * @param isSuccess Whether logout was successful
     */
    fun showLogoutFeedback(isSuccess: Boolean) {
        val message = if (isSuccess) {
            "Logged out successfully."
        } else {
            "Logout may not have completed properly."
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show device token setup feedback
     * @param isFirstTime Whether this is first-time setup
     */
    fun showTokenSetupFeedback(isFirstTime: Boolean) {
        val message = if (isFirstTime) {
            "Device authentication set up successfully!"
        } else {
            "Authentication restored from previous session."
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Show authentication state change feedback
     * @param state New authentication state
     */
    fun showStateChangeFeedback(state: AuthenticationState) {
        val message = when (state) {
            AuthenticationState.AUTHENTICATING -> "Authenticating..."
            AuthenticationState.AUTHENTICATED -> "Authentication successful!"
            AuthenticationState.NOT_AUTHENTICATED -> "Please log in to continue."
            AuthenticationState.ERROR -> "Authentication error occurred."
            else -> return // Don't show feedback for other states
        }
        
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Get user-friendly error message
     * @param error Authentication error
     * @return User-friendly error message
     */
    fun getUserFriendlyErrorMessage(error: AuthError): String {
        return when (error.type) {
            AuthErrorType.TOKEN_EXPIRED -> {
                "Your session has expired. This happens for security after ${AuthConstants.DEFAULT_TOKEN_EXPIRATION_DAYS} days. Please log in again."
            }
            AuthErrorType.TOKEN_INVALID -> {
                "There was a problem with your authentication. Please try logging in again."
            }
            AuthErrorType.DEVICE_NOT_RECOGNIZED -> {
                "This device is not recognized in your account. This can happen if you've reset your device or restored from backup."
            }
            AuthErrorType.NETWORK_ERROR -> {
                "Unable to connect to authentication servers. Please check your internet connection and try again."
            }
            AuthErrorType.ENCRYPTION_ERROR -> {
                "A security error occurred while processing your authentication. Please try again."
            }
            AuthErrorType.UNKNOWN_ERROR -> {
                "An unexpected error occurred. If this continues, please contact support."
            }
        }
    }
    
    /**
     * Get recovery suggestions for errors
     * @param error Authentication error
     * @return List of recovery suggestions
     */
    fun getRecoverySuggestions(error: AuthError): List<String> {
        return when (error.type) {
            AuthErrorType.TOKEN_EXPIRED -> listOf(
                "Tap 'Login' to create a new session",
                "Your data and preferences are safe"
            )
            AuthErrorType.TOKEN_INVALID -> listOf(
                "Try logging out and logging back in",
                "Clear app data if problem persists"
            )
            AuthErrorType.DEVICE_NOT_RECOGNIZED -> listOf(
                "Use sync code from another device",
                "Or create a new account on this device"
            )
            AuthErrorType.NETWORK_ERROR -> listOf(
                "Check your internet connection",
                "Try switching between WiFi and mobile data",
                "Wait a moment and try again"
            )
            AuthErrorType.ENCRYPTION_ERROR -> listOf(
                "Restart the app",
                "Clear app cache if problem persists"
            )
            AuthErrorType.UNKNOWN_ERROR -> listOf(
                "Restart the app",
                "Check for app updates",
                "Contact support if problem continues"
            )
        }
    }
    
    /**
     * Show detailed error dialog (for implementation by UI layer)
     * @param error Authentication error
     * @return Error dialog data
     */
    fun createErrorDialogData(error: AuthError): ErrorDialogData {
        return ErrorDialogData(
            title = "Authentication Error",
            message = getUserFriendlyErrorMessage(error),
            suggestions = getRecoverySuggestions(error),
            isRecoverable = error.isRecoverable(),
            primaryAction = if (error.isRecoverable()) "Retry" else "OK",
            secondaryAction = if (error.type == AuthErrorType.DEVICE_NOT_RECOGNIZED) "Use Sync Code" else null
        )
    }
    
    /**
     * Show toast message
     */
    private fun showToast(message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}

/**
 * Data for error dialog display
 */
data class ErrorDialogData(
    val title: String,
    val message: String,
    val suggestions: List<String>,
    val isRecoverable: Boolean,
    val primaryAction: String,
    val secondaryAction: String? = null
)