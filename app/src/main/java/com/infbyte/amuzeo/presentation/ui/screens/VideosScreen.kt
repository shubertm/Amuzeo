package com.infbyte.amuzeo.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    videosViewModel: VideosViewModel,
    onNavigateTo: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    fun search() {
        videosViewModel.onSearchVideos(searchQuery)
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
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    if (videosViewModel.state.isSearching) {
                                        videosViewModel.setIsSearching(false)
                                        return@IconButton
                                    }
                                    onNavigateBack()
                                },
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "")
                            }
                        },
                        trailingIcon = {
                            if (!videosViewModel.state.isSearching) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            videosViewModel.setIsSearching(true)
                                        },
                                    ) { Icon(Icons.Outlined.Search, "") }
                                    IconButton(
                                        onClick = { onNavigateTo(Screens.ABOUT) },
                                    ) { Icon(Icons.Outlined.Info, "") }
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
                modifier =
                    Modifier.fillMaxWidth().padding(
                        start = if (!videosViewModel.state.isSearching) 8.dp else 0.dp,
                        end = if (!videosViewModel.state.isSearching) 8.dp else 0.dp,
                    ),
                content = {
                    Videos(videosViewModel.state.videosSearchResult) { video ->
                        videosViewModel.onVideoClick(video)
                        onNavigateTo(Screens.VIDEO_PLAYBACK)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier.padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        ) {
            Videos(videosViewModel.state.videos) { video ->
                videosViewModel.onVideoClick(video)
                onNavigateTo(Screens.VIDEO_PLAYBACK)
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }
}
