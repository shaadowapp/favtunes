package com.shaadow.tunes.ui.screens.settings.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.LocalPlayerPadding

@Composable
fun PrivacyPolicy() {
    val playerPadding = LocalPlayerPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "HyTunes is a music streaming platform developed and operated by Shaadow Platforms. This Privacy Policy outlines the collection, use, and protection of personal information when you use our services. By using HyTunes, you consent to the collection and use of information in accordance with this policy.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )



//        Content
        Text(
            text = "1. Information Collection and Use",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "We may collect personally identifiable information, including but not limited to email addresses. This information is used exclusively for providing and improving our services, and is not shared or sold to third parties unless explicitly stated in this policy.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth())

        Text(
            text = "\n2. Log Data",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "In case of errors, we may collect data and information through third-party services on your device, including but not limited to device IP address, device name, operating system version, and usage statistics.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n3. Cookies",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "While we do not use cookies explicitly, third-party services integrated into HyTunes may use cookies to enhance user experience. Users may choose to disable cookies through their browser settings, although this may affect app functionality.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n4. Service Providers",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "We may employ third-party companies and individuals to facilitate our services, provide support, or analyze usage data. These third parties have access to your personal information to perform assigned tasks but are not authorized to use it for any other purposes.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n5. Security",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "We employ commercially acceptable methods to protect your personal information. However, no transmission method over the internet is 100% secure, and we cannot guarantee absolute security.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n6. Links to Other Sites",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "Our service may contain links to third-party websites. We are not responsible for the privacy practices or content of these external sites. Please review the privacy policy of any third-party sites you visit.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n7. Children’s Privacy",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "We do not knowingly collect personal information from children. We encourage parents and guardians to monitor their children's use of the app and ensure they do not provide personal information without consent.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n8. Changes to This Privacy Policy",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "We reserve the right to update this Privacy Policy periodically. Any changes will be posted here with an updated revision date. Continued use of HyTunes constitutes acceptance of these changes.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n9. Legal Liability",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "As an MSME registered startup, Shaadow Platforms complies with all applicable laws regarding the collection, use, and protection of personal information. We are not liable for any unauthorized access or actions of third-party service providers integrated into the platform.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n10. No Copyright Infringement",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "HyTunes uses the YouTube Music API to provide streaming services and does not host or distribute any copyrighted content. Users are responsible for ensuring their actions comply with copyright laws.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(
            text = "\n11. Governing Law",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "This Privacy Policy is governed by the laws of the country in which Shaadow Platforms is registered. Any disputes will be subject to the exclusive jurisdiction of the courts in that country.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(text= "\n12. Automated Login System vs. Traditional Login Systems",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth())
            Text(text = "HyTunes uses an innovative, automated login system that does not require users to input personal credentials, such as emails or passwords, to access the app. Below, we clarify how this system differs from traditional login mechanisms, and why certain information may still be requested for other purposes.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp))

        Text(text = "(a) Understanding HyTunes’ Automated Login System",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp)) // added padding from left
            Text(text = "HyTunes introduces a cutting-edge, automated login system designed to prioritize user convenience and privacy, eliminating the need for traditional credentials like email addresses or passwords. Unlike traditional login systems that rely on storing personal information for authentication, our system assigns device-specific identifiers and anonymized tokens to provide seamless access without creating user accounts. While we proudly market HyTunes as a platform that requires no login or signup for its core features, there are instances where we may request user information, such as email addresses or names, for purposes unrelated to the login process. These requests are strictly voluntary and occur only when addressing specific operational needs, such as resolving critical bugs, troubleshooting device-specific issues, enhancing AI-driven features, collecting feedback, or developing a robust help center to support users. Importantly, any such information is never tied to account creation or authentication, ensuring that our commitment to 'No login or signup required' remains intact. By using the app, users acknowledge that these requests, when made, serve to enhance functionality and user experience while maintaining the integrity of their privacy.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 9.dp))

        Text(text = "(b) Why Certain Information May Be Requested",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp))
            Text(text = "While HyTunes does not require personal credentials for login or signup, there may be situations where additional information, such as an email address or name, could be requested for reasons unrelated to the login process. Such instances include, but are not limited to:",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 9.dp))

                Text(text = "(i) Serious Bugs",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF155E95), // blue color
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 15.dp))
                    Text(text = "If a critical bug is identified in the app, we may ask for user feedback or device information to resolve the issue efficiently.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))

                Text(text = "(ii) Device-Specific Issues",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF155E95), // blue color
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 15.dp))
                    Text(text = "In rare cases where a device encounters app-related errors, we may request contact details to troubleshoot the issue effectively.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))

                Text(text = "(iii) Feedback Collection",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF155E95), // blue color
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 15.dp))
                    Text(text = "To improve the app’s features and user experience, we may request voluntary feedback.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))

                Text(text = "(iv) Help Center Development",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF155E95), // blue color
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 15.dp))
                    Text(text = "We aim to create a comprehensive help center for resolving user problems quickly. Collecting occasional input from users helps us address common concerns.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))

                Text(text = "(v) AI-Driven Features",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF155E95), // blue color
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 15.dp))
                    Text(text = "Some advanced features, such as AI-based personalization, may require limited input from users to optimize functionality.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))


        Text(text = "(c) Outcomes and Necessity",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp))
            Text(text = "The automated login system is designed to prioritize user convenience and privacy by removing the dependency on personal credentials for account management. Any additional information requested by HyTunes is strictly for operational or improvement-related purposes and is not linked to the login or signup process.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 9.dp))

        Text(text = "(d) Marketing and Legal Clarifications",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp))
            Text(text = "HyTunes markets itself as a platform requiring no login or signup to use its core features. It is essential to clarify that any request for user information, such as an email address or name, is unrelated to the login process. These requests are made exclusively for operational, troubleshooting, feedback collection, or feature enhancement purposes.\n" +
                        "To this end:",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 15.sp,
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 9.dp))
                    Text(text = "(i) HyTunes does not use such information to create user accounts.",style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 15.dp))
                    Text(text = "(ii) There is no requirement to provide this information to access music streaming services.",style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))
                    Text(text = "(iii) Providing additional information is strictly voluntary and situational, ensuring compliance with the app’s marketing claims.",style = MaterialTheme.typography.bodySmall,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 15.dp))


        Text(text = "(e) Defense Against Misinformation",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF155E95), // blue color
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp))
        Text(text = "HyTunes operates with full transparency and adherence to its privacy commitments. While the app may request specific information for certain operational reasons, it does not contradict its claim of ‘No login or signup required.’ The requests for such information are context-dependent and not a prerequisite for using the app’s primary features. By using the app, users acknowledge and agree to these terms.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 15.sp,
            fontWeight = FontWeight(400),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 9.dp))





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
