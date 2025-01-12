package com.shaadow.tunes.ui.styling

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shaadow.tunes.ui.styling.Typography
import com.shaadow.tunes.ui.styling.backgroundDark
import com.shaadow.tunes.ui.styling.backgroundLight
import com.shaadow.tunes.ui.styling.errorContainerDark
import com.shaadow.tunes.ui.styling.errorContainerLight
import com.shaadow.tunes.ui.styling.errorDark
import com.shaadow.tunes.ui.styling.errorLight
import com.shaadow.tunes.ui.styling.inverseOnSurfaceDark
import com.shaadow.tunes.ui.styling.inverseOnSurfaceLight
import com.shaadow.tunes.ui.styling.inversePrimaryDark
import com.shaadow.tunes.ui.styling.inversePrimaryLight
import com.shaadow.tunes.ui.styling.inverseSurfaceDark
import com.shaadow.tunes.ui.styling.inverseSurfaceLight
import com.shaadow.tunes.ui.styling.onBackgroundDark
import com.shaadow.tunes.ui.styling.onBackgroundLight
import com.shaadow.tunes.ui.styling.onErrorContainerDark
import com.shaadow.tunes.ui.styling.onErrorContainerLight
import com.shaadow.tunes.ui.styling.onErrorDark
import com.shaadow.tunes.ui.styling.onErrorLight
import com.shaadow.tunes.ui.styling.onPrimaryContainerDark
import com.shaadow.tunes.ui.styling.onPrimaryContainerLight
import com.shaadow.tunes.ui.styling.onPrimaryDark
import com.shaadow.tunes.ui.styling.onPrimaryLight
import com.shaadow.tunes.ui.styling.onSecondaryContainerDark
import com.shaadow.tunes.ui.styling.onSecondaryContainerLight
import com.shaadow.tunes.ui.styling.onSecondaryDark
import com.shaadow.tunes.ui.styling.onSecondaryLight
import com.shaadow.tunes.ui.styling.onSurfaceDark
import com.shaadow.tunes.ui.styling.onSurfaceLight
import com.shaadow.tunes.ui.styling.onSurfaceVariantDark
import com.shaadow.tunes.ui.styling.onSurfaceVariantLight
import com.shaadow.tunes.ui.styling.onTertiaryContainerDark
import com.shaadow.tunes.ui.styling.onTertiaryContainerLight
import com.shaadow.tunes.ui.styling.onTertiaryDark
import com.shaadow.tunes.ui.styling.onTertiaryLight
import com.shaadow.tunes.ui.styling.outlineDark
import com.shaadow.tunes.ui.styling.outlineLight
import com.shaadow.tunes.ui.styling.outlineVariantDark
import com.shaadow.tunes.ui.styling.outlineVariantLight
import com.shaadow.tunes.ui.styling.primaryContainerDark
import com.shaadow.tunes.ui.styling.primaryContainerLight
import com.shaadow.tunes.ui.styling.primaryDark
import com.shaadow.tunes.ui.styling.primaryLight
import com.shaadow.tunes.ui.styling.scrimDark
import com.shaadow.tunes.ui.styling.scrimLight
import com.shaadow.tunes.ui.styling.secondaryContainerDark
import com.shaadow.tunes.ui.styling.secondaryContainerLight
import com.shaadow.tunes.ui.styling.secondaryDark
import com.shaadow.tunes.ui.styling.secondaryLight
import com.shaadow.tunes.ui.styling.surfaceBrightDark
import com.shaadow.tunes.ui.styling.surfaceBrightLight
import com.shaadow.tunes.ui.styling.surfaceContainerDark
import com.shaadow.tunes.ui.styling.surfaceContainerHighDark
import com.shaadow.tunes.ui.styling.surfaceContainerHighLight
import com.shaadow.tunes.ui.styling.surfaceContainerHighestDark
import com.shaadow.tunes.ui.styling.surfaceContainerHighestLight
import com.shaadow.tunes.ui.styling.surfaceContainerLight
import com.shaadow.tunes.ui.styling.surfaceContainerLowDark
import com.shaadow.tunes.ui.styling.surfaceContainerLowLight
import com.shaadow.tunes.ui.styling.surfaceContainerLowestDark
import com.shaadow.tunes.ui.styling.surfaceContainerLowestLight
import com.shaadow.tunes.ui.styling.surfaceDark
import com.shaadow.tunes.ui.styling.surfaceDimDark
import com.shaadow.tunes.ui.styling.surfaceDimLight
import com.shaadow.tunes.ui.styling.surfaceLight
import com.shaadow.tunes.ui.styling.surfaceVariantDark
import com.shaadow.tunes.ui.styling.surfaceVariantLight
import com.shaadow.tunes.ui.styling.tertiaryContainerDark
import com.shaadow.tunes.ui.styling.tertiaryContainerLight
import com.shaadow.tunes.ui.styling.tertiaryDark
import com.shaadow.tunes.ui.styling.tertiaryLight

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}