package com.infbyte.amuzeo.repo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.utils.getImageLoader
import com.infbyte.amuzeo.utils.getVideoThumbnailRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideosRepo(private val context: Context) {
    private val _videos = mutableListOf<Video>()
    private val videos: List<Video> = _videos
    private var folders: List<Folder> = listOf()
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

                    _videos += Video(item, extractFolderName(path), thumbnailRequest)
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
}
