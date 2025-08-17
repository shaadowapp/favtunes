package com.shaadow.tunes.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.utils.CountryDetector
import androidx.compose.material3.Card
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.innertube.Innertube
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.QuickPicksSource
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.ShimmerHost
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.screens.home.PersonalizedRecommendations
import com.shaadow.tunes.ui.components.themed.TextPlaceholder
import com.shaadow.tunes.utils.TimeUtils
import com.shaadow.tunes.ui.items.AlbumItem
import com.shaadow.tunes.ui.items.ArtistItem
import com.shaadow.tunes.ui.items.ItemPlaceholder
import com.shaadow.tunes.ui.items.ListItemPlaceholder
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.ui.items.PlaylistItem
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.SnapLayoutInfoProvider
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.isLandscape
import com.shaadow.tunes.utils.quickPicksSourceKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.viewmodels.QuickPicksViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun QuickPicks(
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onOfflinePlaylistClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    val viewModel: QuickPicksViewModel = viewModel()
    val quickPicksSource by rememberPreference(quickPicksSourceKey, QuickPicksSource.Trending)
    val scope = rememberCoroutineScope()

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val itemSize = 108.dp + 2 * 8.dp
    val quickPicksLazyGridState = rememberLazyGridState()
    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp)

    // Only reload if source changes, not on every composition
    LaunchedEffect(quickPicksSource) {
        if (viewModel.relatedPageResult == null || viewModel.trending == null) {
            viewModel.loadQuickPicks(quickPicksSource = quickPicksSource)
        }
    }

    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor =
            if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f

        val density = LocalDensity.current

        val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            with(density) {
                SnapLayoutInfoProvider(
                    lazyGridState = quickPicksLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }
        }

        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 16.dp + playerPadding)
        ) {
            // Time-based greeting at the top
            Text(
                text = TimeUtils.getTimeBasedGreeting(),
                style = MaterialTheme.typography.displaySmall.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFFEC4899)
                        )
                    )
                ),
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
            )
            
            // Top 10 Trending Songs Section
            val context = LocalContext.current
            val countryCode = remember { CountryDetector.getCountryCode(context) }
            val countryName = remember { CountryDetector.getCountryName(countryCode) }
            
            Text(
                text = "Top 5 Trending in $countryName",
                style = MaterialTheme.typography.titleMedium,
                modifier = sectionTextModifier
            )
            
            // Trending songs grid
            viewModel.relatedPageResult?.getOrNull()?.songs?.take(6)?.let { trendingSongs ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(trendingSongs) { index, song ->
                        Box {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val mediaItem = song.asMediaItem
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(mediaItem)
                                        binder?.setupRadio(
                                            com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                        )
                                    }
                            )
                            
                            // Ranking overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progressive loading: Show basic content first, load details gradually
            viewModel.relatedPageResult?.getOrNull()?.let { related ->
                Text(
                    text = stringResource(id = R.string.quick_picks),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = sectionTextModifier
                )

                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(count = 4),
                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                ) {
                    viewModel.trending?.let { song ->
                        item {
                            LocalSongItem(
                                modifier = Modifier
                                    .animateItem()
                                    .width(itemInHorizontalGridWidth),
                                song = song,
                                onClick = {
                                    val mediaItem = song.asMediaItem
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(mediaItem)
                                    binder?.setupRadio(
                                        com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                    )
                                },
                                onLongClick = {
                                    menuState.display {
                                        NonQueuedMediaItemMenu(
                                            onDismiss = menuState::hide,
                                            mediaItem = song.asMediaItem,
                                            onRemoveFromQuickPicks = {
                                                query {
                                                    Database.clearEventsFor(song.id)
                                                }
                                            },
                                            onGoToAlbum = onAlbumClick,
                                            onGoToArtist = onArtistClick
                                        )
                                    }
                                }
                            )
                        }
                    }

                    items(
                        items = related?.songs?.dropLast(if (viewModel.trending == null) 0 else 1)
                            ?: emptyList(),
                        key = Innertube.SongItem::key
                    ) { song ->
                        SongItem(
                            modifier = Modifier
                                .animateItem()
                                .width(itemInHorizontalGridWidth),
                            song = song,
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            },
                            onLongClick = {
                                menuState.display {
                                    NonQueuedMediaItemMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = song.asMediaItem,
                                        onGoToAlbum = onAlbumClick,
                                        onGoToArtist = onArtistClick
                                    )
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Personalized Recommendations section with sticky horizontal scrolling
                PersonalizedRecommendations()
                
                Spacer(modifier = Modifier.height(32.dp))

                related?.albums?.let { albums ->
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(id = R.string.related_albums),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(
                            items = albums,
                            key = Innertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                modifier = Modifier.widthIn(max = itemSize),
                                album = album,
                                onClick = { onAlbumClick(album.key) }
                            )
                        }
                    }
                }

                related?.artists?.let { artists ->
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(id = R.string.similar_artists),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(
                            items = artists,
                            key = Innertube.ArtistItem::key,
                        ) { artist ->
                            ArtistItem(
                                modifier = Modifier.widthIn(max = itemSize),
                                artist = artist,
                                onClick = { onArtistClick(artist.key) }
                            )
                        }
                    }
                }

                related?.playlists?.let { playlists ->
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(id = R.string.recommended_playlists),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(
                            items = playlists,
                            key = Innertube.PlaylistItem::key,
                        ) { playlist ->
                            PlaylistItem(
                                modifier = Modifier.widthIn(max = itemSize),
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist.key) }
                            )
                        }
                    }
                }

                Unit
            } ?: viewModel.relatedPageResult?.exceptionOrNull()?.let {
                Text(
                    text = stringResource(id = R.string.home_error),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                )

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.loadQuickPicks(quickPicksSource)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(id = R.string.retry)
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(text = stringResource(id = R.string.retry))
                    }

                    FilledTonalButton(
                        onClick = onOfflinePlaylistClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DownloadForOffline,
                            contentDescription = stringResource(id = R.string.offline)
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(text = stringResource(id = R.string.offline))
                    }
                }
            } ?: if (viewModel.isLoading) {
                // Show loading only when no data is available
                Text(
                    text = stringResource(id = R.string.quick_picks),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = sectionTextModifier
                )
                
                ShimmerHost {
                    repeat(4) {
                        ListItemPlaceholder()
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.spacer))
                    TextPlaceholder(modifier = sectionTextModifier)
                    
                    Row(modifier = Modifier.padding(start = 8.dp)) {
                        repeat(2) {
                            ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                        }
                    }
                }
            } else {
                // Empty state
            }
            
            // Footer section - moved from About screen
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "PROUDLY",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "INDIAN",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "APP",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "MADE WITH ❤️ AND SUPPORT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    
                    // App Version
                    val context = LocalContext.current
                    val versionName = remember {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                context.packageManager.getPackageInfo(
                                    context.packageName,
                                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                                ).versionName
                            } else {
                                @Suppress("DEPRECATION")
                                context.packageManager.getPackageInfo(
                                    context.packageName,
                                    0
                                ).versionName
                            }
                        } catch (e: Exception) {
                            "Unknown Version"
                        }
                    }
                    
                    Text(
                        text = "APP VERSION: $versionName",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }
}
