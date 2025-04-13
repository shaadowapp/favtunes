package com.shaadow.tunes.ui.screens.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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

@Composable
fun About() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val playerPadding = LocalPlayerPadding.current

    // Retrieve the app version based on the current API level
    val versionName = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName
        }
    } catch (e: Exception) {
        "Unknown Version"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding)
    ) {
        // App Icon
        Icon(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(108.dp)
                .padding(top = 25.dp)
                .aspectRatio(1f),
            tint = Color.Red
        )

        // App Name
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        // Description
        Text(
            text = "FavTunes is a powerful music player that lets you stream your favorite songs, and customize your audio experience with an equalizer. Enjoy seamless playback and intuitive controls, all wrapped in a sleek and modern design.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp),
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Website Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                uriHandler.openUri("https://tunes.shaadow.in?utm_source=FavTunes_Android&utm_from=setting-about&utm_pkg=com-shaadow-tunes")
            }) {
                Text(text = "Our Official Website")
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // Footer Text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 15.dp, start = 18.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "PROUDLY",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "INDIAN",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "APP",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "MADE WITH ❤️ IN INDIA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }

        // App Version
        Text(
            text = "APP VERSION: $versionName",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 18.dp)
        )
    }
}
