package com.infbyte.amuzeo.presentation.viewmodels

import android.app.Activity
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
                        state = state.copy(videos = videos, folders = folders, isLoaded = true)
                    }
                    launch(Dispatchers.Main) {
                        amuzeoPlayer.createPlaylist(videos.map { it.item })
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

    fun hideUi() {
        state = state.copy(isUiVisible = false)
    }

    fun toggleUi(visible: Boolean) {
        state = state.copy(isUiVisible = visible)
    }

    fun cancelDelayHidingUi() {
        if (uiJob != null && uiJob?.isActive!!) {
            uiJob?.cancel()
            uiJob = null
        }
    }

    fun delayHidingUi(view: View) {
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

    private fun showUi() {
        state = state.copy(isUiVisible = true)
    }

    private fun startProgressMonitor() {
        viewModelScope.launch {
            while (state.isPlaying) {
                delay(1000)
                state = state.copy(progress = amuzeoPlayer.getProgress())
            }
        }
    }
}
