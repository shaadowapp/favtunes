package com.shaadow.tunes.suggestion

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.shaadow.tunes.suggestion.integration.AdvancedPlayerServiceEnhancer
import com.shaadow.tunes.suggestion.onboarding.OnboardingManager
import com.shaadow.tunes.suggestion.onboarding.OnboardingScreen

/**
 * Complete integration system for the FavTunes suggestion system
 * Provides easy integration points for the entire app
 */
class SuggestionSystemIntegration(private val context: Context) {
    
    private val onboardingManager = OnboardingManager(context)
    private val playerEnhancer = AdvancedPlayerServiceEnhancer(context)
    
    /**
     * Check if onboarding is needed
     */
    fun needsOnboarding(): Boolean = onboardingManager.needsOnboarding()
    
    /**
     * Get the player service enhancer
     */
    fun getPlayerEnhancer(): AdvancedPlayerServiceEnhancer = playerEnhancer
    
    /**
     * Complete onboarding setup
     */
    fun completeOnboarding() {
        onboardingManager.skipOnboarding() // Use defaults if no preferences selected
    }
    
    /**
     * Initialize the suggestion system
     */
    fun initialize() {
        // Perform any necessary initialization
        // This could include migration, cleanup, etc.
    }
    
    companion object {
        /**
         * Create a singleton instance for the app
         */
        @Volatile
        private var INSTANCE: SuggestionSystemIntegration? = null
        
        fun getInstance(context: Context): SuggestionSystemIntegration {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SuggestionSystemIntegration(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Composable integration helper for onboarding
 */
@Composable
fun SuggestionOnboardingIntegration(
    context: Context,
    onOnboardingComplete: () -> Unit,
    content: @Composable () -> Unit
) {
    val integration = remember { SuggestionSystemIntegration.getInstance(context) }
    
    if (integration.needsOnboarding()) {
        OnboardingScreen(
            onComplete = {
                integration.completeOnboarding()
                onOnboardingComplete()
            },
            onSkip = {
                integration.completeOnboarding()
                onOnboardingComplete()
            }
        )
    } else {
        content()
    }
    
    // Initialize the system
    LaunchedEffect(Unit) {
        integration.initialize()
    }
}