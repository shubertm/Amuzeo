package com.infbyte.amuzeo.presentation.viewmodels

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.infbyte.amuzeo.models.AmuzeoSideEffect
import com.infbyte.amuzeo.models.AmuzeoState
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.playback.AmuzeoPlayer
import com.infbyte.amuzeo.repo.ContentId
import com.infbyte.amuzeo.repo.TagsRepo
import com.infbyte.amuzeo.repo.VideosRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideosViewModel(
    private val amuzeoPlayer: AmuzeoPlayer,
    private val videosRepo: VideosRepo,
    private val tagsRepo: TagsRepo,
) : ViewModel() {
    val videoPlayer: Player? = amuzeoPlayer.getPlayer()

    var state by mutableStateOf(AmuzeoState.INITIAL_STATE)
        private set

    var sideEffect by mutableStateOf(AmuzeoSideEffect())
        private set

    var confirmExit: Boolean = false
        private set

    private var uiJob: Job? = null

    fun init(context: Context) {
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
                        tagsRepo.init(context.filesDir.toPath())
                        val taggedVideos =
                            videos.map { video ->
                                video.addTags(tags = tagsRepo.getTags(video.fileId))
                                video
                            }

                        if (state.isRefreshing && videos.isEmpty()) {
                            delay(2000)
                        }
                        state =
                            state.copy(
                                videos = taggedVideos,
                                currentVideos = taggedVideos,
                                folders = folders,
                                videosSearchResult = taggedVideos,
                                allTags = tagsRepo.getTags(),
                                foldersSearchResult = folders,
                                isLoaded = true,
                                hasVideos = videos.isNotEmpty(),
                                isRefreshing = false,
                            )

                        sideEffect = sideEffect.copy(showSplash = false)
                        launch(Dispatchers.Main) {
                            amuzeoPlayer.createPlaylist(state.videos.map { it.item })
                        }
                    }
                },
            )
        }
    }

    fun onVideoClick(video: Video) {
        state = state.copy(currentVideo = video, currentVideos = state.videos)
        amuzeoPlayer.createPlaylist(state.currentVideos.map { it.item })
        val index = state.currentVideos.indexOf(video)
        amuzeoPlayer.selectVideo(index)
    }

    fun onFolderClick(folder: Folder) {
        viewModelScope.launch {
            state =
                with(state) {
                    val videosInFolder =
                        videos.filter { video ->
                            video.folder == folder.name
                        }
                    copy(videos = videosInFolder)
                }
        }
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

    fun onTagVideo(
        id: ContentId,
        tags: Set<String>,
    ) {
        viewModelScope.launch {
            tagsRepo.setTags(id, tags)
            state = state.copy(allTags = tagsRepo.getTags())
        }
    }

    fun addToFilterTags(tag: String) {
        state =
            with(state) {
                val mutableTags = filterTags.toMutableSet()
                if (mutableTags.contains(tag)) {
                    mutableTags.remove(tag)
                    return@with copy(filterTags = mutableTags)
                }
                mutableTags.add(tag)
                copy(filterTags = mutableTags)
            }
    }

    fun filterVideosWithTags() {
        state =
            with(state) {
                if (filterTags.isEmpty()) {
                    return@with copy(videos = videosRepo.videos)
                }
                val videos =
                    videos.filter { video: Video ->
                        video.tags.any { tag -> filterTags.contains(tag) }
                    }
                copy(videos = videos)
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
    }

    fun showUi() {
        state = state.copy(isUiVisible = true)
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
                state.copy(
                    videosSearchResult =
                        state.currentVideos.filter { video ->
                            video.title.contains(query, ignoreCase = true)
                        },
                )
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

    fun onNavigateToVideos() {
        viewModelScope.launch {
            state = state.copy(videos = videosRepo.videos)
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            state = state.copy(searchQuery = query)
        }
    }

    fun showAppSettingsRedirectDialog() {
        sideEffect = sideEffect.copy(showAppSettingsDialog = true)
    }

    fun hideAppSettingsRedirectDialog() {
        sideEffect = sideEffect.copy(showAppSettingsDialog = false)
    }
}
