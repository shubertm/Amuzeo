package com.infbyte.amuzeo.models

import dev.arkbuilders.arklib.user.tags.Tags

data class AmuzeoState(
    val currentVideo: Video = Video.EMPTY,
    val currentVideos: List<Video> = emptyList(),
    val videos: List<Video> = listOf(),
    val videosSearchResult: List<Video> = listOf(),
    val folders: List<Folder> = listOf(),
    val foldersSearchResult: List<Folder> = listOf(),
    val taggedVideos: List<Video> = listOf(),
    val taggedVideosSearchResult: List<Video> = listOf(),
    val allTags: Tags = emptySet(),
    val filterTags: Tags = emptySet(),
    val progress: Float = 0f,
    val searchQuery: String = "",
    val isLoaded: Boolean = false,
    val isPlaying: Boolean = false,
    val isUiVisible: Boolean = true,
    val isReadPermGranted: Boolean = false,
    val isSearching: Boolean = false,
    val hasVideos: Boolean = false,
    val isRefreshing: Boolean = false,
) {
    companion object {
        val INITIAL_STATE = AmuzeoState()
    }
}
