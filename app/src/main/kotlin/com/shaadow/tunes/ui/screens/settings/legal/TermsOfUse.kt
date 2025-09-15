package com.shaadow.tunes.ui.screens.settings.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfUse() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = 64.dp,
                bottom = 120.dp,
                start = 24.dp,
                end = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Text(
            text = "Terms of Use",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "1. Acceptance of Terms",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "By downloading, installing, or using FavTunes, you agree to be bound by these Terms of Use. If you do not agree to these terms, please do not use our application.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "2. Description of Service",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "FavTunes is a music streaming application that provides access to music content through YouTube Music's public API. The app is designed for personal, non-commercial use only.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "3. User Responsibilities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "• You must comply with all applicable laws and regulations\n• You are responsible for your own internet connection and data usage\n• You must not attempt to reverse engineer or modify the application\n• You must not use the app for any commercial purposes",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "4. Intellectual Property",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "All music content is owned by respective artists and labels. FavTunes does not claim ownership of any music content. The application code is open source and available under the specified license.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "5. Disclaimer of Warranties",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "FavTunes is provided \"as is\" without any warranties. We do not guarantee uninterrupted service or that the app will be error-free. Use at your own risk.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "6. Limitation of Liability",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "In no event shall FavTunes developers be liable for any indirect, incidental, special, or consequential damages arising from your use of the application.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "7. Changes to Terms",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "We reserve the right to modify these terms at any time. Continued use of the application after changes constitutes acceptance of the new terms.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "8. Contact Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "For questions about these Terms of Use, please contact us through the app's feedback feature or visit our GitHub repository.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Last updated: ${java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}