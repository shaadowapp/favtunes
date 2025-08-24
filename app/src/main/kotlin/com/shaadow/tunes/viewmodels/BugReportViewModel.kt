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

class BugReportViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RepositoryProvider.getBugReportRepository(application)
    // DeviceInfoCollector is an object, not a class
    private val rateLimitManager = RateLimitManager(application)
    private val validationUtils = ValidationUtils
    
    private val _uiState = MutableStateFlow(BugReportUiState())
    val uiState: StateFlow<BugReportUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "BugReportViewModel"
    }
    
    init {
        // Initialize with device info
        viewModelScope.launch {
            val deviceInfo = BugReportDeviceInfoCollector(getApplication()).collectDeviceInfo(getAppVersion())
            _uiState.value = _uiState.value.copy(deviceInfo = deviceInfo)
        }
    }
    
    /**
     * Update the bug report title
     */
    fun updateTitle(title: String) {
        // Temporarily bypass sanitization to test space issue
        Log.d(TAG, "updateTitle input: '$title'")
        Log.d(TAG, "updateTitle contains spaces: ${title.contains(' ')}")
        
        val sanitizedTitle = title // validationUtils.sanitizeText(title)
        // Skip validation temporarily
        
        _uiState.value = _uiState.value.copy(
            title = sanitizedTitle,
            titleError = null
        )
    }
    
    /**
     * Update the bug report description
     */
    fun updateDescription(description: String) {
        // Log the input to debug space issues
        Log.d(TAG, "updateDescription input: '$description'")
        Log.d(TAG, "updateDescription input length: ${description.length}")
        Log.d(TAG, "updateDescription contains spaces: ${description.contains(' ')}")
        
        // Temporarily bypass sanitization to test space issue
        val sanitizedDescription = description
        Log.d(TAG, "updateDescription sanitized: '$sanitizedDescription'")
        
        // Skip validation temporarily to isolate the space issue
        _uiState.value = _uiState.value.copy(
            description = sanitizedDescription,
            descriptionError = null
        )
    }
    
    /**
     * Update the bug severity
     */
    fun updateSeverity(severity: BugSeverity) {
        _uiState.value = _uiState.value.copy(severity = severity)
    }
    
    /**
     * Update the bug category
     */
    fun updateCategory(category: BugCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    /**
     * Add a reproduction step
     */
    fun addReproductionStep(step: String) {
        val sanitizedStep = validationUtils.sanitizeText(step)
        if (sanitizedStep.isNotBlank()) {
            val currentSteps = _uiState.value.reproductionSteps.toMutableList()
            currentSteps.add(sanitizedStep)
            
            val validation = validationUtils.validateReproductionSteps(currentSteps)
            _uiState.value = _uiState.value.copy(
                reproductionSteps = currentSteps,
                reproductionStepsError = if (validation.isValid) null else validation.errorMessage
            )
        }
    }
    
    /**
     * Remove a reproduction step
     */
    fun removeReproductionStep(index: Int) {
        val currentSteps = _uiState.value.reproductionSteps.toMutableList()
        if (index in currentSteps.indices) {
            currentSteps.removeAt(index)
            _uiState.value = _uiState.value.copy(reproductionSteps = currentSteps)
        }
    }
    
    /**
     * Update a reproduction step
     */
    fun updateReproductionStep(index: Int, step: String) {
        val sanitizedStep = validationUtils.sanitizeText(step)
        val currentSteps = _uiState.value.reproductionSteps.toMutableList()
        if (index in currentSteps.indices) {
            currentSteps[index] = sanitizedStep
            
            val validation = validationUtils.validateReproductionSteps(currentSteps)
            _uiState.value = _uiState.value.copy(
                reproductionSteps = currentSteps,
                reproductionStepsError = if (validation.isValid) null else validation.errorMessage
            )
        }
    }
    
    /**
     * Add an attachment
     */
    fun addAttachment(attachmentPath: String) {
        val currentAttachments = _uiState.value.attachments.toMutableList()
        currentAttachments.add(attachmentPath)
        
        val validation = validationUtils.validateAttachments(currentAttachments)
        _uiState.value = _uiState.value.copy(
            attachments = currentAttachments,
            attachmentsError = if (validation.isValid) null else validation.errorMessage
        )
    }
    
    /**
     * Remove an attachment
     */
    fun removeAttachment(index: Int) {
        val currentAttachments = _uiState.value.attachments.toMutableList()
        if (index in currentAttachments.indices) {
            currentAttachments.removeAt(index)
            _uiState.value = _uiState.value.copy(attachments = currentAttachments)
        }
    }
    
    /**
     * Submit the bug report
     */
    fun submitBugReport() {
        viewModelScope.launch {
            try {
                // Check rate limit
                val rateLimitResult = rateLimitManager.canSubmitBugReport()
                if (!rateLimitResult.allowed) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = rateLimitResult.reason ?: "Rate limit exceeded"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentState = _uiState.value
                val bugReport = BugReport(
                    title = currentState.title,
                    description = currentState.description,
                    severity = currentState.severity,
                    category = currentState.category,
                    deviceInfo = currentState.deviceInfo,
                    appVersion = getAppVersion(),
                    reproductionSteps = currentState.reproductionSteps,
                    attachments = currentState.attachments
                )
                
                // Validate the complete bug report
                val validation = validationUtils.validateBugReport(bugReport)
                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validation.errorMessage
                    )
                    return@launch
                }
                
                // Sanitize the bug report
                val sanitizedBugReport = validationUtils.sanitizeBugReport(bugReport)
                
                // Submit the bug report
                val result = repository.submitBugReport(sanitizedBugReport)
                
                if (result.isSuccess) {
                    rateLimitManager.recordBugReportSubmission()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitted = true,
                        submissionId = result.getOrNull()
                    )
                    Log.d(TAG, "Bug report submitted successfully: ${result.getOrNull()}")
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = FirestoreHelper.getErrorMessage((error as? Exception) ?: Exception("Unknown error: ${error?.message}"))
                    )
                    Log.e(TAG, "Failed to submit bug report", error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
                Log.e(TAG, "Unexpected error submitting bug report", e)
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
            _uiState.value = BugReportUiState(deviceInfo = deviceInfo)
        }
    }
    
    /**
     * Check if the form is valid for submission
     */
    fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.title.isNotBlank() &&
                state.description.isNotBlank() &&
                state.titleError == null &&
                state.descriptionError == null &&
                state.reproductionStepsError == null &&
                state.attachmentsError == null
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
 * UI state for the bug report screen
 */
data class BugReportUiState(
    val title: String = "",
    val description: String = "",
    val severity: BugSeverity = BugSeverity.MEDIUM,
    val category: BugCategory = BugCategory.OTHER,
    val reproductionSteps: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val deviceInfo: DeviceInfo = DeviceInfo("", "", "", "", "", 0, ""),
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val submissionId: String? = null,
    val error: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val reproductionStepsError: String? = null,
    val attachmentsError: String? = null
)