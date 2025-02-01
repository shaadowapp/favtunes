package com.shaadow.tunes

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.viewmodels.UserViewModel

@Composable
fun AppScreen() {
    val userViewModel: UserViewModel = viewModel()

    userViewModel.getOrCreateUser { user ->
        Log.d("AutoLogin", "User Logged In: ${user.username} (${user.publicKey})")
        // TODO: Navigate to the main app screen or show UI
    }
}
