package com.infbyte.amuzeo.presentation.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.infbyte.amuze.ads.GoogleMobileAdsConsentManager
import com.infbyte.amuze.contracts.AppSettingsContract
import com.infbyte.amuze.ui.dialogs.AppSettingsRedirectDialog
import com.infbyte.amuze.ui.screens.AboutScreen
import com.infbyte.amuze.ui.screens.LoadingScreen
import com.infbyte.amuze.ui.screens.NoMediaAvailableScreen
import com.infbyte.amuze.ui.screens.NoMediaPermissionScreen
import com.infbyte.amuzeo.BuildConfig
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.ui.screens.MainScreen
import com.infbyte.amuzeo.presentation.ui.screens.Screens
import com.infbyte.amuzeo.presentation.ui.screens.VideoScreen
import com.infbyte.amuzeo.presentation.ui.screens.VideosScreen
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import com.infbyte.amuzeo.utils.AmuzeoPermissions.isReadPermissionGranted
import com.infbyte.amuzeo.utils.AmuzeoPermissions.showReqPermRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    private val videosViewModel: VideosViewModel by viewModel()

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private val isMobileAdsInitialized = AtomicBoolean(false)

    private val permLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            videosViewModel.setReadPermGranted(isGranted)
            if (isGranted) {
                videosViewModel.init(this)
            }
        }

    private val appSettingsLauncher =
        registerForActivityResult(AppSettingsContract()) {
            videosViewModel.setReadPermGranted(it)
            if (it) {
                videosViewModel.init(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmuzeoTheme {
                LaunchedEffect("") {
                    googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this@MainActivity)

                    googleMobileAdsConsentManager.checkConsent(this@MainActivity) {
                        if (googleMobileAdsConsentManager.canRequestAds) {
                            initMobileAds()
                        }
                    }

                    if (googleMobileAdsConsentManager.canRequestAds) {
                        initMobileAds()
                    }

                    if (!videosViewModel.state.isLoaded) {
                        videosViewModel.setReadPermGranted(isReadPermissionGranted(this@MainActivity))
                        if (!videosViewModel.state.isReadPermGranted) {
                            launchPermRequest()
                        } else {
                            videosViewModel.init(this@MainActivity)
                        }
                    }
                }

                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    var initialScreen by rememberSaveable { mutableStateOf(Screens.MAIN) }

                    if (
                        (videosViewModel.state.isReadPermGranted && !videosViewModel.state.isLoaded) ||
                        videosViewModel.state.isRefreshing
                    ) {
                        LoadingScreen(stringResource(R.string.amuzeo_preparing))
                        return@Surface
                    }

                    if (videosViewModel.sideEffect.showAppSettingsDialog) {
                        AppSettingsRedirectDialog(
                            stringResource(R.string.amuzeo_perm_rationale),
                            onAccept = {
                                videosViewModel.hideAppSettingsRedirectDialog()
                                appSettingsLauncher.launch(packageName)
                            },
                            onDismiss = { videosViewModel.hideAppSettingsRedirectDialog() },
                        )
                    }

                    initialScreen =
                        when {
                            !videosViewModel.state.isReadPermGranted -> {
                                Screens.NO_PERMISSION
                            }
                            !videosViewModel.state.hasVideos -> {
                                Screens.NO_MEDIA
                            }
                            else -> Screens.MAIN
                        }

                    NavHost(navController, initialScreen) {
                        composable(Screens.MAIN) {
                            MainScreen(
                                videosViewModel,
                                onNavigateTo = { route -> navController.navigate(route) },
                                onNavigateBack = { onExit() },
                            )
                        }

                        composable(Screens.VIDEOS) {
                            VideosScreen(
                                videosViewModel,
                                onNavigateTo = { navController.navigate(it) },
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        composable(Screens.VIDEO_PLAYBACK) {
                            VideoScreen(
                                videosViewModel,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        composable(Screens.NO_PERMISSION) {
                            NoMediaPermissionScreen(
                                appIcon = R.drawable.amuzeo_intro,
                                action = R.string.amuzeo_watch,
                                onStartAction = {
                                    if (!showReqPermRationale()) {
                                        videosViewModel.showAppSettingsRedirectDialog()
                                        return@NoMediaPermissionScreen
                                    }
                                    launchPermRequest()
                                },
                                onExit = { onExit() },
                                aboutApp = { navController.navigate(Screens.ABOUT) },
                            )
                        }

                        composable(Screens.NO_MEDIA) {
                            NoMediaAvailableScreen(
                                R.string.amuzeo_no_videos,
                                onRefresh = {
                                    if (!videosViewModel.state.isReadPermGranted) {
                                        launchPermRequest()
                                    } else {
                                        videosViewModel.setIsRefreshing(true)
                                        videosViewModel.init(this@MainActivity)
                                    }
                                },
                                onExit = { onExit() },
                                aboutApp = { navController.navigate(Screens.ABOUT) },
                            )
                        }

                        composable(Screens.ABOUT) {
                            AboutScreen(
                                stringResource(R.string.app_name),
                                BuildConfig.VERSION_NAME,
                                R.drawable.ic_amuzeo_splash,
                                R.string.amuzeo_privacy_policy_link,
                                adsConsentManager = googleMobileAdsConsentManager,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videosViewModel.pauseVideo()
    }

    override fun onResume() {
        super.onResume()
        videosViewModel.playVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            videosViewModel.onExitApp()
        }
    }

    private fun initMobileAds() {
        if (isMobileAdsInitialized.getAndSet(true)) return
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity)
        }
    }

    private fun launchPermRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            return
        }
        permLauncher.launch(
            Manifest.permission.READ_MEDIA_VIDEO,
        )
    }

    private fun onExit() {
        if (!videosViewModel.confirmExit) {
            Toast.makeText(
                this,
                getString(R.string.amuzeo_confirm_exit),
                Toast.LENGTH_SHORT,
            )
                .show()
            videosViewModel.confirmExit()
        } else {
            finish()
        }
    }
}
