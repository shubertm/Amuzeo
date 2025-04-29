package com.infbyte.amuzeo.presentation.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.infbyte.amuze.ui.screens.AboutScreen
import com.infbyte.amuze.ui.screens.LoadingScreen
import com.infbyte.amuze.ui.screens.NoMediaAvailableScreen
import com.infbyte.amuze.ui.screens.NoMediaPermissionScreen
import com.infbyte.amuze.utils.GoogleMobileAdsConsentManager
import com.infbyte.amuzeo.BuildConfig
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.ui.screens.MainScreen
import com.infbyte.amuzeo.presentation.ui.screens.Screens
import com.infbyte.amuzeo.presentation.ui.screens.VideoScreen
import com.infbyte.amuzeo.presentation.ui.screens.VideosScreen
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import com.infbyte.amuzeo.utils.AmuzeoPermissions.isReadPermissionGranted
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val videosViewModel: VideosViewModel by viewModel()

    private val permLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            videosViewModel.setReadPermGranted(isGranted)
            if (isGranted) {
                videosViewModel.init(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!videosViewModel.state.isLoaded) {
            videosViewModel.setReadPermGranted(isReadPermissionGranted(this))
            if (!videosViewModel.state.isReadPermGranted) {
                launchPermRequest()
            } else {
                videosViewModel.init(this)
            }
        }

        installSplashScreen().setKeepOnScreenCondition {
            videosViewModel.sideEffect.showSplash
        }

        setContent {
            AmuzeoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    if (!videosViewModel.state.isReadPermGranted) {
                        videosViewModel.hideSystemBars(LocalView.current)
                        NoMediaPermissionScreen(
                            appIcon = R.drawable.amuzeo_intro,
                            action = R.string.amuzeo_watch,
                            onStartAction = { launchPermRequest() },
                            onExit = { onExit() },
                            aboutApp = { navigateBack ->
                                AboutScreen(
                                    appName = stringResource(R.string.app_name),
                                    appVersion = BuildConfig.VERSION_NAME,
                                    appIconRes = R.drawable.amuzeo_foreground,
                                    privacyPolicyLinkRes = R.string.amuzeo_privacy_policy,
                                    adsConsentManager = GoogleMobileAdsConsentManager(this),
                                    onNavigateBack = { navigateBack() },
                                )
                            },
                        )
                        return@Surface
                    }

                    if (
                        (videosViewModel.state.isReadPermGranted && !videosViewModel.state.isLoaded) ||
                        videosViewModel.state.isRefreshing
                    ) {
                        LoadingScreen()
                        return@Surface
                    }

                    if (!videosViewModel.state.hasVideos) {
                        NoMediaAvailableScreen(
                            R.string.amuzeo_no_videos,
                            onRefresh = {
                                if (!videosViewModel.state.isReadPermGranted) {
                                    launchPermRequest()
                                } else {
                                    videosViewModel.setIsRefreshing(true)
                                    videosViewModel.init(this)
                                }
                            },
                            onExit = { onExit() },
                            aboutApp = { navigateBack ->
                                AboutScreen(
                                    stringResource(R.string.app_name),
                                    BuildConfig.VERSION_NAME,
                                    R.drawable.amuzeo_foreground,
                                    R.string.amuzeo_privacy_policy_link,
                                    adsConsentManager = GoogleMobileAdsConsentManager(this),
                                    onNavigateBack = { navigateBack() },
                                )
                            },
                        )
                        return@Surface
                    }

                    NavHost(navController, Screens.MAIN) {
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

                        composable(Screens.ABOUT) {
                            AboutScreen(
                                stringResource(R.string.app_name),
                                BuildConfig.VERSION_NAME,
                                R.drawable.ic_amuzeo_splash,
                                R.string.amuzeo_privacy_policy_link,
                                adsConsentManager = GoogleMobileAdsConsentManager(this@MainActivity),
                            ) { navController.popBackStack() }
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
