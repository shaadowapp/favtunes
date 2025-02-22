package com.shaadow.tunes.ui.screens.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import com.shaadow.tunes.R
import com.shaadow.tunes.service.LoginRequiredException
import com.shaadow.tunes.service.PlayableFormatNotFoundException
import com.shaadow.tunes.service.UnplayableException
import com.shaadow.tunes.service.VideoIdMismatchException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@Composable
fun PlaybackError(
    error: PlaybackException?,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var isShowingLogsDialog by rememberSaveable { mutableStateOf(false) }

    val errorMessage = when (error?.cause?.cause) {
        is UnresolvedAddressException, is UnknownHostException -> stringResource(id = R.string.network_error)
        is PlayableFormatNotFoundException -> stringResource(id = R.string.playable_format_not_found_error)
        is UnplayableException -> stringResource(id = R.string.video_source_deleted_error)
        is LoginRequiredException -> stringResource(id = R.string.server_restrictions_error)
        is VideoIdMismatchException -> stringResource(id = R.string.id_mismatch_error)
        else -> stringResource(id = R.string.unknown_playback_error)
    }

    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.8F))
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(id = R.string.retry)
                    )

                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                    Text(text = stringResource(id = R.string.retry))
                }
                FilledTonalIconButton(
                    onClick = { isShowingLogsDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(id = R.string.error_log)
                    )
                }
            }
        }
    }
        if (isShowingLogsDialog) {
            val errorText = Log.getStackTraceString(error)

            AlertDialog(
                onDismissRequest = { isShowingLogsDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = { isShowingLogsDialog = false }
                    ) {
                        Text(text = stringResource(id = R.string.close))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { clipboardManager.setText(AnnotatedString(errorText)) }
                    ) {
                        Text(text = stringResource(id = R.string.copy))
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.error_log))
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        SelectionContainer {
                            Text(
                                text = errorText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            )
        }
    }
