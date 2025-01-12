package com.shaadow.tunes.ui.screens.settings.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R

@Composable
fun TermsOfUse() {
    val playerPadding = LocalPlayerPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Introduction
        Text(
            text = "Welcome to HyTunes, a music streaming platform developed and operated by Shaadow Platforms. By accessing or using the HyTunes app, you agree to comply with the following Terms and Conditions. Please read them carefully before using our service. If you do not agree with these terms, you must not use the app.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )



        // Sections Content
        Text(text = "1. General Information",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes is a music streaming app that allows users to listen to audio content from the YouTube Music API. Our platform provides a curated music listening experience without offering any download features, subscriptions, or advertisements. We are not responsible for any third-party content that may be accessed through external links.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n2. Streaming Services",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes is a music streaming platform and does not provide any functionality for downloading or storing songs. We offer access to a wide variety of audio content that streams directly from YouTube Music. Our service is limited to streaming audio only, and we do not support other media types (such as video or other file extensions).",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n3. Login System",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "(a) Traditional Login System",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp)) // added padding from left
        Text(text = "A traditional login system typically requires users to create an account using a username, email, and password. The credentials are stored on a server to authenticate users and grant access to the platform.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 9.dp))

        Text(text = "(b) Automated Login System",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp)) // added padding from left
        Text(text = "Our platform uses an innovative, crypto-based automated login system. This system uses a **private-public key** approach to authenticate users securely and privately without the need for traditional login credentials. Your device generates a unique **public key** that interacts with your **private key** stored securely on the device to provide access. This system is designed to be fast, easy, and protect your privacy.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 9.dp))

        Text(text = "\n4. Why We Do Not Have Ads or Subscriptions",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes is committed to providing a **clean, ad-free experience** for users. We do not monetize the platform through advertisements or subscriptions. Our goal is to give users a hassle-free experience while enjoying music. As a free service, we rely on our users' satisfaction and engagement rather than traditional monetization strategies such as subscriptions or ads.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n5. How HyTunes Provides Streaming Services",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes offers streaming services by integrating the YouTube Music API, allowing users to access a vast library of music from YouTube's platform directly through our app. We do not host or store any audio files on our servers. All audio content is streamed in real-time via the API provided by YouTube Music, ensuring that we comply with their terms and conditions.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n6. Legal Liability and User Responsibility",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "While HyTunes strives to provide a seamless music streaming experience, we **do not take responsibility** for any illegal activities conducted by users of the platform. By using our app, you acknowledge that you are solely responsible for any actions you take within the platform. Any illegal or unauthorized activities, such as using third-party tools or services to download content, are the responsibility of the user, not of HyTunes or Shaadow Platforms.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n7. Limitation of Liability",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "To the fullest extent permitted by law, Shaadow Platforms and HyTunes will not be liable for any indirect, incidental, special, or consequential damages arising from your use of this platform. We are not responsible for the content provided by third-party services such as YouTube Music, nor can we guarantee the accuracy or availability of such content.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n8. Personal Liability",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes operates on the premise that all users are **individually responsible** for their actions within the platform. If you choose to access content, share links, or engage in any activities within the app, you do so at your own risk. **You are personally liable** for any activities that violate the rights of others, including but not limited to copyright infringement, and you agree to indemnify HyTunes and Shaadow Platforms from any claims resulting from your actions.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n9. Modification of Terms",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "Shaadow Platforms reserves the right to update, modify, or revise these Terms and Conditions at any time. Any changes will be posted on this page with an updated revision date. By continuing to use the platform after such updates, you agree to the modified Terms and Conditions.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n10. No Copyright Infringement",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "HyTunes only offers audio streaming services using content sourced directly from YouTube Music. We do not upload or distribute any copyrighted content. Users of the platform should ensure that their actions are compliant with all applicable copyright laws. We do not encourage or support any illegal downloading or redistribution of content from our platform.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))

        Text(text = "\n11. Governing Law",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
        Text(text = "These Terms and Conditions shall be governed by and construed in accordance with the laws of the country in which Shaadow Platforms is located, without regard to its conflict of law principles. Any legal disputes will be subject to the exclusive jurisdiction of the courts in that country.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))


        

        // Footer
        Text(
            text = "\nIf you have any questions or concerns about these Terms and Conditions, please contact us at support@shaadow.in",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "\nLast Updated: January 11, 2025\n\n",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier.fillMaxWidth())
    }
}
