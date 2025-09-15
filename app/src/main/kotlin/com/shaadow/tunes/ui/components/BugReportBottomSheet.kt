package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import com.shaadow.tunes.utils.ScreenDetector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.viewmodels.BugReportViewModel
import com.shaadow.tunes.models.BugSeverity



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportBottomSheet(
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    viewModel: BugReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showSeverityDropdown by remember { mutableStateOf(false) }
    val severities = BugSeverity.values().toList()
    
    // Auto-detect screen context when sheet opens
    LaunchedEffect(Unit) {
        val screenContext = ScreenDetector.getCurrentScreenContext(navController)
        viewModel.setScreenContext(screenContext)
    }
    
    // Handle successful submission
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            // Reset the form after successful submission
            viewModel.resetForm()
            onDismiss()
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
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
            
            // Screen Context Info
            uiState.screenContext?.let { screenContext ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "📍 Reporting issue from: ${screenContext.screenName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Error Display
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Title Field - Fixed for space input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { newValue ->
                    // Debug logging for space input
                    android.util.Log.d("BugReportBottomSheet", "Title onValueChange: '$newValue'")
                    android.util.Log.d("BugReportBottomSheet", "Title contains spaces: ${newValue.contains(' ')}")
                    viewModel.updateTitle(newValue)
                },
                label = { Text("What went wrong?") },
                placeholder = { Text("Brief description of the issue") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = true
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Description Field - Fixed for proper text input with enhanced space handling
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { newValue ->
                    // Debug logging for space input
                    android.util.Log.d("BugReportBottomSheet", "TextField onValueChange: '$newValue'")
                    android.util.Log.d("BugReportBottomSheet", "Contains spaces: ${newValue.contains(' ')}")
                    android.util.Log.d("BugReportBottomSheet", "Length: ${newValue.length}")
                    
                    // Direct update without immediate processing to test space handling
                    viewModel.updateDescription(newValue)
                },
                label = { Text("Describe the issue") },
                placeholder = { Text("What happened? What did you expect? Include steps to reproduce if possible...") },
                minLines = 3,
                maxLines = 5,
                isError = uiState.descriptionError != null,
                supportingText = uiState.descriptionError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                    autoCorrectEnabled = true
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /* Handle done action */ }
                ),
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Severity Selection - Simplified
            Text(
                text = "How severe is this issue?",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                severities.forEach { severity ->
                    FilterChip(
                        onClick = { viewModel.updateSeverity(severity) },
                        label = { 
                            Text(
                                text = when(severity) {
                                    BugSeverity.LOW -> "Minor"
                                    BugSeverity.MEDIUM -> "Normal"
                                    BugSeverity.HIGH -> "Major"
                                    BugSeverity.CRITICAL -> "Critical"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = uiState.severity == severity
                    )
                }
            }
            
            // Device Info Display - Simplified
            Text(
                text = "📱 Device info will be included automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.submitBugReport()
                },
                enabled = viewModel.isFormValid() && !uiState.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else if (uiState.isSubmitted) {
                    Icon(Icons.Default.Check, contentDescription = null)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        uiState.isLoading -> "Submitting..."
                        uiState.isSubmitted -> "Submitted"
                        else -> "Send Report"
                    }
                )
            }
        }
    }
}