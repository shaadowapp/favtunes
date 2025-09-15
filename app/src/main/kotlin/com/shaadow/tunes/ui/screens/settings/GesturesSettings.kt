package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.utils.listGesturesEnabledKey
import com.shaadow.tunes.utils.miniplayerGesturesEnabledKey
import com.shaadow.tunes.utils.playerGesturesEnabledKey
import com.shaadow.tunes.utils.rememberPreference

@Composable
fun GestureSettings() {
    val playerPadding = LocalPlayerPadding.current

    var listGesturesEnabled by rememberPreference(listGesturesEnabledKey, true)
    var playerGesturesEnabled by rememberPreference(playerGesturesEnabledKey, true)
    var miniplayerGesturesEnabled by rememberPreference(miniplayerGesturesEnabledKey, true)

    // Screen identifier for accurate screen detection
    ScreenIdentifier(
        screenId = "gestures_settings",
        screenName = "Gestures Settings"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = 32.dp,
                bottom = 32.dp + playerPadding,
                start = 0.dp,
                end = 0.dp
            )
    ) {
        SwitchSettingEntry(
            title = stringResource(id = R.string.list_gestures),
            text = stringResource(id = R.string.list_gestures_description),
            icon = Icons.Outlined.LibraryMusic,
            isChecked = listGesturesEnabled,
            onCheckedChange = { listGesturesEnabled = !listGesturesEnabled }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.player_gestures),
            text = stringResource(id = R.string.player_gestures_description),
            icon = Icons.Outlined.PlayCircle,
            isChecked = playerGesturesEnabled,
            onCheckedChange = { playerGesturesEnabled = !playerGesturesEnabled }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.miniplayer_gestures),
            text = stringResource(id = R.string.player_gestures_description),
            icon = Icons.Outlined.PlayArrow,
            isChecked = miniplayerGesturesEnabled,
            onCheckedChange = { miniplayerGesturesEnabled = !miniplayerGesturesEnabled }
        )
    }
}