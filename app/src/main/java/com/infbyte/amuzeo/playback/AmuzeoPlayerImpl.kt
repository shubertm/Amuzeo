package com.infbyte.amuzeo.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AmuzeoPlayerImpl(context: Context) : AmuzeoPlayer {
    private var exoPlayer: ExoPlayer =
        ExoPlayer.Builder(context)
            .build()

    override var isPlayingChanged: (Boolean) -> Unit = {}

    private val listener =
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                isPlayingChanged(isPlaying)
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int,
            ) {
                super.onMediaItemTransition(mediaItem, reason)
            }
        }

    override fun init() {
        exoPlayer.addListener(listener)
    }

    override fun createPlaylist(items: List<MediaItem>) {
        exoPlayer.setMediaItems(items)
    }

    override fun selectVideo(index: Int) {
        exoPlayer.run {
            seekTo(index, 0)
            prepare()
            playWhenReady = true
        }
    }

    override fun playVideo() {
        exoPlayer.play()
    }

    override fun pauseVideo() {
        exoPlayer.pause()
    }

    override fun stopVideo() {
        exoPlayer.stop()
    }

    override fun nextVideo() {
        exoPlayer.seekToNext()
    }

    override fun prevVideo() {
        exoPlayer.seekToPrevious()
    }

    override fun prepareVideo() {
        exoPlayer.prepare()
    }

    override fun seekTo(position: Float) {
        exoPlayer.run {
            val pos = (position * duration).toLong()
            seekTo(pos)
        }
    }

    override fun getPlayer(): Player = exoPlayer

    override fun isPlaying(): Boolean = exoPlayer.isPlaying

    override fun getProgress(): Float {
        val position = exoPlayer.currentPosition.toFloat()
        val duration = exoPlayer.duration.toFloat()
        val progress = position / duration
        return progress
    }

    override fun release() {
        exoPlayer.run {
            stop()
            release()
            removeListener(listener)
        }
    }
}
