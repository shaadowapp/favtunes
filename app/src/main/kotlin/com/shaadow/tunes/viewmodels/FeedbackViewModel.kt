package com.shaadow.tunes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.tunes.models.*
import com.shaadow.tunes.repository.RepositoryProvider
import com.shaadow.tunes.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RepositoryProvider.getBugReportRepository(application)
    // DeviceInfoCollector is an object, not a class
    private val rateLimitManager = RateLimitManager(application)
    private val validationUtils = ValidationUtils
    
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "FeedbackViewModel"
    }
    
    init {
        // Initialize with device info
        viewModelScope.launch {
            val deviceInfo = BugReportDeviceInfoCollector(getApplication()).collectDeviceInfo(getAppVersion())
            _uiState.value = _uiState.value.copy(deviceInfo = deviceInfo)
        }
    }
    
    /**
     * Update the feedback rating
     */
    fun updateRating(rating: Int) {
        if (rating in 1..5) {
            _uiState.value = _uiState.value.copy(
                rating = rating,
                ratingError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                ratingError = "Rating must be between 1 and 5"
            )
        }
    }
    
    /**
     * Update the feedback category
     */
    fun updateCategory(category: FeedbackCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    /**
     * Update the feedback message
     */
    fun updateMessage(message: String) {
        // Temporarily bypass sanitization to test space issue
        Log.d(TAG, "updateMessage input: '$message'")
        Log.d(TAG, "updateMessage contains spaces: ${message.contains(' ')}")
        
        val sanitizedMessage = message // validationUtils.sanitizeText(message)
        // Skip validation temporarily
        
        _uiState.value = _uiState.value.copy(
            message = sanitizedMessage,
            messageError = null
        )
    }
    
    /**
     * Update the optional email field
     */
    fun updateEmail(email: String) {
        val trimmedEmail = email.trim()
        val emailError = if (trimmedEmail.isNotEmpty() && !validationUtils.isValidEmail(trimmedEmail)) {
            "Please enter a valid email address"
        } else null
        
        _uiState.value = _uiState.value.copy(
            email = if (trimmedEmail.isEmpty()) null else trimmedEmail,
            emailError = emailError
        )
    }
    
    /**
     * Submit the feedback
     */
    fun submitFeedback() {
        viewModelScope.launch {
            try {
                // Check rate limit
                val rateLimitResult = rateLimitManager.canSubmitFeedback()
                if (!rateLimitResult.allowed) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = rateLimitResult.reason ?: "Rate limit exceeded"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentState = _uiState.value
                val feedback = UserFeedback(
                    rating = currentState.rating,
                    category = currentState.category,
                    message = currentState.message,
                    email = currentState.email,
                    deviceInfo = currentState.deviceInfo,
                    appVersion = getAppVersion()
                )
                
                // Validate the complete feedback
                val validation = validationUtils.validateUserFeedback(feedback)
                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validation.errorMessage
                    )
                    return@launch
                }
                
                // Sanitize the feedback
                val sanitizedFeedback = validationUtils.sanitizeUserFeedback(feedback)
                
                // Submit the feedback
                val result = repository.submitFeedback(sanitizedFeedback)
                
                if (result.isSuccess) {
                    rateLimitManager.recordFeedbackSubmission()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitted = true,
                        submissionId = result.getOrNull()
                    )
                    Log.d(TAG, "Feedback submitted successfully: ${result.getOrNull()}")
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = FirestoreHelper.getErrorMessage((error as? Exception) ?: Exception("Unknown error: ${error?.message}"))
                    )
                    Log.e(TAG, "Failed to submit feedback", error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
                Log.e(TAG, "Unexpected error submitting feedback", e)
            }
        }
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Reset the form to initial state
     */
    fun resetForm() {
        viewModelScope.launch {
            val deviceInfo = BugReportDeviceInfoCollector(getApplication()).collectDeviceInfo(getAppVersion())
            _uiState.value = FeedbackUiState(deviceInfo = deviceInfo)
        }
    }
    
    /**
     * Check if the form is valid for submission
     */
    fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.rating in 1..5 &&
                state.message.isNotBlank() &&
                state.ratingError == null &&
                state.messageError == null &&
                state.emailError == null
    }
    
    /**
     * Get feedback categories for display
     */
    fun getFeedbackCategories(): List<FeedbackCategory> {
        return FeedbackCategory.values().toList()
    }
    
    /**
     * Get rating descriptions
     */
    fun getRatingDescription(rating: Int): String {
        return when (rating) {
            1 -> "Very Poor"
            2 -> "Poor"
            3 -> "Average"
            4 -> "Good"
            5 -> "Excellent"
            else -> ""
        }
    }
    
    /**
     * Get the current app version
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = getApplication<Application>().packageManager
                .getPackageInfo(getApplication<Application>().packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get app version", e)
            "Unknown"
        }
    }
}

/**
 * UI state for the feedback screen
 */
data class FeedbackUiState(
    val rating: Int = 0,
    val category: FeedbackCategory = FeedbackCategory.GENERAL,
    val message: String = "",
    val email: String? = null,
    val deviceInfo: DeviceInfo = DeviceInfo("", "", "", "", "", 0, ""),
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val submissionId: String? = null,
    val error: String? = null,
    val ratingError: String? = null,
    val messageError: String? = null,
    val emailError: String? = null
)