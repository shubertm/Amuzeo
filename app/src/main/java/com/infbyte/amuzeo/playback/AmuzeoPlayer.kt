package com.infbyte.amuzeo.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

interface AmuzeoPlayer {
    var isPlayingChanged: (Boolean) -> Unit

    fun init()

    fun getPlayer(): Player?

    fun createPlaylist(items: List<MediaItem>)

    fun selectVideo(index: Int)

    fun playVideo()

    fun pauseVideo()

    fun stopVideo()

    fun prepareVideo()

    fun nextVideo()

    fun prevVideo()

    fun seekTo(position: Float)

    fun release()

    fun isPlaying(): Boolean

    fun getProgress(): Float
}
