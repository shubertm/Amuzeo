package com.infbyte.amuzeo.models

import androidx.media3.common.MediaItem
import coil.request.ImageRequest

data class Video(
    val item: MediaItem,
    val folder: String,
    val thumbnailRequest: ImageRequest? = null,
) {
    val title = item.mediaMetadata.title.toString()

    companion object {
        val EMPTY = Video(MediaItem.EMPTY.buildUpon().setUri("").build(), "")
    }
}
