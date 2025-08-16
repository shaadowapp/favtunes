package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Send
import com.shaadow.tunes.utils.ScreenDetector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.os.Build



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportBottomSheet(
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    var bugDescription by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var showPriority by remember { mutableStateOf(false) }
    var selectedScreen by remember { mutableStateOf(ScreenDetector.getCurrentScreenName(navController)) }
    var showScreenDropdown by remember { mutableStateOf(false) }
    var customScreenName by remember { mutableStateOf("") }
    var showCustomScreen by remember { mutableStateOf(false) }
    
    val priorities = listOf("Low", "Medium", "High", "Critical")
    val screenNames = ScreenDetector.getAllScreenNames()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.heightIn(min = LocalConfiguration.current.screenHeightDp.dp * 0.7f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Report Bug",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Tip 💡: you can open Report bug screen by shaking your phone",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Screen Detection
            Box {
                OutlinedTextField(
                    value = selectedScreen,
                    onValueChange = { },
                    label = { Text("Screen") },
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
            
            // Optional Priority Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Checkbox(
                    checked = showPriority,
                    onCheckedChange = { showPriority = it }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Set Priority (optional)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            if (showPriority) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    priorities.forEach { priority ->
                        FilterChip(
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) },
                            selected = selectedPriority == priority
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = bugDescription,
                onValueChange = { bugDescription = it },
                label = { Text("Describe the bug") },
                placeholder = { Text("What happened? Steps to reproduce...") },
                minLines = 3,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Log Data Section
            Text(
                text = "System Log Data",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val context = LocalContext.current
            val logData = remember(selectedScreen) {
                val appVersion = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags.of(0)
                        ).versionName ?: "Unknown"
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        ).versionName ?: "Unknown"
                    }
                } catch (e: Exception) {
                    "Unknown"
                }
                
                "Screen: $selectedScreen\n" +
                "Device: ${android.os.Build.MODEL}\n" +
                "Android: ${android.os.Build.VERSION.RELEASE}\n" +
                "App Version: $appVersion\n" +
                "Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
            }
            
            OutlinedTextField(
                value = logData,
                onValueChange = { },
                readOnly = true,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            Button(
                onClick = {
                    // TODO: Handle bug report submission
                    onDismiss()
                },
                enabled = bugDescription.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send Report")
            }
        }
    }
}