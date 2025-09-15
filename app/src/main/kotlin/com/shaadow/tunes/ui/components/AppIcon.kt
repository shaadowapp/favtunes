package com.shaadow.tunes.ui.components
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.R

@Composable
fun AppIcon(context: Context) {
    Image(
        painter = painterResource(R.drawable.app_icon),
        contentDescription = "App Logo",
        modifier = Modifier.size(45.dp)
    )
}