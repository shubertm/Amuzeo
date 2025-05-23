package com.infbyte.amuzeo.models

data class AmuzeoState(
    val currentVideo: Video = Video.EMPTY,
    val currentVideos: List<Video> = emptyList(),
    val videos: List<Video> = listOf(),
    val videosSearchResult: List<Video> = listOf(),
    val folders: List<Folder> = listOf(),
    val foldersSearchResult: List<Folder> = listOf(),
    val allTags: Set<String> = emptySet(),
    val filterTags: Set<String> = emptySet(),
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
