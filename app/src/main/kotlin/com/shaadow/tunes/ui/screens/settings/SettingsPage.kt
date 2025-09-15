package com.shaadow.tunes.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.shaadow.tunes.enums.SettingsSection
import com.shaadow.tunes.ui.screens.settings.legal.PrivacyPolicy
import com.shaadow.tunes.ui.screens.settings.legal.TermsOfUse
import com.shaadow.tunes.ui.components.ScreenIdentifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(
    section: SettingsSection,
    pop: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = section.resourceId))
                },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        // Screen identifier for accurate screen detection
        ScreenIdentifier(
            screenId = when (section) {
                SettingsSection.Profile -> "settings_profile"
                SettingsSection.About -> "settings_about"
                SettingsSection.General -> "settings_general"
                SettingsSection.Suggestions -> "settings_suggestions"
                SettingsSection.Player -> "settings_player"
                SettingsSection.Cache -> "settings_cache"
                SettingsSection.Gestures -> "settings_gestures"
                SettingsSection.Database -> "settings_database"
                SettingsSection.Other -> "settings_other"
                SettingsSection.TermsOfUse -> "settings_terms"
                SettingsSection.PrivacyPolicy -> "settings_privacy"
                SettingsSection.BugReport -> "settings_bugreport"
                SettingsSection.Feedback -> "settings_feedback"
            },
            screenName = when (section) {
                SettingsSection.Profile -> "Profile Settings"
                SettingsSection.About -> "About Settings"
                SettingsSection.General -> "General Settings"
                SettingsSection.Suggestions -> "Suggestions Settings"
                SettingsSection.Player -> "Player Settings"
                SettingsSection.Cache -> "Cache Settings"
                SettingsSection.Gestures -> "Gestures Settings"
                SettingsSection.Database -> "Database Settings"
                SettingsSection.Other -> "Other Settings"
                SettingsSection.TermsOfUse -> "Terms of Use"
                SettingsSection.PrivacyPolicy -> "Privacy Policy"
                SettingsSection.BugReport -> "Bug Report Settings"
                SettingsSection.Feedback -> "Feedback Settings"
            }
        )
        
        when (section) {
            SettingsSection.Profile -> ProfileScreen(paddingValues)
            SettingsSection.About -> About()
            SettingsSection.General -> GeneralSettings()
            SettingsSection.Suggestions -> SuggestionSettings(paddingValues)
            SettingsSection.Player -> PlayerSettings(paddingValues)
            SettingsSection.Cache -> CacheSettings()
            SettingsSection.Gestures -> GestureSettings()
            SettingsSection.Database -> DatabaseSettings()
            SettingsSection.Other -> OtherSettings()
            SettingsSection.TermsOfUse -> TermsOfUse()
            SettingsSection.PrivacyPolicy -> PrivacyPolicy()
            SettingsSection.BugReport -> {
                // Bug report is handled via navigation, not as a settings page
                Text("Bug Report - This should be handled via navigation")
            }
            SettingsSection.Feedback -> {
                // Feedback is handled via navigation, not as a settings page
                Text("Feedback - This should be handled via navigation")
            }
        }
    }
}