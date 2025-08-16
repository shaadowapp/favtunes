package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.R
import com.shaadow.tunes.viewmodels.UserViewModel
import androidx.compose.runtime.*
import com.shaadow.tunes.database.UserEntity


@Composable
fun ProfileScreen(userViewModel: UserViewModel = viewModel()) {
    var showKey by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var currentImage by remember { mutableIntStateOf(R.drawable.m_user_1) }
    var userData by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.getOrCreateUser { user ->
            userData = user
        }
    }

    userData?.let { user ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Image(
                        painter = painterResource(id = currentImage),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { showDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Username
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Tap profile picture to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            // Security Keys Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Security Keys",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Public Key
                    KeyItem(
                        title = "Public Key",
                        value = user.publicKey,
                        isVisible = showKey,
                        onToggleVisibility = { showKey = !showKey }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Private Key
                    KeyItem(
                        title = "Private Key",
                        value = user.privateKey,
                        isVisible = showPin,
                        onToggleVisibility = { showPin = !showPin }
                    )
                }
            }



            // Account Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Account Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    InfoRow("Device", user.deviceModel)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Created", user.getFormattedDate())
                }
            }

            InfoInformation(text = "This new-age login system ensures security without passwords and emails. Your public-private key stays safe inside your device without any usage restriction and third-party interference.")
        }

        // Show the popup when user clicks the profile picture
        if (showDialog) {
            ProfilePictureDialog(
                onDismiss = { showDialog = false },
                onImageSelected = { newImage ->
                    currentImage = newImage
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun KeyItem(
    title: String,
    value: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = if (isVisible) "Hide" else "Show",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isVisible) value else "••••••••••••••••",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfilePictureDialog(onDismiss: () -> Unit, onImageSelected: (Int) -> Unit) {
    val images = listOf(
        R.drawable.m_user_1, // Default
        R.drawable.m_user_2,
        R.drawable.m_user_3,
        R.drawable.f_user_1,
        R.drawable.f_user_2,
        R.drawable.f_user_3,
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Choose Profile Picture", fontSize = 18.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Display images in 2 rows, 3 per row
                images.chunked(3).forEach { rowImages ->
                    Row {
                        rowImages.forEach { imageRes ->
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Profile Option",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable { onImageSelected(imageRes) }
                                    .padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}
