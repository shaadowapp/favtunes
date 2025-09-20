package com.shaadow.tunes.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.service.PlayerMediaBrowserService
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.isAtLeastAndroid12
import com.shaadow.tunes.utils.isAtLeastAndroid6
import com.shaadow.tunes.utils.isIgnoringBatteryOptimizations
import com.shaadow.tunes.utils.isInvincibilityEnabledKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.utils.shakeDetectionEnabledKey
import com.shaadow.tunes.utils.toast
import androidx.core.net.toUri

@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun OtherSettings() {
    // Screen identifier for accurate screen detection
    ScreenIdentifier(
        screenId = "other_settings",
        screenName = "Other Settings"
    )
    
    val context = LocalContext.current
    val playerPadding = LocalPlayerPadding.current

    var isAndroidAutoEnabled by remember {
        val component = ComponentName(context, PlayerMediaBrowserService::class.java)
        val disabledFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val enabledFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        mutableStateOf(
            value = context.packageManager.getComponentEnabledSetting(component) == enabledFlag,
            policy = object : SnapshotMutationPolicy<Boolean> {
                override fun equivalent(a: Boolean, b: Boolean): Boolean {
                    context.packageManager.setComponentEnabledSetting(
                        component,
                        if (b) enabledFlag else disabledFlag,
                        PackageManager.DONT_KILL_APP
                    )
                    return a == b
                }
            }
        )
    }

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)
    var isShakeDetectionEnabled by rememberPreference(shakeDetectionEnabledKey, true)
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(context.isIgnoringBatteryOptimizations) }
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = 64.dp,
                bottom = 32.dp + playerPadding,
                start = 8.dp,
                end = 8.dp
            )
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        SwitchSettingEntry(
            title = stringResource(id = R.string.shake_detection),
            text = stringResource(id = R.string.shake_detection_description),
            icon = Icons.Outlined.Vibration,
            isChecked = isShakeDetectionEnabled,
            onCheckedChange = { isShakeDetectionEnabled = it }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacer))

        SwitchSettingEntry(
            title = stringResource(id = R.string.android_auto),
            text = stringResource(id = R.string.android_auto_description),
            icon = Icons.Outlined.DirectionsCar,
            isChecked = isAndroidAutoEnabled,
            onCheckedChange = { isAndroidAutoEnabled = it }
        )

        InfoInformation(
            text = stringResource(id = R.string.android_auto_information),
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        Spacer(modifier = Modifier.height(Dimensions.spacer))

        Text(
            text = stringResource(id = R.string.service_lifetime),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SettingsEntry(
            title = stringResource(id = R.string.ignore_battery_optimizations),
            text = if (isIgnoringBatteryOptimizations) {
                stringResource(id = R.string.already_unrestricted)
            } else {
                stringResource(id = R.string.disable_background_restrictions)
            },
            icon = Icons.Outlined.Battery0Bar,
            onClick = {
                if (!isAtLeastAndroid6) return@SettingsEntry

                try {
                    activityResultLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                } catch (_: ActivityNotFoundException) {
                    try {
                        activityResultLauncher.launch(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    } catch (_: ActivityNotFoundException) {
                        context.toast("Couldn't find battery optimization settings, please whitelist FavTunes manually")
                    }
                }
            },
            isEnabled = !isIgnoringBatteryOptimizations
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.service_lifetime),
            text = stringResource(id = R.string.service_lifetime_description),
            icon = Icons.Outlined.Stars,
            isChecked = isInvincibilityEnabled,
            onCheckedChange = { isInvincibilityEnabled = it }
        )

        InfoInformation(
            text = stringResource(id = R.string.service_lifetime_information) +
                    if (isAtLeastAndroid12) "\n" + stringResource(id = R.string.service_lifetime_information_plus) else "",
            modifier = Modifier.padding(horizontal = 0.dp)
        )
    }
}