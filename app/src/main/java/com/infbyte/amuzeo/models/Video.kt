package com.infbyte.amuzeo.models

import androidx.compose.ui.graphics.ImageBitmap
import androidx.media3.common.MediaItem
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.user.tags.Tag
import dev.arkbuilders.arklib.user.tags.Tags

data class Video(
    val item: MediaItem,
    val folder: String,
    val resourceId: ResourceId,
    val thumbnail: ImageBitmap,
) {
    val title = item.mediaMetadata.title.toString()

    private val _tags = mutableSetOf<Tag>()
    val tags: Tags = _tags

    fun addTag(tag: Tag) {
        _tags.add(tag)
    }

    fun addTags(tags: Tags) {
        _tags.addAll(tags)
    }

    companion object {
        val EMPTY =
            Video(
                MediaItem.EMPTY.buildUpon().setUri("").build(),
                "",
                resourceId = ResourceId.fromString("0-0"),
                ImageBitmap(10, 10),
            )
    }
}
