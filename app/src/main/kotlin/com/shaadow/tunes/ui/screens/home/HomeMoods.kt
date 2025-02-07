package com.shaadow.tunes.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shaadow.tunes.ui.components.CategoryCard
import com.shaadow.tunes.ui.screens.search.explore.MusicCategories

@Composable
fun HomeMoods(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Move remember inside the @Composable function
    val categories by remember {
        mutableStateOf(MusicCategories.allCategories.shuffled().take(6))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Header Row with Title and See All button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore Music",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { navController.navigate("explore") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight(600)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = "See all categories",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Grid
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onCategoryClick = { searchQuery ->
                        navController.navigate("search?initialQuery=${Uri.encode(searchQuery)}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}