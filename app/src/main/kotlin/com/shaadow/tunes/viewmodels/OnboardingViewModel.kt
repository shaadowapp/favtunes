package com.shaadow.tunes.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.tunes.data.OnboardingData
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import com.shaadow.tunes.utils.LanguagePreference
import kotlinx.coroutines.launch

class OnboardingViewModel(private val context: Context) : ViewModel() {
    
    private val suggestionIntegration = SimpleSuggestionIntegration.getInstance(context)
    
    // Step management
    var currentStep by mutableStateOf(0)
    val totalSteps = 4
    
    // Selection states
    var selectedLanguage by mutableStateOf(LanguagePreference.getLanguageCode(context))
    var selectedGenres by mutableStateOf(setOf<String>())
    var selectedMoods by mutableStateOf(setOf<String>())
    var isLoading by mutableStateOf(false)
    
    // Language options
    val supportedLanguages = LanguagePreference.getSupportedLanguages()
    
    // Data from OnboardingData
    val genres = OnboardingData.genres
    val moods = OnboardingData.moods
    
    // Selection limits
    companion object {
        const val MIN_GENRES = 3
        const val MAX_GENRES = 7
        const val MIN_MOODS = 2
        const val MAX_MOODS = 4
    }
    
    // Step navigation
    fun nextStep() {
        if (currentStep < totalSteps - 1) {
            currentStep++
        }
    }
    
    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
        }
    }
    
    fun canProceedFromCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> true // Welcome step, always can proceed
            1 -> selectedLanguage.isNotEmpty() // Language step, need language selected
            2 -> selectedGenres.size >= MIN_GENRES // Genre step, need minimum genres
            3 -> selectedMoods.size >= MIN_MOODS // Mood step, need minimum moods
            else -> false
        }
    }
    
    // Selection updates with limits
    fun updateLanguage(languageCode: String) {
        selectedLanguage = languageCode
    }
    
    fun updateGenres(genreId: String) {
        selectedGenres = if (selectedGenres.contains(genreId)) {
            selectedGenres - genreId
        } else {
            if (selectedGenres.size < MAX_GENRES) {
                selectedGenres + genreId
            } else {
                selectedGenres
            }
        }
    }
    
    fun updateMoods(moodId: String) {
        selectedMoods = if (selectedMoods.contains(moodId)) {
            selectedMoods - moodId
        } else {
            if (selectedMoods.size < MAX_MOODS) {
                selectedMoods + moodId
            } else {
                selectedMoods
            }
        }
    }
    
    // Helper methods for UI
    fun getGenreSelectionText(): String {
        return "Select ${MIN_GENRES}-${MAX_GENRES} genres (${selectedGenres.size}/${MAX_GENRES})"
    }
    
    fun getMoodSelectionText(): String {
        return "Select ${MIN_MOODS}-${MAX_MOODS} moods (${selectedMoods.size}/${MAX_MOODS})"
    }
    
    fun canSelectMoreGenres(): Boolean = selectedGenres.size < MAX_GENRES
    fun canSelectMoreMoods(): Boolean = selectedMoods.size < MAX_MOODS
    
    // Completion
    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Save language preference
                val sharedPrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("selected_language", selectedLanguage).apply()
                
                // Save only the selected genres as user preferences (not moods)
                // Moods are used for other purposes but genres are what define music taste
                val success = suggestionIntegration.getSuggestionSystem().setInitialPreferences(selectedGenres.toList())
                
                // Also save moods separately for other features if needed
                val moodPrefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
                moodPrefs.edit().putStringSet("selected_moods", selectedMoods).apply()
                
                if (success) {
                    onComplete()
                }
            } catch (e: Exception) {
                // Handle error - for now just complete anyway
                onComplete()
            } finally {
                isLoading = false
            }
        }
    }
    
    fun skipOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                suggestionIntegration.completeOnboarding()
                onComplete()
            } catch (e: Exception) {
                onComplete()
            } finally {
                isLoading = false
            }
        }
    }
    
    fun hasSelections(): Boolean {
        return selectedGenres.size >= MIN_GENRES && selectedMoods.size >= MIN_MOODS
    }
    
    fun getProgressPercentage(): Float {
        return (currentStep + 1).toFloat() / totalSteps.toFloat()
    }
}