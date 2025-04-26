package com.infbyte.amuzeo.models

import androidx.compose.ui.graphics.ImageBitmap
import androidx.media3.common.MediaItem

data class Video(
    val item: MediaItem,
    val folder: String,
    val fileId: String,
    val thumbnail: ImageBitmap,
) {
    val title = item.mediaMetadata.title.toString()

    private val _tags = mutableSetOf<String>()
    val tags: Set<String> = _tags

    fun addTag(tag: String) {
        _tags.add(tag)
    }

    fun addTags(tags: Set<String>) {
        _tags.addAll(tags)
    }

    companion object {
        val EMPTY =
            Video(
                MediaItem.EMPTY.buildUpon().setUri("").build(),
                "",
                fileId = "0",
                ImageBitmap(10, 10),
            )
    }
}
