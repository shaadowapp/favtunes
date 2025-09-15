package com.shaadow.tunes.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.utils.SecureCredentialsManager

/**
 * Setup screen for configuring secure credentials
 * Only shown on first app launch or when credentials need to be updated
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val credentialsManager = remember { SecureCredentialsManager.getInstance(context) }
    
    var oneSignalAppId by remember { mutableStateOf("") }
    var githubToken by remember { mutableStateOf("") }
    var remoteConfigRepo by remember { mutableStateOf("") }
    var remoteConfigOwner by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Initial Setup",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Configure secure credentials for app features",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = oneSignalAppId,
            onValueChange = { oneSignalAppId = it },
            label = { Text("OneSignal App ID (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            supportingText = { Text("Required for push notifications") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = githubToken,
            onValueChange = { githubToken = it },
            label = { Text("GitHub Token (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Required for remote configuration") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = remoteConfigRepo,
            onValueChange = { remoteConfigRepo = it },
            label = { Text("Config Repository (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Repository name for remote config") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = remoteConfigOwner,
            onValueChange = { remoteConfigOwner = it },
            label = { Text("Config Owner (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("GitHub username/organization") }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // Skip setup - app will work with limited functionality
                    onSetupComplete()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Skip Setup")
            }
            
            Button(
                onClick = {
                    isLoading = true
                    
                    // Save credentials securely
                    if (oneSignalAppId.isNotBlank()) {
                        credentialsManager.setOneSignalAppId(oneSignalAppId)
                    }
                    if (githubToken.isNotBlank()) {
                        credentialsManager.setGitHubToken(githubToken)
                    }
                    if (remoteConfigRepo.isNotBlank()) {
                        credentialsManager.setRemoteConfigRepo(remoteConfigRepo)
                    }
                    if (remoteConfigOwner.isNotBlank()) {
                        credentialsManager.setRemoteConfigOwner(remoteConfigOwner)
                    }
                    
                    onSetupComplete()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save & Continue")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "All credentials are stored securely using Android's encrypted storage. You can update these later in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}