package com.infbyte.amuzeo.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import com.infbyte.amuzeo.utils.getSubListIfNotEmpty
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    videosViewModel: VideosViewModel,
    onNavigateTo: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(0) { 3 }
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    fun search() {
        when (pagerState.currentPage) {
            0 -> videosViewModel.onSearchVideos(searchQuery)
            1 -> videosViewModel.onSearchFolders(searchQuery)
        }
    }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { query ->
                            searchQuery = query
                            search()
                        },
                        onSearch = { query -> },
                        expanded = videosViewModel.state.isSearching,
                        onExpandedChange = { isExpanded ->
                            searchQuery = ""
                            videosViewModel.setIsSearching(isExpanded)
                            if (isExpanded) {
                                search()
                            }
                        },
                        placeholder = { Text(stringResource(R.string.amuzeo_search)) },
                        trailingIcon = {
                            if (!videosViewModel.state.isSearching) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            videosViewModel.setIsSearching(true)
                                        },
                                    ) { Icon(Icons.Outlined.Search, "") }
                                    IconButton(onClick = {}) { Icon(Icons.Outlined.Info, "") }
                                }
                            }
                        },
                    )
                },
                expanded = videosViewModel.state.isSearching,
                onExpandedChange = { isExpanded ->
                    searchQuery = ""
                    videosViewModel.setIsSearching(isExpanded)
                    if (isExpanded) {
                        search()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp),
                content = {
                    when (pagerState.currentPage) {
                        0 -> {
                            VideosScreen(
                                videosViewModel.state.videosSearchResult,
                                videosViewModel.videoImageLoader,
                            ) { index ->
                                onNavigateTo(Screens.VIDEO_PLAYBACK)
                                videosViewModel.onVideoClick(index)
                            }
                        }
                        1 -> {
                            FoldersScreen(videosViewModel.state.foldersSearchResult) {}
                        }
                        2 -> {
                            TaggedVideosScreen(
                                videosViewModel.state.videos.getSubListIfNotEmpty(1, 6),
                                videosViewModel.taggedVideoImageLoader,
                            ) {}
                        }
                    }
                },
            )
        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewMainScreen() {
    AmuzeoTheme {
        Scaffold(
            Modifier.fillMaxSize(),
            topBar = {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = "",
                            onQueryChange = {},
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.amuzeo_search)) },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = {}) { Icon(Icons.Outlined.Search, "") }
                                    IconButton(onClick = {}) { Icon(Icons.Outlined.Info, "") }
                                }
                            },
                        )
                    },
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    content = {
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {
                        },
                        icon = {
                            Icon(ImageVector.vectorResource(R.drawable.ic_video_library), contentDescription = "")
                        },
                        label = {
                            Text(stringResource(R.string.amuzeo_videos))
                        },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
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
                        selected = false,
                        onClick = {
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
            padding.calculateTopPadding()
        }
    }
}
