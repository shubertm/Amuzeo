package com.infbyte.amuzeo.repo

import java.nio.file.Path

interface TagsRepo {
    suspend fun init(path: Path)

    suspend fun setTags(
        contentId: ContentId,
        tags: Set<String>,
    )

    suspend fun getTags(contentId: ContentId): Set<String>

    suspend fun getTags(contentIds: List<ContentId>): Set<String>

    suspend fun getTags(): Set<String>
}
