package com.shaadow.tunes.ui.screens.bugreport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.viewmodels.BugReportViewModel
import com.shaadow.tunes.models.BugSeverity
import com.shaadow.tunes.models.BugCategory
import com.shaadow.tunes.utils.NetworkMonitor
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: BugReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = networkMonitor.isCurrentlyConnected())
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSeverityDropdown by remember { mutableStateOf(false) }
    var currentReproductionStep by remember { mutableStateOf("") }
    
    val categories = BugCategory.values().toList()
    val severities = BugSeverity.values().toList()
    
    // Handle successful submission
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Bug") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Help us improve the app",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Your bug reports help us identify and fix issues. Please provide as much detail as possible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Network Status Indicator
            if (!isConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "You're offline. Your bug report will be saved and submitted when you're back online.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
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
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Form Section
            Text(
                text = "Bug Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Title Field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Bug Title *") },
                placeholder = { Text("Brief description of the issue") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Category and Severity Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selection
                Box(modifier = Modifier.weight(1f)) {
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
                        modifier = Modifier.fillMaxWidth()
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
                Box(modifier = Modifier.weight(1f)) {
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = showSeverityDropdown,
                        onDismissRequest = { showSeverityDropdown = false }
                    ) {
                        severities.forEach { severity ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            when (severity) {
                                                BugSeverity.LOW -> Icons.Default.Info
                                                BugSeverity.MEDIUM -> Icons.Default.Warning
                                                BugSeverity.HIGH -> Icons.Default.Error
                                                BugSeverity.CRITICAL -> Icons.Default.ErrorOutline
                                            },
                                            contentDescription = null,
                                            tint = when (severity) {
                                                BugSeverity.LOW -> MaterialTheme.colorScheme.primary
                                                BugSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                                BugSeverity.HIGH -> MaterialTheme.colorScheme.error
                                                BugSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(severity.name)
                                    }
                                },
                                onClick = {
                                    viewModel.updateSeverity(severity)
                                    showSeverityDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description *") },
                placeholder = { Text("Describe what happened, what you expected, and any other relevant details...") },
                minLines = 4,
                maxLines = 6,
                isError = uiState.descriptionError != null,
                supportingText = uiState.descriptionError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // Reproduction Steps Section
            Text(
                text = "Reproduction Steps (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Help us reproduce the issue by listing the steps you took",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentReproductionStep,
                    onValueChange = { currentReproductionStep = it },
                    label = { Text("Add step") },
                    placeholder = { Text("e.g., Tap on the play button") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (currentReproductionStep.isNotBlank()) {
                            viewModel.addReproductionStep(currentReproductionStep)
                            currentReproductionStep = ""
                        }
                    },
                    enabled = currentReproductionStep.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
            
            // Display reproduction steps
            uiState.reproductionSteps.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = step,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { viewModel.removeReproductionStep(index) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove step",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            uiState.reproductionStepsError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Device Info Section
            Text(
                text = "Device Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )
            
            Text(
                text = "This information helps us understand your environment",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeviceInfoRow("Device", uiState.deviceInfo.deviceModel)
                    DeviceInfoRow("OS Version", uiState.deviceInfo.osVersion)
                    DeviceInfoRow("App Version", uiState.deviceInfo.appVersion)
                    DeviceInfoRow("Network", uiState.deviceInfo.networkType)
                    DeviceInfoRow("Screen", uiState.deviceInfo.screenResolution)
                }
            }
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.submitBugReport()
                },
                enabled = viewModel.isFormValid() && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else if (uiState.isSubmitted) {
                    Icon(Icons.Default.Check, contentDescription = null)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = when {
                        uiState.isLoading -> "Submitting Bug Report..."
                        uiState.isSubmitted -> "Bug Report Submitted"
                        else -> "Submit Bug Report"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String
) {
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