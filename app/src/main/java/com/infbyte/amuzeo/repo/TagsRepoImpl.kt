package com.infbyte.amuzeo.repo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.forEachLine
import kotlin.io.path.notExists
import kotlin.io.path.writeLines

class TagsRepoImpl : TagsRepo {
    private val iODispatcher = Dispatchers.IO

    private val idToTags: MutableMap<ContentId, Set<String>> = mutableMapOf()

    private lateinit var tagsStorageFile: Path

    private fun Path.tagsStorage(): Path {
        return resolve("tags")
    }

    override suspend fun init(path: Path) {
        withContext(Dispatchers.IO) {
            tagsStorageFile = path.tagsStorage()
            readTags()
        }
    }

    override suspend fun setTags(
        contentId: ContentId,
        tags: Set<String>,
    ) {
        idToTags[contentId] = tags
        writeTags()
    }

    override suspend fun getTags(contentId: ContentId): Set<String> {
        return idToTags[contentId] ?: emptySet()
    }

    override suspend fun getTags(contentIds: List<ContentId>): Set<String> {
        return contentIds.flatMap {
            getTags(it)
        }.toSet()
    }

    override suspend fun getTags(): Set<String> {
        return idToTags.flatMap {
            it.value
        }.toSet()
    }

    private suspend fun writeTags() {
        withContext(iODispatcher) {
            if (tagsStorageFile.notExists()) {
                Log.d(LOG_TAG, "Creating tags storage")
                tagsStorageFile.createFile()
            }

            val lines =
                idToTags.map { map ->
                    "${map.key}:${map.value.joinToString(",")}"
                }

            Log.d(LOG_TAG, "Writing all applied tags")
            tagsStorageFile.writeLines(lines)
        }
    }

    private suspend fun readTags() {
        withContext(iODispatcher) {
            if (tagsStorageFile.notExists()) {
                Log.d(LOG_TAG, "No tags are applied yet")
                return@withContext
            }
            Log.d(LOG_TAG, "Reading all applied tags")

            tagsStorageFile.forEachLine { line ->
                val parts = line.split(KEY_VALUE_SEPARATOR)
                val id = parts[0]
                val tags = parts[1].split(",").toSet()
                idToTags[id] = tags
            }
        }
    }

    companion object {
        private const val LOG_TAG = "TagsRepoImpl"
        private const val KEY_VALUE_SEPARATOR = ":"
    }
}
