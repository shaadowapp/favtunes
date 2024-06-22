package it.vfsfitvnm.vimusic.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class IconButtonInfo(
    val enabled: Boolean = true,
    val onClick: () -> Unit,
    val icon: ImageVector,
    @StringRes val description: Int
)