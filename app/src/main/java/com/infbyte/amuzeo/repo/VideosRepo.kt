package com.infbyte.amuzeo.repo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.utils.createVideoThumbnail
import com.infbyte.amuzeo.utils.getVideoDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

class VideosRepo(private val context: Context) {
    private val _videos = mutableListOf<Video>()
    val videos: List<Video> = _videos
    private var folders: List<Folder> = listOf()
    private val contentIds: MutableList<String> = mutableListOf()
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

    @OptIn(UnstableApi::class)
    suspend fun loadVideos(
        isLoading: () -> Unit,
        onComplete: (videos: List<Video>, folders: List<Folder>) -> Unit,
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
            _videos.clear()
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
                    val videoUri =
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
                            .setDurationMs(context.getVideoDuration(videoUri))
                            .build()

                    val item =
                        MediaItem.Builder()
                            .setMediaMetadata(meta)
                            .setUri(videoUri)
                            .setMediaId(id.toString())
                            .build()

                    val videoPath = Paths.get(path)

                    val fileId = videoPath.fileId()

                    val thumbnail = context.createVideoThumbnail(videoUri, Size(640, 480))

                    if (thumbnail != null) {
                        contentIds.add(fileId)

                        _videos +=
                            Video(
                                item = item,
                                folder = extractFolderName(path),
                                fileId = fileId,
                                thumbnail = thumbnail,
                            )

                        _folderPaths.add(extractFolderPath(path))
                    }
                }
                query.close()
            }
            loadFolders()
            onComplete(videos, folders)
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
        private const val LOG_TAG = "Videos Repo"
    }
}
