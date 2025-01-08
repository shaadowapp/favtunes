package com.shaadow.tunes.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.ui.graphics.vector.ImageVector
import it.vfsfitvnm.vimusic.R
import com.shaadow.tunes.models.SortBy

enum class AlbumSortBy(
    @StringRes override val text: Int,
    override val icon: ImageVector
) : SortBy {
    Title(
        text = R.string.title,
        icon = Icons.Outlined.SortByAlpha
    ),
    Year(
        text = R.string.year,
        icon = Icons.Outlined.CalendarMonth
    ),
    DateAdded(
        text = R.string.date_added,
        icon = Icons.Outlined.Schedule
    )
}
