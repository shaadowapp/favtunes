package com.shaadow.tunes.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape



@Composable
fun Feedback() {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(start=16.dp, end=16.dp, bottom=16.dp, top = 100.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Feedback",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Email Address",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        // Email Field
        BasicTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.DarkGray, RectangleShape)
                .height(30.dp)
                .background(Color.LightGray),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (emailError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    innerTextField()
                }
            }
        )
        if (emailError) {
            Text(
                text = "Invalid email address",
                color = Color.Red,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your message",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        // Message Field
        BasicTextField(
            value = message,
            onValueChange = {
                message = it.take(250) // Limit message to 250 characters
                messageError = message.isEmpty()
            },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.DarkGray, RectangleShape)
                .background(Color.LightGray)
                .height(200.dp)
                .verticalScroll(rememberScrollState(),true, reverseScrolling = true),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (messageError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    innerTextField()
                }
            }
        )
        if (messageError) {
            Text(
                text = "Message cannot be empty",
                color = Color.Red,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && message.isNotEmpty()) {
                    // Simulate successful submission
                    showSuccessDialog = true
                }
            },
            enabled = email.isNotEmpty() && message.isNotEmpty()
        ) {
            Text(text = "Submit now")
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Thank you for your valuable feedback!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { showSuccessDialog = false }) {
                    Text("Close")
                }
            }
        }
    }
}