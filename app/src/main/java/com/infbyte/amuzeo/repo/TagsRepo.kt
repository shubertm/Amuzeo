package com.infbyte.amuzeo.repo

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.user.tags.Tags
import java.nio.file.Path

interface TagsRepo {
    suspend fun init(
        path: Path,
        resourceIds: List<ResourceId>,
        updateTags: suspend (Tags) -> Unit,
    )

    suspend fun writeTags(
        id: ResourceId,
        tags: Tags,
    )

    suspend fun readTags(id: ResourceId): Tags

    suspend fun readTags(ids: List<ResourceId>): Tags
}
