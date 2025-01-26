package com.shaadow.tunes.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Function to represent a single feature button
@Composable
fun FeatureButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(190.dp)
            .height(60.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.Gray),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 16.sp, color = Color.Gray)
        }
    }
}


// The main composable function for the screen
@Composable
    fun More(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grid with two buttons in each row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureButton(
                    icon = Icons.Outlined.Feedback,
                    label = "Feedback",
                    onClick = {
                        // Navigate to Feedback screen
                        navController.navigate("feedback")
                    }
                )

                FeatureButton(
                    icon = Icons.Outlined.Notifications,
                    label = "Messages",
                    onClick = {
                        // Navigate to Feedback screen
                        navController.navigate("messages")
                    }
                )
            }
        }
    }
}