package com.infbyte.amuzeo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_PREVIOUS_SYNC
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.nio.file.Path
import kotlin.math.max

typealias VideoDuration = Long

fun Context.getVideoDuration(uri: Uri?): VideoDuration {
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(this, uri)
    val duration =
        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
    metaRetriever.release()
    return duration
}

fun Context.createVideoThumbnail(
    path: Path,
    size: Size,
): ImageBitmap {
    val file = path.toFile()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ThumbnailUtils.createVideoThumbnail(
            file,
            size,
            null,
        ).asImageBitmap()
    }
    return createVideoThumbnail(Uri.fromFile(file), size)
}

fun Context.createVideoThumbnail(
    uri: Uri,
    size: Size,
): ImageBitmap {
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(this, uri)
    val thumbnailsBytes = metaRetriever.embeddedPicture

    if (thumbnailsBytes != null) {
        return BitmapFactory.decodeByteArray(thumbnailsBytes, 0, thumbnailsBytes.size).asImageBitmap()
    }

    val width =
        metaRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH,
        )?.toFloat() ?: size.width.toFloat()
    val height =
        metaRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
        )?.toFloat() ?: size.height.toFloat()

    val widthRatio = size.width.toFloat() / width
    val heightRatio = size.height.toFloat() / height

    val ratio = max(widthRatio, heightRatio)

    if (ratio > 1) {
        val reqWidth = width * ratio
        val reqHeight = height * ratio

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val frame =
                metaRetriever.getScaledFrameAtTime(
                    -1,
                    OPTION_PREVIOUS_SYNC,
                    reqWidth.toInt(),
                    reqHeight.toInt(),
                )
            metaRetriever.release()
            return frame?.asImageBitmap()!!
        }
    }

    // Should be scaled according to requested size
    val frame = metaRetriever.frameAtTime
    metaRetriever.release()
    return frame?.asImageBitmap()!!
}

fun VideoDuration.format(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val remSeconds = seconds % 60
    val hours = minutes / 60
    val remMinutes = minutes % 60
    return "${
        if (hours < 9) "0$hours" else hours
    }:${
        if (remMinutes < 9) "0$remMinutes" else remMinutes
    }:${
        if (remSeconds < 9) "0$remSeconds" else remSeconds
    }"
}

fun Uri.decodeImage(): Bitmap? = BitmapFactory.decodeFile(toString())
