package com.example.hustlefix.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.hustlefix.R

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Plays a smooth, subtle sound. 
     * We use a lower volume (0.3f) to keep it from being "too much".
     */
    fun playSound(context: Context, resId: Int, volume: Float = 0.3f) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.setVolume(volume, volume)
            mediaPlayer?.start()
        } catch (e: Exception) {}
    }

    fun playClick(context: Context) {
        // Only use the very subtle system tick
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.2f)
        } catch (e: Exception) {}
    }

    fun playSuccess(context: Context) {
        // Soft chime for success
        playSound(context, R.raw.splash_chime, 0.4f)
    }

    fun playNotification(context: Context) {
        // Instead of a loud ringtone, use the soft success chime at low volume
        playSound(context, R.raw.splash_chime, 0.2f)
    }

    fun playEmergency(context: Context) {
        // Keep emergencies audible but not ear-piercing
        playSound(context, R.raw.splash_chime, 0.6f)
    }
}
