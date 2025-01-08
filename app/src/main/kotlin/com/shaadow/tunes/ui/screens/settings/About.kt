package com.shaadow.tunes.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.ui.styling.Dimensions
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.material3.Button


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@ExperimentalAnimationApi
@Composable
fun About() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val playerPadding = LocalPlayerPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding)
    ) {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
        val versionCode = packageInfo.versionName


        Icon(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(108.dp)
                .padding(top = 25.dp)
                .aspectRatio(1F),
            tint = Color.Red
        )

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "HyTunes is a powerful music player that lets you stream your favorite songs, and customize your audio experience with an equalizer. Enjoy seamless playback and intuitive controls, all wrapped in a sleek and modern design.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFFE2E2E2),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(Dimensions.spacer + 20.dp))

        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen
                .padding(16.dp),
            contentAlignment = Alignment.Center // Center content both vertically and horizontally
        ) {
            GoToLinkButton(
                url = "https://tunes.shaadow.in?utm_source=HyTunes_Android&utm_from=setting-about&utm_pkg=com-shaadow-tunes",
                buttonText = "Our official website"
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.spacer + 25.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top= 15.dp,start=18.dp) // Add padding around the edges
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart) // Align text at the bottom-left corner
                    .padding(bottom = 16.dp), // Add bottom padding if needed
                horizontalAlignment = Alignment.Start // Ensure left alignment for text
            ) {
                // First text: "PROUDLY INDIAN APP" - Big size, faded color
                Text(
                    text = "PROUDLY",
                    fontSize = 50.sp, // Big font size
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE2E2E2), // Faded color
                )
                Text(
                    text = "INDIAN",
                    fontSize = 50.sp, // Big font size
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE2E2E2), // Faded color
                )
                Text(
                    text = "APP",
                    fontSize = 50.sp, // Big font size
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE2E2E2), // Faded color
                )

                // Second text: "MADE WITH ❤️ IN INDIA" - Normal size, faded color
                Text(
                    text = "MADE WITH ❤️ IN INDIA",
                    fontSize = 15.sp, // Normal font size
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E2E2), // Faded color (light grayish-blue)
                    modifier = Modifier
                        .padding(top = 5.dp)
                )
            }
        }



        Text(
            text = "APP VERSION: $versionCode",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFE2E2E2),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 18.dp)
        )
    }
}

@Composable
fun GoToLinkButton(url: String, buttonText: String) {
    val uriHandler = LocalUriHandler.current // Get the UriHandler for opening links

    Button(onClick = {
        uriHandler.openUri(url) // Open the given URL
    }) {
        Text(text = buttonText) // Display the provided button text
    }
}

