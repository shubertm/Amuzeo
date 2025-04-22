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
import coil.ImageLoader
import com.infbyte.amuzeo.models.AmuzeoSideEffect
import com.infbyte.amuzeo.models.AmuzeoState
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.playback.AmuzeoPlayer
import com.infbyte.amuzeo.repo.TagsRepo
import com.infbyte.amuzeo.repo.VideosRepo
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.user.tags.Tag
import dev.arkbuilders.arklib.user.tags.Tags
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

    val videoImageLoader: ImageLoader = videosRepo.videoImageLoader
    val taggedVideoImageLoader: ImageLoader = videosRepo.taggedVideoImageLoader

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
                onComplete = { videos, folders, ids ->
                    launch {
                        launch {
                            tagsRepo.init(context.filesDir.toPath(), ids) { tags ->
                                val taggedVideos =
                                    videos.map { video ->
                                        video.addTags(tags = tagsRepo.readTags(video.resourceId))
                                        video
                                    }

                                state =
                                    state.copy(
                                        videos = taggedVideos,
                                        allTags = tags,
                                        taggedVideos = taggedVideos,
                                        taggedVideosSearchResult = taggedVideos,
                                    )
                            }
                        }

                        if (state.isRefreshing && videos.isEmpty()) {
                            delay(2000)
                        }
                        state =
                            state.copy(
                                videos = videos,
                                currentVideos = videos,
                                folders = folders,
                                videosSearchResult = videos,
                                taggedVideos = videos,
                                taggedVideosSearchResult = videos,
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

    fun onVideoClick(video: Video) {
        if (state.currentVideo != video) {
            state = state.copy(currentVideo = video)
            amuzeoPlayer.createPlaylist(state.currentVideos.map { it.item })
        }
        val index = state.currentVideos.indexOf(video)
        amuzeoPlayer.selectVideo(index)
    }

    fun onFolderClick(folder: Folder) {
        state =
            with(state) {
                val videosInFolder =
                    videos.filter { video ->
                        video.folder == folder.name
                    }
                copy(currentVideos = videosInFolder)
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
        id: ResourceId,
        tags: Tags,
    ) {
        viewModelScope.launch {
            tagsRepo.writeTags(id, tags)
            state =
                with(state) {
                    val mutableTags = allTags.toMutableSet()
                    mutableTags.addAll(tags)
                    copy(allTags = mutableTags)
                }
        }
    }

    fun addToFilterTags(tag: Tag) {
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
                    return@with copy(taggedVideos = videos)
                }
                val videos =
                    videos.filter { video: Video ->
                        video.tags.any { tag -> filterTags.contains(tag) }
                    }
                copy(taggedVideos = videos)
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
                with(state) {
                    copy(
                        videosSearchResult =
                            currentVideos.filter { video ->
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
