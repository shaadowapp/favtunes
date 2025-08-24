package com.shaadow.tunes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shaadow.tunes.R

@Composable
fun BugReportMenuItem(
    onClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.bug_report)) },
        onClick = {
            onDismiss()
            onClick()
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.BugReport,
                contentDescription = null
            )
        }
    )
}

@Composable
fun FeedbackMenuItem(
    onClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.feedback)) },
        onClick = {
            onDismiss()
            onClick()
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Feedback,
                contentDescription = null
            )
        }
    )
}