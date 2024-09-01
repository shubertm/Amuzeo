package com.infbyte.amuzeo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.memory.MemoryCache
import coil.request.ImageRequest

typealias VideoDuration = Long

fun Context.getImageLoader(): ImageLoader =
    ImageLoader.Builder(this)
        .memoryCache(MemoryCache.Builder(this).maxSizePercent(0.25).build())
        .components {
            add(VideoFrameDecoder.Factory())
        }.build()

fun Context.getVideoThumbnailRequest(mediaUri: Uri?): ImageRequest =
    ImageRequest.Builder(this)
        .data(mediaUri)
        .size(200, 50)
        .build()

fun Context.getVideoDuration(uri: Uri?): VideoDuration {
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(this, uri)
    val duration =
        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
    return duration
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
