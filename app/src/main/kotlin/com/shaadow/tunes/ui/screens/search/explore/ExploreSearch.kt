package com.shaadow.tunes.ui.screens.search.explore

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme // Added for typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight // Added for text styling
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shaadow.tunes.ui.components.CategoryCard

data class MusicCategory(
    val name: String,
    val searchQuery: String,
    val gradientColors: List<Color>
)

@Composable
fun ExploreSearch(navController: NavController) {
    // Get screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Calculate grid height (screen height minus padding and header)
    val gridHeight = screenHeight - 120.dp // Adjust this value based on your header and padding


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom=16.dp, start = 16.dp, end = 16.dp, top = 55.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Search Button
            Button(
                onClick = { navController.navigate("search") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .height(gridHeight)
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 70.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(MusicCategories.allCategories) { category ->
                    CategoryCard(
                        category = category,
                        onCategoryClick = { searchQuery ->
                            // Changed to use initialQuery parameter for consistency with SearchScreen
                            navController.navigate("search?initialQuery=${Uri.encode(searchQuery)}") {
                                // Added navigation options for better UX
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}