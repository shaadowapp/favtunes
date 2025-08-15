package com.shaadow.tunes.suggestion

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Simple suggestion system integration that works without compilation errors
 * This replaces the complex SuggestionSystemIntegration
 * Note: Context is always applicationContext from getInstance method
 */
@Suppress("StaticFieldLeak") // Context is always applicationContext
class SimpleSuggestionIntegration(private val context: Context) {
    
    private val suggestionSystem = WorkingSuggestionSystem(context)
    
    /**
     * Check if onboarding is needed
     */
    fun needsOnboarding(): Boolean = !suggestionSystem.isOnboardingComplete()
    
    /**
     * Complete onboarding setup
     */
    fun completeOnboarding() {
        // Set default preferences if none selected
        suggestionSystem.setInitialPreferences(listOf("pop", "rock"))
    }
    
    /**
     * Get the suggestion system instance
     */
    fun getSuggestionSystem(): WorkingSuggestionSystem = suggestionSystem
    
    /**
     * Initialize the suggestion system
     */
    fun initialize() {
        // Perform any necessary initialization
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleSuggestionIntegration? = null
        
        fun getInstance(context: Context): SimpleSuggestionIntegration {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SimpleSuggestionIntegration(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Simple composable integration helper
 */
@Composable
fun SimpleSuggestionOnboardingIntegration(
    context: Context,
    onOnboardingComplete: () -> Unit,
    content: @Composable () -> Unit
) {
    val integration = remember { SimpleSuggestionIntegration.getInstance(context) }
    
    if (integration.needsOnboarding()) {
        // For now, just complete onboarding automatically
        // You can replace this with actual onboarding UI later
        LaunchedEffect(Unit) {
            integration.completeOnboarding()
            onOnboardingComplete()
        }
    } else {
        content()
    }
    
    // Initialize the system
    LaunchedEffect(Unit) {
        integration.initialize()
    }
}