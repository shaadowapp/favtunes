package com.shaadow.tunes.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.More
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.ui.graphics.vector.ImageVector
import com.shaadow.tunes.R

enum class SettingsSection(
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    Profile(
        resourceId = R.string.profile,
        Icons.Outlined.PersonOutline
    ),
    General(
        resourceId = R.string.general,
        Icons.Outlined.Tune
    ),
    Suggestions(
        resourceId = R.string.suggestions,
        Icons.Outlined.Psychology
    ),
    Player(
        resourceId = R.string.player,
        icon = Icons.Outlined.PlayArrow
    ),
    Gestures(
        resourceId = R.string.gestures,
        icon = Icons.Outlined.Gesture
    ),
    Cache(
        resourceId = R.string.cache,
        icon = Icons.Outlined.History
    ),
    Database(
        resourceId = R.string.database,
        icon = Icons.Outlined.Save
    ),
    Other(
        resourceId = R.string.other,
        icon = Icons.AutoMirrored.Outlined.More
    ),
    About(
        resourceId = R.string.about,
        icon = Icons.Outlined.Info
    ),
    BugReport(
        resourceId = R.string.bug_report,
        icon = Icons.Outlined.BugReport
    ),
    Feedback(
        resourceId = R.string.feedback,
        icon = Icons.Outlined.Feedback
    ),
    TermsOfUse(
        resourceId = R.string.TermsOfUse,
        icon = Icons.Outlined.AttachFile
    ),
    PrivacyPolicy(
        resourceId = R.string.PrivacyPolicy,
        icon = Icons.Outlined.PrivacyTip
    )
}