package com.shaadow.tunes.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info

import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.SettingsSection
import com.shaadow.tunes.ui.components.themed.ValueSelectorDialog
import com.shaadow.tunes.ui.styling.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SettingsScreen(
    pop: () -> Unit,
    onGoToSettingsPage: (Int) -> Unit,
    onNavigateToBugReport: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    val playerPadding = LocalPlayerPadding.current
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + playerPadding,
                    start = 0.dp,
                    end = 0.dp
                )
        ) {
            // Account Category
            CategoryHeader("Account")
            listOf(0).forEach { index -> // Profile
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
            
            // Playback Category
            CategoryHeader("Playback")
            listOf(3, 4).forEach { index -> // Player, Gestures
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
            
            // Personalization Category
            CategoryHeader("Personalization")
            listOf(1, 2).forEach { index -> // General, Suggestions
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
            
            // Storage Category
            CategoryHeader("Storage")
            listOf(5, 6).forEach { index -> // Cache, Database
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
            
            // Legal Category
            CategoryHeader("Legal")
            listOf(11, 12).forEach { index -> // Terms of Use, Privacy Policy
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
            
            // Support Category
            CategoryHeader("Support")
            
            // Bug Report
            ListItem(
                headlineContent = {
                    Text(text = stringResource(id = R.string.bug_report))
                },
                modifier = Modifier.clickable { onNavigateToBugReport() },
                leadingContent = {
                    Icon(
                        imageVector = SettingsSection.BugReport.icon,
                        contentDescription = stringResource(id = R.string.bug_report)
                    )
                }
            )
            
            // Feedback
            ListItem(
                headlineContent = {
                    Text(text = stringResource(id = R.string.feedback))
                },
                modifier = Modifier.clickable { onNavigateToFeedback() },
                leadingContent = {
                    Icon(
                        imageVector = SettingsSection.Feedback.icon,
                        contentDescription = stringResource(id = R.string.feedback)
                    )
                }
            )
            

            
            // Others Category
            CategoryHeader("Others")
            listOf(7, 8).forEach { index -> // Other, About
                val section = SettingsSection.entries[index]
                SettingsItem(section, index, onGoToSettingsPage)
            }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> EnumValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    crossinline onValueSelected: (T) -> Unit,
    icon: ImageVector,
    isEnabled: Boolean = true,
    crossinline valueText: (T) -> String = Enum<T>::name,
    noinline trailingContent: @Composable (() -> Unit)? = null
) {
    ValueSelectorSettingsEntry(
        title = title,
        selectedValue = selectedValue,
        values = enumValues<T>().toList(),
        onValueSelected = onValueSelected,
        icon = icon,
        isEnabled = isEnabled,
        valueText = valueText,
        trailingContent = trailingContent,
    )
}

@Composable
inline fun <T> ValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    values: List<T>,
    crossinline onValueSelected: (T) -> Unit,
    icon: ImageVector,
    isEnabled: Boolean = true,
    crossinline valueText: (T) -> String = { it.toString() },
    noinline trailingContent: @Composable (() -> Unit)? = null
) {
    var isShowingDialog by remember { mutableStateOf(false) }

    if (isShowingDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingDialog = false },
            title = title,
            selectedValue = selectedValue,
            values = values,
            onValueSelected = onValueSelected,
            valueText = valueText
        )
    }

    SettingsEntry(
        title = title,
        text = valueText(selectedValue),
        icon = icon,
        onClick = { isShowingDialog = true },
        isEnabled = isEnabled,
        trailingContent = trailingContent
    )
}

@Composable
fun SwitchSettingEntry(
    title: String,
    text: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    SettingsEntry(
        title = title,
        text = text,
        icon = icon,
        onClick = { onCheckedChange(!isChecked) },
        isEnabled = isEnabled
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun SettingsEntry(
    title: String,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(text = title)
        },
        modifier = Modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1F else Dimensions.lowOpacity),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title
            )
        },
        supportingContent = {
            Text(text = text)
        },
        trailingContent = { trailingContent?.invoke() }
    )
}

@Composable
fun InfoInformation(
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            modifier = Modifier
                .size(20.dp),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(section: SettingsSection, index: Int, onGoToSettingsPage: (Int) -> Unit) {
    ListItem(
        headlineContent = {
            Text(text = stringResource(id = section.resourceId))
        },
        modifier = Modifier.clickable { onGoToSettingsPage(index) },
        leadingContent = {
            Icon(
                imageVector = section.icon,
                contentDescription = stringResource(id = section.resourceId)
            )
        }
    )
}

@Composable
fun SettingsProgress(text: String, progress: Float) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.width(240.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        )
    }
}