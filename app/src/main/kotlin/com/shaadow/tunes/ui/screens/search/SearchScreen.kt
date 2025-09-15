package com.shaadow.tunes.ui.screens.search

import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.ui.components.SearchInputField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaadow.innertube.Innertube

import com.shaadow.innertube.requests.searchSuggestions
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.models.SearchQuery
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.pauseSearchHistoryKey
import com.shaadow.tunes.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchScreen(
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    initialQuery: String = ""  // Already existed, used for category search
) {
    val playerPadding = LocalPlayerPadding.current

    val context = LocalContext.current
    var history: List<SearchQuery> by remember { mutableStateOf(emptyList()) }
    var suggestionsResult: Result<List<String>?>? by remember { mutableStateOf(null) }

    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var expanded by rememberSaveable { mutableStateOf(initialQuery.isEmpty()) }
    // NEW: Added activeSearch state
    var activeSearch by rememberSaveable { mutableStateOf(false) }
    var searchText: String? by rememberSaveable { mutableStateOf(initialQuery.ifBlank { null }) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun onSearch(searchQuery: String) {
        query = searchQuery
        searchText = searchQuery
        expanded = false
        // NEW: Added these lines
        activeSearch = true
        keyboardController?.hide()

        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            query {
                Database.insert(SearchQuery(query = query))
            }
        }
    }

    // MODIFIED: Updated LaunchedEffect for initialQuery
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            query = initialQuery
            delay(100)
            activeSearch = true
            keyboardController?.hide()
        }
    }

    // UNCOMMENTED: These LaunchedEffect blocks
    LaunchedEffect(query) {
        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            Database.queries("%$query%")
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collect { history = it }
        }
    }

    LaunchedEffect(query) {
        suggestionsResult = if (query.isNotEmpty()) {
            delay(200)
            Innertube.searchSuggestions(input = query)
        } else null
    }

    // Screen identifier for accurate screen detection
    ScreenIdentifier(
        screenId = "search",
        screenName = "Search Screen"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val onExpandedChange: (Boolean) -> Unit = { expandedState ->
            if (searchText.isNullOrEmpty() && !expandedState) pop()
            else expanded = expandedState
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .focusable()
            )
        }

        SearchInputField(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                activeSearch = true
            },
            active = expanded,
            onActiveChange = { newExpanded ->
                if (!newExpanded && query.isEmpty()) {
                    pop()
                } else {
                    expanded = newExpanded
                }
            },
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = playerPadding)
            ) {
                items(
                    items = history,
                    key = SearchQuery::id
                ) { searchQuery ->
                    ListItem(
                        headlineContent = {
                            Text(text = searchQuery.query)
                        },
                        modifier = Modifier.clickable { onSearch(searchQuery.query) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(
                                    onClick = {
                                        query {
                                            Database.delete(searchQuery)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }

                                IconButton(
                                    onClick = { query = searchQuery.query },
                                    modifier = Modifier.rotate(225F)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }

                suggestionsResult?.getOrNull()?.let { suggestions ->
                    items(items = suggestions) { suggestion ->
                        ListItem(
                            headlineContent = {
                                Text(text = suggestion)
                            },
                            modifier = Modifier.clickable { onSearch(suggestion) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { query = suggestion },
                                    modifier = Modifier.rotate(225F)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    }
                } ?: suggestionsResult?.exceptionOrNull()?.let {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "An error has occurred.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .alpha(Dimensions.mediumOpacity)
                            )
                        }
                    }
                }
            }
        }

        // MODIFIED: Changed condition to use activeSearch
        if (activeSearch) {
            SearchResults(
                query = query,
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick
            )
        }
    }

    LaunchedEffect(Unit) {
        if (searchText.isNullOrEmpty()) focusRequester.requestFocus()
    }
}