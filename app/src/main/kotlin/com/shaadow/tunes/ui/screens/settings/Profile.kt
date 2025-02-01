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

    var currentImage by remember { mutableIntStateOf(R.drawable.m_user_1) } // Default image


    var userData by remember { mutableStateOf<UserEntity?>(null) }

    // Fetch user details when the screen loads
    LaunchedEffect(Unit) {
        userViewModel.getOrCreateUser { user ->
            userData = user
        }
    }


    userData?.let { user ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture (Clickable)
            Image(
                painter = painterResource(id = currentImage),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                //.clickable { showDialog = true } // Show dialog on click
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Username
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = user.username, fontSize = 25.sp, fontWeight = FontWeight(600))
            }



            Spacer(modifier = Modifier.height(50.dp))


            // Public Key
            Column(horizontalAlignment = Alignment.Start) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(0.dp)
                ) {
                    Text(text = "Public Key: ", fontSize = 15.sp)

                    Text(
                        text = if (showKey) "Hide Key" else "Show Key",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showKey = !showKey }
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = if (showKey) user.publicKey else "******************",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(50.dp))
                        .border(2.dp, Color.DarkGray, shape = RoundedCornerShape(50.dp))
                        .padding(18.dp, 6.dp),
                    color = Color.Black
                )
            }



            Spacer(modifier = Modifier.height(20.dp))

            // Private key
            Column(horizontalAlignment = Alignment.Start) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(0.dp)
                ) {
                    Text(text = "Private Key: ", fontSize = 15.sp)

                    Text(
                        text = if (showPin) "Hide Key" else "Show Key",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showPin = !showPin }
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = if (showPin) user.privateKey else "******************",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(50.dp))
                        .border(2.dp, Color.DarkGray, shape = RoundedCornerShape(50.dp))
                        .padding(18.dp, 6.dp),
                    color = Color.Black
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // Device details
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "Device details: ", fontSize = 15.sp)

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = user.deviceModel,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Account creation
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "Account created at: ", fontSize = 15.sp)

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = user.getFormattedDate(),
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Black
                )
            }


            Spacer(modifier = Modifier.height(35.dp))

            InfoInformation(text = "This new-age login system ensures security without passwords. Your private key stays safe while you log in using just a username and PIN.")
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
