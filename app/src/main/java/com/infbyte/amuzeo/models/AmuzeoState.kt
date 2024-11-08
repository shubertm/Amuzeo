package com.infbyte.amuzeo.models

data class AmuzeoState(
    val currentVideo: Video = Video.EMPTY,
    val videos: List<Video> = listOf(),
    val folders: List<Folder> = listOf(),
    val progress: Float = 0f,
    val isLoaded: Boolean = false,
    val isPlaying: Boolean = false,
    val isUiVisible: Boolean = true,
    val isReadPermGranted: Boolean = false,
) {
    companion object {
        val INITIAL_STATE = AmuzeoState()
    }
}
