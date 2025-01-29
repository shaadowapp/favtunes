package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.R

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Add profile content here
        Text(
            text = stringResource(id = R.string.profile_heading),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(id = R.string.profile_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 12.dp, start = 18.dp, end = 18.dp),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "hy 2",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 12.dp, start = 18.dp, end = 18.dp),
            textAlign = TextAlign.Center,
        )

    }
}
