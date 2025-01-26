package com.shaadow.tunes.ui.components
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.R

@Composable
fun AppIcon(context: Context) {
    val isDarkMode = isDarkTheme(context)

    Image(
        painter = painterResource(
            id = if (isDarkMode) R.drawable.logo_light else R.drawable.logo_dark
        ),
        contentDescription = "App Logo",
        modifier = Modifier.size(105.dp)
    )
}

fun isDarkTheme(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}