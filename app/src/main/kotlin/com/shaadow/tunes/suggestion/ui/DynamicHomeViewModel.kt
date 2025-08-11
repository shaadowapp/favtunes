package com.shaadow.tunes.suggestion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.analytics.SessionContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing dynamic home screen state
 */
class DynamicHomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val suggestionSystem = AdvancedSuggestionSystem(application)
    private val suggestionProvider = HomeSuggestionProvider(suggestionSystem)
    
    private val _uiState = MutableStateFlow(DynamicHomeUiState())
    val uiState: StateFlow<DynamicHomeUiState> = _uiState.asStateFlow()
    
    init {
        loadSuggestions()
        startDiscoverySession()
    }
    
    fun loadSuggestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val sections = suggestionProvider.getHomeSections()
                _uiState.value = _uiState.value.copy(
                    sections = sections,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }
    
    fun refreshSuggestions() {
        loadSuggestions()
    }
    
    fun startDiscoverySession() {
        suggestionSystem.startListeningSession(SessionContext.DISCOVERY)
    }
    
    fun endSession() {
        suggestionSystem.endListeningSession()
    }
    
    fun performLearningUpdate() {
        viewModelScope.launch {
            try {
                suggestionSystem.performLearningUpdate()
                // Refresh suggestions after learning update
                loadSuggestions()
            } catch (e: Exception) {
                // Handle learning update error silently
            }
        }
    }
    
    fun getListeningAnalytics() = suggestionSystem.getListeningAnalytics()
    
    override fun onCleared() {
        super.onCleared()
        endSession()
    }
}

/**
 * UI state for the dynamic home screen
 */
data class DynamicHomeUiState(
    val sections: List<SuggestionSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)