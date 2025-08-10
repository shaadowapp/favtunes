package com.shaadow.tunes.ui.screens.player

import android.content.ClipData
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

import androidx.compose.ui.res.stringResource
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
    isDisplayed: Boolean,
    messageProvider: () -> String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = messageProvider(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss"
                    )
                }
            }
        }
    }
}

@Composable
fun getErrorMessage(error: PlaybackException?): String {
    return when {
        // Check direct error types first
        error is PlayableFormatNotFoundException -> stringResource(id = R.string.playable_format_not_found_error)
        error is UnplayableException -> stringResource(id = R.string.video_source_deleted_error)
        error is LoginRequiredException -> stringResource(id = R.string.server_restrictions_error)
        error is VideoIdMismatchException -> stringResource(id = R.string.id_mismatch_error)
        
        // Check first level cause
        error?.cause is UnresolvedAddressException || error?.cause is UnknownHostException -> stringResource(id = R.string.network_error)
        error?.cause is PlayableFormatNotFoundException -> stringResource(id = R.string.playable_format_not_found_error)
        error?.cause is UnplayableException -> stringResource(id = R.string.video_source_deleted_error)
        error?.cause is LoginRequiredException -> stringResource(id = R.string.server_restrictions_error)
        error?.cause is VideoIdMismatchException -> stringResource(id = R.string.id_mismatch_error)
        
        // Check second level cause
        error?.cause?.cause is UnresolvedAddressException || error?.cause?.cause is UnknownHostException -> stringResource(id = R.string.network_error)
        error?.cause?.cause is PlayableFormatNotFoundException -> stringResource(id = R.string.playable_format_not_found_error)
        error?.cause?.cause is UnplayableException -> stringResource(id = R.string.video_source_deleted_error)
        error?.cause?.cause is LoginRequiredException -> stringResource(id = R.string.server_restrictions_error)
        error?.cause?.cause is VideoIdMismatchException -> stringResource(id = R.string.id_mismatch_error)
        
        // Check error message for network issues
        error?.message?.contains("network", ignoreCase = true) == true -> stringResource(id = R.string.network_error)
        error?.message?.contains("connection", ignoreCase = true) == true -> stringResource(id = R.string.network_error)
        
        else -> stringResource(id = R.string.unknown_playback_error)
    }
}