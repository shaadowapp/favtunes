package com.shaadow.tunes.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
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
import com.shaadow.tunes.ui.screens.settings.ProfileScreen
import com.shaadow.tunes.ui.screens.settings.About
import com.shaadow.tunes.ui.screens.settings.CacheSettings
import com.shaadow.tunes.ui.screens.settings.DatabaseSettings
import com.shaadow.tunes.ui.screens.settings.GeneralSettings
import com.shaadow.tunes.ui.screens.settings.OtherSettings
import com.shaadow.tunes.ui.screens.settings.PlayerSettings
import com.shaadow.tunes.ui.screens.settings.legal.TermsOfUse
import com.shaadow.tunes.enums.SettingsSection
import com.shaadow.tunes.ui.components.consumeCustomWindowInsets
import com.shaadow.tunes.ui.screens.settings.legal.PrivacyPolicy

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
        Surface(modifier = Modifier.consumeCustomWindowInsets(paddingValues)) {
            when (section) {
                SettingsSection.Profile -> ProfileScreen()
                SettingsSection.About -> About()
                SettingsSection.General -> GeneralSettings()
                SettingsSection.Player -> PlayerSettings()
                SettingsSection.Cache -> CacheSettings()
                SettingsSection.Database -> DatabaseSettings()
                SettingsSection.Other -> OtherSettings()
                SettingsSection.TermsOfUse -> TermsOfUse()
                SettingsSection.PrivacyPolicy -> PrivacyPolicy()
            }
        }
    }
}