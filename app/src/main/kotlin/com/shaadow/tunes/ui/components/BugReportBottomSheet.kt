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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.viewmodels.BugReportViewModel
import com.shaadow.tunes.models.BugSeverity
import com.shaadow.tunes.models.BugCategory
import android.os.Build



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportBottomSheet(
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    viewModel: BugReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSeverityDropdown by remember { mutableStateOf(false) }
    var currentReproductionStep by remember { mutableStateOf("") }
    
    val categories = BugCategory.values().toList()
    val severities = BugSeverity.values().toList()
    
    // Handle successful submission
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onDismiss()
        }
    }
    
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
            // Header
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
            
            // Tip Card
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
            
            // Title Field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Bug Title") },
                placeholder = { Text("Brief description of the issue") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Category Selection
            Box {
                OutlinedTextField(
                    value = uiState.category.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                viewModel.updateCategory(category)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Severity Selection
            Box {
                OutlinedTextField(
                    value = uiState.severity.name,
                    onValueChange = { },
                    label = { Text("Severity") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showSeverityDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                
                DropdownMenu(
                    expanded = showSeverityDropdown,
                    onDismissRequest = { showSeverityDropdown = false }
                ) {
                    severities.forEach { severity ->
                        DropdownMenuItem(
                            text = { Text(severity.name) },
                            onClick = {
                                viewModel.updateSeverity(severity)
                                showSeverityDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                placeholder = { Text("Describe what happened, what you expected, and steps to reproduce...") },
                minLines = 3,
                maxLines = 4,
                isError = uiState.descriptionError != null,
                supportingText = uiState.descriptionError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Reproduction Steps
            Text(
                text = "Reproduction Steps (optional)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = currentReproductionStep,
                    onValueChange = { currentReproductionStep = it },
                    label = { Text("Add step") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (currentReproductionStep.isNotBlank()) {
                            viewModel.addReproductionStep(currentReproductionStep)
                            currentReproductionStep = ""
                        }
                    },
                    enabled = currentReproductionStep.isNotBlank()
                ) {
                    Text("Add")
                }
            }
            
            // Display reproduction steps
            uiState.reproductionSteps.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. $step",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = { viewModel.removeReproductionStep(index) }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
            
            uiState.reproductionStepsError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Device Info Display
            Text(
                text = "Device Information",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Device: ${uiState.deviceInfo.deviceModel}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "OS: ${uiState.deviceInfo.osVersion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "App Version: ${uiState.deviceInfo.appVersion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Network: ${uiState.deviceInfo.networkType}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
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
                    Icon(Icons.Default.Send, contentDescription = null)
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