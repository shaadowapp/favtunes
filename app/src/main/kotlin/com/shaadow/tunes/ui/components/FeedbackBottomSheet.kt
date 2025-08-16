package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import com.shaadow.tunes.utils.ScreenDetector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    var feedbackText by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedScreen by remember { mutableStateOf(ScreenDetector.getCurrentScreenName(navController)) }
    var showScreenDropdown by remember { mutableStateOf(false) }
    var customScreenName by remember { mutableStateOf("") }
    var showCustomScreen by remember { mutableStateOf(false) }
    
    val screenNames = ScreenDetector.getAllScreenNames()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Feedback",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Optional Screen Selection
            Box {
                OutlinedTextField(
                    value = selectedScreen,
                    onValueChange = { },
                    label = { Text("Screen (optional)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showScreenDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                
                DropdownMenu(
                    expanded = showScreenDropdown,
                    onDismissRequest = { showScreenDropdown = false }
                ) {
                    screenNames.forEach { screen ->
                        DropdownMenuItem(
                            text = { Text(screen) },
                            onClick = {
                                if (screen == "Other") {
                                    showCustomScreen = true
                                } else {
                                    selectedScreen = screen
                                    showCustomScreen = false
                                }
                                showScreenDropdown = false
                            }
                        )
                    }
                }
            }
            
            if (showCustomScreen) {
                OutlinedTextField(
                    value = customScreenName,
                    onValueChange = { 
                        customScreenName = it
                        selectedScreen = it
                    },
                    label = { Text("Custom Screen Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
            
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Your feedback") },
                minLines = 4,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            Button(
                onClick = {
                    // TODO: Handle feedback submission
                    onDismiss()
                },
                enabled = feedbackText.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send")
            }
        }
    }
}