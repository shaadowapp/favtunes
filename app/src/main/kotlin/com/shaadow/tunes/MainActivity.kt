package com.shaadow.tunes

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.compose.rememberNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.bodies.BrowseBody
import com.shaadow.innertube.requests.playlistPage
import com.shaadow.innertube.requests.song
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.service.PlayerService
import com.shaadow.tunes.ui.components.BottomNavigation
import com.shaadow.tunes.ui.screens.Navigation
import com.shaadow.tunes.ui.screens.player.PlayerScaffold
import com.shaadow.tunes.ui.styling.AppTheme
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) this@MainActivity.binder = service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }
    
    private var isServiceBound = false

    private var binder by mutableStateOf<PlayerService.Binder?>(null)
    private var data by mutableStateOf<Uri?>(null)

    override fun onStart() {
        super.onStart()
        bindService(intent<PlayerService>(), serviceConnection, BIND_AUTO_CREATE)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Obtain the FirebaseAnalytics instance.
        analytics = Firebase.analytics

        // Initialize the ActivityResultLauncher in onCreate
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                // If the update is canceled or fails, execute particular code here
                Log.d("Update flow failed!", "Result Code: ${result.resultCode}")
            }
        }

        checkForInAppUpdate()

        val launchedFromNotification = intent?.extras?.getBoolean("expandPlayerBottomSheet") == true
        data = intent?.data ?: intent?.getStringExtra(Intent.EXTRA_TEXT)?.toUri()

        // Preload content immediately on IO thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Preload database connections
                Database.trending().first()
            } catch (e: Exception) {
                // Silently handle errors
            }
        }

        setContent {
            AppScreen()
        }

        setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            val playerState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                confirmValueChange = { value ->
                    if (value == SheetValue.Hidden) {
                        binder?.stopRadio()
                        binder?.player?.clearMediaItems()
                    }

                    return@rememberStandardBottomSheetState true
                },
                skipHiddenState = false
            )

            AppTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(value = LocalPlayerServiceBinder provides binder) {
                        val menuState = LocalMenuState.current

                        Scaffold(
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = playerState.targetValue != SheetValue.Expanded,
                                    enter = slideInVertically(initialOffsetY = { it / 2 }),
                                    exit = slideOutVertically(targetOffsetY = { it })
                                ) {
                                    BottomNavigation(navController = navController)
                                }
                            }
                        ) { paddingValues ->
                            PlayerScaffold(
                                navController = navController,
                                sheetState = playerState,
                                scaffoldPadding = paddingValues
                            ) {
                                Navigation(
                                    navController = navController,
                                    sheetState = playerState
                                )
                            }
                        }

                        if (menuState.isDisplayed) {
                            ModalBottomSheet(
                                onDismissRequest = menuState::hide,
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                dragHandle = {
                                    Surface(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = MaterialTheme.shapes.extraLarge
                                    ) {
                                        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
                                    }
                                }
                            ) {
                                menuState.content()
                            }
                        }
                    }
                }
            }

            DisposableEffect(binder?.player) {
                val player = binder?.player ?: return@DisposableEffect onDispose { }

                if (player.currentMediaItem == null) scope.launch { playerState.hide() }
                else {
                    if (launchedFromNotification) {
                        intent.replaceExtras(Bundle())
                        scope.launch { playerState.expand() }
                    } else scope.launch { playerState.partialExpand() }
                }

                val listener = object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null)
                            if (mediaItem.mediaMetadata.extras?.getBoolean("isFromPersistentQueue") != true) scope.launch { playerState.expand() }
                            else scope.launch { playerState.partialExpand() }
                    }
                }

                player.addListener(listener)
                onDispose { player.removeListener(listener) }
            }

            LaunchedEffect(data) {
                val uri = data ?: return@LaunchedEffect

                lifecycleScope.launch(Dispatchers.Main) {
                    when (val path = uri.pathSegments.firstOrNull()) {
                        "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                            val browseId = "VL$playlistId"

                            if (playlistId.startsWith("OLAK5uy_")) {
                                Innertube.playlistPage(BrowseBody(browseId = browseId))?.getOrNull()
                                    ?.let {
                                        it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
                                            navController.navigate(
                                                route = "album/$browseId"
                                            )
                                        }
                                    }
                            } else navController.navigate(route = "playlist/$browseId")
                        }

                        "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                            navController.navigate(
                                route = "artist/$channelId"
                            )
                        }

                        else -> when {
                            path == "watch" -> uri.getQueryParameter("v")
                            uri.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            Innertube.song(videoId)?.getOrNull()?.let { song ->
                                val binder = snapshotFlow { binder }.filterNotNull().first()
                                withContext(Dispatchers.Main) {
                                    binder.player.forcePlay(song.asMediaItem)
                                }
                            }
                        }
                    }
                }

                data = null
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        data = intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()

        appUpdateManager.unregisterListener(listener)
    }

    private fun checkForInAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Registering Listener
        appUpdateManager.registerListener(listener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Request the update here
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            findViewById(android.R.id.content), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (isServiceBound) {
                unbindService(serviceConnection)
                isServiceBound = false
            }
        } catch (e: IllegalArgumentException) {
            // Service was not bound or already unbound
        }
    }
}

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }
val LocalPlayerPadding = compositionLocalOf<Dp> { 0.dp }