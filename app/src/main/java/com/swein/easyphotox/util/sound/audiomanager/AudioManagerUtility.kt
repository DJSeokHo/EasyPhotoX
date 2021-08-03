package com.swein.easyphotox.util.sound.audiomanager

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt

object AudioManagerUtility {

    private lateinit var audioManager: AudioManager
    private var maxVolume = 0

    private var currentVolume = 0
    private var currentIsMute = false

    fun init(context: Context) {

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun setNoMute() {

        currentIsMute = audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // un mute
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)

        // reset to zero first
        for(i in 0 until currentVolume) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
        }

        // set to half of max
        for(i in 0 until (maxVolume * 0.7).roundToInt()) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
        }
    }

    fun resetAfterPlay() {

        // reset to zero first
        for(i in 0 until (maxVolume * 0.7).roundToInt()) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
        }

        // check is mute
        if (currentIsMute) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        }
        else {
            // back to current
            for(i in 0 until currentVolume) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
            }
        }
    }

}