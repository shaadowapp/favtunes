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
fun PrivacyPolicy() {
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
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Our Commitment to Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "FavTunes is designed with privacy in mind. We believe your music preferences and listening habits should remain private and under your control.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Information We Collect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "• Music preferences you set during onboarding\n• Songs you play, like, or add to playlists\n• App usage patterns for improving recommendations\n• Device information for app functionality\n\nAll this data is stored locally on your device and is never transmitted to external servers.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "How We Use Your Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "• Provide personalized music recommendations\n• Improve app performance and user experience\n• Remember your preferences and settings\n• Generate listening statistics and insights\n\nYour data is processed entirely on your device using local algorithms.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Data Storage and Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "• All personal data is stored locally on your device\n• No data is transmitted to external servers\n• Data is protected by your device's security measures\n• You can clear all data anytime through app settings",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Third-Party Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "FavTunes uses YouTube Music's public API to stream music content. When you stream music, you are subject to YouTube's privacy policy. We do not share your personal data with YouTube or any other third parties.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Your Rights and Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "• View all your stored data through app settings\n• Delete specific preferences or all data\n• Export your listening data\n• Disable data collection features\n• Uninstall the app to remove all data",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Children's Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "FavTunes does not knowingly collect personal information from children under 13. If you are a parent and believe your child has provided personal information, please contact us.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Changes to Privacy Policy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy in the app. Changes are effective immediately upon posting.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Contact Us",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "If you have any questions about this Privacy Policy, please contact us through the app's feedback feature or visit our GitHub repository for more information.",
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