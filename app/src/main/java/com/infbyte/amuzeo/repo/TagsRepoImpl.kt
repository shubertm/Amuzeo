package com.infbyte.amuzeo.repo

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.data.stats.StatsEvent
import dev.arkbuilders.arklib.user.tags.TagStorage
import dev.arkbuilders.arklib.user.tags.Tags
import dev.arkbuilders.arklib.user.tags.TagsStorageRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.nio.file.Path

class TagsRepoImpl : TagsRepo {
    private val statsFlow = MutableSharedFlow<StatsEvent>()
    private lateinit var tagsStorage: TagStorage

    override suspend fun init(
        path: Path,
        resourceIds: List<ResourceId>,
        updateTags: suspend (Tags) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            val tagsStorageRepo = TagsStorageRepo(this, statsFlow)
            tagsStorage = tagsStorageRepo.provide(RootIndex.provide(path))
            updateTags(readTags(resourceIds))
        }
    }

    override suspend fun writeTags(
        id: ResourceId,
        tags: Tags,
    ) {
        tagsStorage.setTags(id, tags)
        Log.d("Tags Repo", tagsStorage.getTags(id).toString())
        tagsStorage.persist()
    }

    override suspend fun readTags(id: ResourceId): Tags {
        return tagsStorage.getTags(id)
    }

    override suspend fun readTags(ids: List<ResourceId>): Tags {
        return tagsStorage.getTags(ids)
    }
}
