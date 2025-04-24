package com.infbyte.amuzeo.repo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import coil.size.Size
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.utils.createVideoThumbnail
import com.infbyte.amuzeo.utils.getImageLoader
import com.infbyte.amuzeo.utils.getVideoThumbnailRequest
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.computeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.fileSize

class VideosRepo(private val context: Context) {
    private val _videos = mutableListOf<Video>()
    private val videos: List<Video> = _videos
    private var folders: List<Folder> = listOf()
    private val resourceIds: MutableList<ResourceId> = mutableListOf()
    private val _folderPaths: MutableList<Path> = mutableListOf()
    private val folderPaths: List<Path> = _folderPaths
    private val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    private val projection =
        arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DATA,
        )
    private val selection = null
    private val selectionArgs = null
    private val sortOrder = null

    val videoImageLoader = context.getImageLoader()
    val taggedVideoImageLoader = context.getImageLoader()

    suspend fun loadVideos(
        isLoading: () -> Unit,
        onComplete: (videos: List<Video>, folders: List<Folder>, ids: List<ResourceId>) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            isLoading()
            val contentResolver = context.contentResolver
            val query =
                contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder,
                )
            query?.let {
                val idColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val artistColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                val albumColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
                val pathColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val songUri =
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id,
                        )

                    val path = it.getString(pathColumn)

                    val meta =
                        MediaMetadata.Builder()
                            .setAlbumTitle(album)
                            .setArtist(artist)
                            .setTitle(title)
                            .build()

                    val item =
                        MediaItem.Builder()
                            .setMediaMetadata(meta)
                            .setUri(songUri)
                            .setMediaId(id.toString())
                            .build()
                    val thumbnailRequest = context.getVideoThumbnailRequest(songUri)
                    val thumbnail = context.createVideoThumbnail(Path(path), android.util.Size(640, 480))

                    val videoPath = Path(path)

                    val size = videoPath.fileSize()
                    val resourceId = computeId(size, videoPath)

                    resourceIds.add(resourceId)

                    _videos +=
                        Video(
                            item = item,
                            folder = extractFolderName(path),
                            resourceId = resourceId,
                            thumbnail = thumbnail,
                            thumbnailRequest = thumbnailRequest,
                        )

                    _folderPaths.add(extractFolderPath(path))
                }
                query.close()
            }
            loadFolders()
            onComplete(videos, folders, resourceIds)
        }
    }

    private fun loadFolders() {
        folders =
            videos.map {
                extractFolderName(it.folder)
            }.toSet()
                .map { folder ->
                    val numberOfSongs =
                        videos.count { song ->
                            extractFolderName(
                                song.folder,
                            ) == folder
                        }
                    Folder(folder, numberOfSongs)
                }
    }

    private fun extractFolderName(path: String) = path.substringBeforeLast('/').substringAfterLast('/')

    private fun extractFolderPath(path: String) = Path(path.substringBeforeLast('/'))

    companion object {
        const val LOG_TAG = "Videos Repo"
    }
}
