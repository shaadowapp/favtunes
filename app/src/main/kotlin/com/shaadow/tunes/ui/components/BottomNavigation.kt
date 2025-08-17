package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shaadow.tunes.enums.NavigationLabelsVisibility
import com.shaadow.tunes.models.Screen
import com.shaadow.tunes.utils.homeScreenTabIndexKey
import com.shaadow.tunes.utils.navigationLabelsVisibilityKey
import com.shaadow.tunes.utils.rememberPreference

@Composable
fun BottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val navigationLabelsVisibility by rememberPreference(
        navigationLabelsVisibilityKey,
        NavigationLabelsVisibility.Visible
    )
    val (_, onScreenChanged) = rememberPreference(
        homeScreenTabIndexKey,
        defaultValue = 0
    )

    val homeScreens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Explore,
        Screen.Playlists,
        Screen.MyTunes
    )

    NavigationBar(
        modifier = if (navigationLabelsVisibility == NavigationLabelsVisibility.Hidden) Modifier.heightIn(
            max = 95.dp
        ) else Modifier
    ) {
        homeScreens.forEachIndexed { index, screen ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onScreenChanged(index)
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = stringResource(id = screen.resourceId)
                    )
                },
                label = {
                    if (navigationLabelsVisibility != NavigationLabelsVisibility.Hidden) {
                        Text(
                            text = stringResource(id = screen.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                alwaysShowLabel = navigationLabelsVisibility != NavigationLabelsVisibility.VisibleWhenActive
            )
        }
    }
}