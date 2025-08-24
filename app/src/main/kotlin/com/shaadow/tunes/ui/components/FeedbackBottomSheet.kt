package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Feedback
import com.shaadow.tunes.utils.ScreenDetector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.viewmodels.FeedbackViewModel
import com.shaadow.tunes.models.FeedbackCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    viewModel: FeedbackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    val categories = viewModel.getFeedbackCategories()
    
    // Handle successful submission
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onDismiss()
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Feedback,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Send Feedback",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
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
            
            // Rating Section
            Text(
                text = "How would you rate your experience?",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    IconButton(
                        onClick = { viewModel.updateRating(starIndex) }
                    ) {
                        Icon(
                            if (starIndex <= uiState.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Rate $starIndex stars",
                            tint = if (starIndex <= uiState.rating) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            if (uiState.rating > 0) {
                Text(
                    text = viewModel.getRatingDescription(uiState.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            uiState.ratingError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
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
            
            // Message Field
            OutlinedTextField(
                value = uiState.message,
                onValueChange = viewModel::updateMessage,
                label = { Text("Your feedback") },
                placeholder = { Text("Tell us what you think, suggest improvements, or report issues...") },
                minLines = 4,
                maxLines = 6,
                isError = uiState.messageError != null,
                supportingText = uiState.messageError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Anonymous Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleAnonymous(!uiState.isAnonymous) }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = uiState.isAnonymous,
                    onCheckedChange = viewModel::toggleAnonymous
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Submit anonymously",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = if (uiState.isAnonymous) 
                    "Your feedback will be submitted without any identifying information." 
                else 
                    "Device information will be included to help us improve the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.submitFeedback()
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
                        else -> "Send Feedback"
                    }
                )
            }
        }
    }
}