package com.infbyte.amuzeo.presentation.viewmodels

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import coil.ImageLoader
import com.infbyte.amuzeo.models.AmuzeoSideEffect
import com.infbyte.amuzeo.models.AmuzeoState
import com.infbyte.amuzeo.playback.AmuzeoPlayer
import com.infbyte.amuzeo.repo.VideosRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideosViewModel(
    private val amuzeoPlayer: AmuzeoPlayer,
    private val videosRepo: VideosRepo,
) : ViewModel() {
    val videoPlayer: Player? = amuzeoPlayer.getPlayer()

    val videoImageLoader: ImageLoader = videosRepo.videoImageLoader
    val taggedVideoImageLoader: ImageLoader = videosRepo.taggedVideoImageLoader

    var state by mutableStateOf(AmuzeoState.INITIAL_STATE)
        private set

    var sideEffect by mutableStateOf(AmuzeoSideEffect())
        private set

    var confirmExit: Boolean = false
        private set

    private var uiJob: Job? = null

    fun init() {
        amuzeoPlayer.isPlayingChanged = { isPlaying ->
            state = state.copy(isPlaying = isPlaying)
            startProgressMonitor()
        }
        viewModelScope.launch {
            videosRepo.loadVideos(
                isLoading = {
                    amuzeoPlayer.init()
                },
                onComplete = { videos, folders ->
                    launch {
                        if (state.isRefreshing && videos.isEmpty()) {
                            delay(2000)
                        }
                        state =
                            state.copy(
                                videos = videos,
                                folders = folders,
                                videosSearchResult = videos,
                                foldersSearchResult = folders,
                                isLoaded = true,
                                hasVideos = videos.isNotEmpty(),
                                isRefreshing = false,
                            )
                        sideEffect = sideEffect.copy(showSplash = false)
                        launch(Dispatchers.Main) {
                            amuzeoPlayer.createPlaylist(videos.map { it.item })
                        }
                    }
                },
            )
        }
    }

    fun onVideoClick(index: Int) {
        val video = state.videos[index]
        if (state.currentVideo != video) {
            state = state.copy(currentVideo = video)
        }
        amuzeoPlayer.selectVideo(index)
    }

    fun onPlayPauseClick() {
        if (state.isPlaying) {
            amuzeoPlayer.pauseVideo()
            return
        }
        amuzeoPlayer.playVideo()
    }

    fun onExitVideoScreen() {
        amuzeoPlayer.stopVideo()
    }

    fun onNextVideoClick() {
        amuzeoPlayer.nextVideo()
    }

    fun onPrevVideoClick() {
        amuzeoPlayer.prevVideo()
    }

    fun onSeekTo(position: Float) {
        state = state.copy(progress = position)
        amuzeoPlayer.seekTo(position)
    }

    fun pauseVideo() {
        if (state.isPlaying) {
            amuzeoPlayer.pauseVideo()
        }
    }

    fun playVideo() {
        if (!state.isPlaying) {
            amuzeoPlayer.playVideo()
        }
    }

    fun setReadPermGranted(granted: Boolean) {
        state = state.copy(isReadPermGranted = granted)
    }

    fun hideSystemBars(view: View) {
        if (state.isUiVisible) {
            viewModelScope.launch {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).run {
                    hide(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    fun showSystemBars(view: View) {
        viewModelScope.launch {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).run {
                show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun hideUi() {
        state = state.copy(isUiVisible = false)
        Log.d("VIEWMODEL", state.isUiVisible.toString())
    }

    fun showUi() {
        state = state.copy(isUiVisible = true)
        Log.d("VIEWMODEL", state.isUiVisible.toString())
    }

    fun cancelDelayHidingUi() {
        if (uiJob != null && uiJob?.isActive!!) {
            uiJob?.cancel()
            uiJob = null
        }
    }

    fun delayHidingUiAndSystemBars(view: View) {
        uiJob =
            viewModelScope.launch {
                showUi()
                delay(5000)
                hideSystemBars(view)
                hideUi()
                uiJob = null
            }
    }

    fun onExitApp() {
        amuzeoPlayer.release()
    }

    fun confirmExit() {
        viewModelScope.launch {
            confirmExit = true
            delay(2000)
            confirmExit = false
        }
    }

    private fun startProgressMonitor() {
        viewModelScope.launch {
            while (state.isPlaying) {
                delay(1000)
                state = state.copy(progress = amuzeoPlayer.getProgress())
            }
        }
    }

    fun setIsRefreshing(refresh: Boolean) {
        state = state.copy(isRefreshing = refresh)
    }

    fun setIsSearching(searching: Boolean) {
        state = state.copy(isSearching = searching)
    }

    fun onSearchVideos(query: String) {
        viewModelScope.launch {
            state =
                with(state) {
                    copy(
                        videosSearchResult =
                            videos.filter { video ->
                                video.title.contains(query, ignoreCase = true)
                            },
                    )
                }
        }
    }

    fun onSearchFolders(query: String) {
        viewModelScope.launch {
            state =
                with(state) {
                    copy(
                        foldersSearchResult =
                            folders.filter { folder ->
                                folder.name.contains(query, ignoreCase = true)
                            },
                    )
                }
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            state = state.copy(searchQuery = query)
        }
    }
}
