package com.swein.easyphotox.util.sound.mediaplayer

import android.content.Context
import android.media.MediaPlayer

object MediaPlayerUtility {

    private lateinit var mediaPlayer: MediaPlayer

    fun init(context: Context, resourceId: Int, runnable: Runnable) {

        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer.isLooping = false
        mediaPlayer.setOnCompletionListener {
            runnable.run()
        }
    }

    fun play() {
        mediaPlayer.start()
    }

    fun release() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}