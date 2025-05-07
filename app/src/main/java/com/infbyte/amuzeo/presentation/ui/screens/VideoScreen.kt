package com.infbyte.amuzeo.presentation.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.models.AmuzeoState
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.theme.onPrimaryLight
import com.infbyte.amuzeo.presentation.ui.AmuzeSeekBar
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel

@Composable
fun VideoScreen(
    videosViewModel: VideosViewModel,
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current

    /*DisposableEffect(key1 = "") {
        videosViewModel.run {
            if (state.isUiVisible) {
                videosViewModel.hideSystemBars(view)
                videosViewModel.hideUi()
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            view.setOnSystemUiVisibilityChangeListener { visibility ->
                videosViewModel.showUi(
                    visibility and
                        (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0,
                )

                videosViewModel.run {
                    if (state.isUiVisible) {
                        cancelDelayHidingUi()
                        delayHidingUiAndSystemBars(view)
                    }
                }
            }
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
                val isVisible = insets.isVisible(WindowInsetsCompat.Type.statusBars())

                videosViewModel.showUi(isVisible)

                if (isVisible) {
                    videosViewModel.cancelDelayHidingUi()
                    videosViewModel.delayHidingUiAndSystemBars(view)
                }

                ViewCompat.onApplyWindowInsets(view, insets)
            }
        }*/

        onDispose {
            view.keepScreenOn = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                view.setOnSystemUiVisibilityChangeListener(null)
            }
        }
    }*/

    LaunchedEffect("") {
        videosViewModel.delayHidingUiAndSystemBars(view)
    }

    SideEffect {
        view.keepScreenOn = videosViewModel.state.isPlaying
    }

    fun onBackPressed() {
        videosViewModel.onExitVideoScreen()
        videosViewModel.cancelDelayHidingUi()
        onNavigateBack()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
            .pointerInput("") {
                awaitEachGesture {
                    if (awaitFirstDown().pressed) {
                        videosViewModel.run {
                            cancelDelayHidingUi()
                            if (state.isUiVisible) {
                                hideSystemBars(view)
                                hideUi()
                                return@run
                            }
                            showUi()
                            showSystemBars(view)
                            delayHidingUiAndSystemBars(view)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        VideoFrame(videoPlayer = videosViewModel.videoPlayer)
        AnimatedVisibility(visible = videosViewModel.state.isUiVisible) {
            Box(Modifier.fillMaxSize()) {
                val controlBoxScope = this
                val config = LocalConfiguration.current

                IconButton(
                    onClick = {
                        onBackPressed()
                    },
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.scrim.copy(.2f), CircleShape),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "",
                        tint = onPrimaryLight,
                    )
                }

                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    VerticalVideoController(
                        state = videosViewModel.state,
                        onPlayPause = { videosViewModel.onPlayPauseClick() },
                        onNext = { videosViewModel.onNextVideoClick() },
                        onPrev = { videosViewModel.onPrevVideoClick() },
                    )
                    AmuzeSeekBar(
                        progress = videosViewModel.state.progress,
                        onSeekTo = { videosViewModel.onSeekTo(it) },
                    )
                } else {
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.scrim.copy(.2f))
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HorizontalVideoController(
                            state = videosViewModel.state,
                            onPlayPause = { videosViewModel.onPlayPauseClick() },
                            onNext = { videosViewModel.onNextVideoClick() },
                            onPrev = { videosViewModel.onPrevVideoClick() },
                        )
                        controlBoxScope.AmuzeSeekBar(
                            progress = videosViewModel.state.progress,
                            onSeekTo = { videosViewModel.onSeekTo(it) },
                        )
                    }
                }
            }
        }
    }

    BackHandler {
        onBackPressed()
    }
}

@Preview
@Composable
fun HorizontalVideoController(
    state: AmuzeoState = AmuzeoState.INITIAL_STATE,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrev: () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = { onPrev() }) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_skip_previous),
                contentDescription = "",
                Modifier.size(32.dp),
                tint = onPrimaryLight,
            )
        }
        Box(
            Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary.copy(0.2f), CircleShape)
                .clip(CircleShape)
                .size(58.dp)
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (!state.isPlaying) {
                    Icons.Outlined.PlayArrow
                } else {
                    ImageVector.vectorResource(R.drawable.ic_pause)
                },
                contentDescription = "",
                Modifier.size(52.dp),
                tint = onPrimaryLight,
            )
        }
        IconButton(onClick = { onNext() }) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_skip_next),
                contentDescription = "",
                Modifier.size(32.dp),
                tint = onPrimaryLight,
            )
        }
    }
}

@Preview
@Composable
fun BoxScope.VerticalVideoController(
    state: AmuzeoState = AmuzeoState.INITIAL_STATE,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrev: () -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxHeight()
            .align(Alignment.CenterEnd)
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.scrim.copy(0.2f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = { onPrev() }) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_skip_previous),
                contentDescription = "",
                Modifier.size(32.dp),
                tint = onPrimaryLight,
            )
        }
        Box(
            Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary.copy(.2f), CircleShape)
                .clip(CircleShape)
                .size(58.dp)
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (!state.isPlaying) {
                    Icons.Outlined.PlayArrow
                } else {
                    ImageVector.vectorResource(R.drawable.ic_pause)
                },
                contentDescription = "",
                Modifier.size(52.dp),
                tint = onPrimaryLight,
            )
        }
        IconButton(onClick = { onNext() }) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_skip_next),
                contentDescription = "",
                Modifier.size(32.dp),
                tint = onPrimaryLight,
            )
        }
    }
}

@Composable
fun VideoFrame(videoPlayer: Player?) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = videoPlayer
                useController = false
            }
        },
        Modifier.fillMaxSize(),
    )
}

@PreviewScreenSizes
@Composable
fun PreviewVideoScreen() {
    AmuzeoTheme {
        AnimatedVisibility(visible = true) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim),
            ) {
                IconButton(
                    onClick = {},
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.scrim.copy(.2f))
                        .clip(CircleShape),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "",
                        tint = onPrimaryLight,
                    )
                }
                val config = LocalConfiguration.current
                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    VerticalVideoController()
                    AmuzeSeekBar()
                } else {
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HorizontalVideoController()
                        this@Box.AmuzeSeekBar()
                    }
                }
            }
        }
    }
}
