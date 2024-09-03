package com.infbyte.amuzeo.presentation.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.infbyte.amuze.ui.dialogs.WalletAddressDialog
import com.infbyte.amuzeo.BuildConfig
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.ui.screens.MainScreen
import com.infbyte.amuzeo.presentation.ui.screens.Screens
import com.infbyte.amuzeo.presentation.ui.screens.VideoScreen
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import com.infbyte.amuzeo.utils.AmuzeoContracts
import com.infbyte.amuzeo.utils.AmuzeoPermissions.isReadPermissionGranted
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val videosViewModel: VideosViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.R)
    private val permLauncherAPI30 =
        registerForActivityResult(
            AmuzeoContracts.RequestPermissionApi30(),
        ) { isGranted ->
            if (isGranted) {
                videosViewModel.init()
            }
        }

    private val permLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                videosViewModel.init()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!videosViewModel.state.isLoaded) {
            if (!isReadPermissionGranted(this)) {
                launchPermRequest()
            } else {
                videosViewModel.init()
                installSplashScreen().setKeepOnScreenCondition {
                    !videosViewModel.state.isLoaded
                }
            }
        }

        setContent {
            AmuzeoTheme {
                val navController = rememberNavController()
                NavHost(navController, Screens.MAIN) {
                    composable(Screens.MAIN) {
                        MainScreen(
                            videosViewModel,
                            onNavigateTo = { route -> navController.navigate(route) },
                            onNavigateBack = { onExit() },
                        )
                    }
                    composable(Screens.VIDEOS) {}

                    composable(Screens.VIDEO_PLAYBACK) {
                        VideoScreen(
                            videosViewModel,
                            onNavigateBack = { navController.popBackStack() },
                        )
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permLauncherAPI30.launch(
                "package:${BuildConfig.APPLICATION_ID}",
            )
        } else {
            permLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }
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
