package com.shaadow.tunes.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.requests.visitorData
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.service.LoginRequiredException
import com.shaadow.tunes.service.PlayableFormatNotFoundException
import com.shaadow.tunes.service.UnplayableException
import com.shaadow.tunes.service.VideoIdMismatchException
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.ui.styling.px
import com.shaadow.tunes.utils.DisposableListener
import com.shaadow.tunes.utils.currentWindow
import com.shaadow.tunes.utils.forceSeekToNext
import com.shaadow.tunes.utils.forceSeekToPrevious
import com.shaadow.tunes.utils.playerGesturesEnabledKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.utils.thumbnail
import kotlinx.coroutines.runBlocking
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    fullScreenLyrics: Boolean,
    toggleFullScreenLyrics: () -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    var playerGesturesEnabled by rememberPreference(playerGesturesEnabledKey, true)
    var nullableWindow by remember { mutableStateOf(player.currentWindow) }
    var error by remember { mutableStateOf<PlaybackException?>(player.playerError) }
    var errorCounter by remember(error) { mutableIntStateOf(0) }

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    // Helper function to determine if error is recoverable
    fun isRecoverableError(error: PlaybackException?): Boolean {
        return when {
            error is VideoIdMismatchException -> false
            error is UnplayableException -> false
            error is LoginRequiredException -> false
            error?.cause is VideoIdMismatchException -> false
            error?.cause is UnplayableException -> false
            error?.cause is LoginRequiredException -> false
            error?.cause?.cause is VideoIdMismatchException -> false
            error?.cause?.cause is UnplayableException -> false
            error?.cause?.cause is LoginRequiredException -> false
            else -> true // Network errors, etc. are recoverable
        }
    }

    val retry = {
        when (error?.cause?.cause) {
            is UnresolvedAddressException,
            is UnknownHostException,
            is PlayableFormatNotFoundException,
            is UnplayableException,
            is LoginRequiredException -> player.prepare()

            else -> {
                runBlocking {
                    Innertube.visitorData = Innertube.visitorData().getOrNull()
                }
                player.prepare()
            }
        }
    }

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableWindow = player.currentWindow
                errorCounter = 0 // Reset error counter for new media item
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException

                if (errorCounter == 0) {
                    retry()
                    errorCounter += 1
                }
            }
        }
    }

    val window = nullableWindow ?: return

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection =
                if (targetState.firstPeriodIndex > initialState.firstPeriodIndex) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        contentAlignment = Alignment.Center,
        label = "thumbnail"
    ) { currentWindow ->
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd) binder.player.forceSeekToPrevious()
                else if (value == SwipeToDismissBoxValue.EndToStart) binder.player.forceSeekToNext()

                return@rememberSwipeToDismissBoxState false
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                    },
                    label = "background"
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                        SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                        SwipeToDismissBoxValue.Settled -> Arrangement.Center
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                        Icon(
                            imageVector = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.SkipPrevious
                                else -> Icons.Outlined.SkipNext
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            modifier = modifier.clip(MaterialTheme.shapes.large),
            gesturesEnabled = playerGesturesEnabled
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .then(
                        if (fullScreenLyrics) Modifier
                            .width(thumbnailSizeDp)
                            .fillMaxHeight() else Modifier
                            .aspectRatio(1f)
                            .size(thumbnailSizeDp)
                    )
            ) {
                AsyncImage(
                    model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(
                        thumbnailSizePx
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onShowLyrics(true) },
                            onLongClick = { onShowStatsForNerds(true) }
                        )
                        .fillMaxSize()
                )

                Lyrics(
                    mediaId = currentWindow.mediaItem.mediaId,
                    isDisplayed = isShowingLyrics && error == null,
                    onDismiss = {
                        onShowLyrics(false)
                        if (fullScreenLyrics) toggleFullScreenLyrics()
                    },
                    ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
                    size = thumbnailSizeDp,
                    mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                    durationProvider = player::getDuration,
                    fullScreenLyrics = fullScreenLyrics,
                    toggleFullScreenLyrics = toggleFullScreenLyrics
                )

                if (isShowingStatsForNerds) {
                    StatsForNerds(
                        mediaId = currentWindow.mediaItem.mediaId,
                        onDismiss = { onShowStatsForNerds(false) }
                    )
                }

                val errorMessage = getErrorMessage(error)
                
                PlaybackError(
                    isDisplayed = error != null,
                    messageProvider = { errorMessage },
                    onDismiss = retry
                )
            }
        }
    }
}