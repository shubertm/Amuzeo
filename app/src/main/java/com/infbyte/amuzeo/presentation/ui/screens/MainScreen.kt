package com.infbyte.amuzeo.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import com.infbyte.amuzeo.utils.getSubListIfNotEmpty
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalStdlibApi::class)
@Composable
fun MainScreen(
    videosViewModel: VideosViewModel,
    onNavigateTo: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(0) { 3 }
    val scope = rememberCoroutineScope()

    Scaffold(
        Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                0,
                                animationSpec = tween(500, 300, LinearEasing),
                            )
                        }
                    },
                    icon = {
                        Icon(ImageVector.vectorResource(R.drawable.ic_video_library), contentDescription = "")
                    },
                    label = {
                        Text(stringResource(R.string.amuzeo_videos))
                    },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                1,
                                animationSpec = tween(500, 300, LinearEasing),
                            )
                        }
                    },
                    icon = {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_folder),
                            contentDescription = "",
                        )
                    },
                    label = {
                        Text(stringResource(R.string.amuzeo_folders))
                    },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                2,
                                animationSpec = tween(500, 300, LinearEasing),
                            )
                        }
                    },
                    icon = {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_bookmark),
                            contentDescription = "",
                        )
                    },
                    label = {
                        Text(stringResource(R.string.amuzeo_tags))
                    },
                )
            }
        },
    ) { padding ->
        HorizontalPager(
            pagerState,
            Modifier.padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
            beyondViewportPageCount = 2,
        ) { page ->
            when (page) {
                0 -> {
                    VideosScreen(videosViewModel.state.videos, videosViewModel.videoImageLoader) { index ->
                        onNavigateTo(Screens.VIDEO_PLAYBACK)
                        videosViewModel.onVideoClick(index)
                    }
                }
                1 -> {
                    FoldersScreen(videosViewModel.state.folders) {}
                }
                2 -> {
                    TaggedVideosScreen(
                        videosViewModel.state.videos.getSubListIfNotEmpty(1, 6),
                        videosViewModel.taggedVideoImageLoader,
                    ) {}
                }
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    AmuzeoTheme {
        MainScreen(videosViewModel = koinViewModel(), {}) {}
    }
}
