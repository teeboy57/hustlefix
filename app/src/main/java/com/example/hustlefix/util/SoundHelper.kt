package com.example.hustlefix.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.hustlefix.R

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Plays a sound with a specific volume. Default is 0.3f for smoothness.
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
        // Use the system's most subtle UI click effect
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.15f)
        } catch (e: Exception) {}
    }

    fun playSuccess(context: Context) {
        // Soft volume for success
        playSound(context, R.raw.splash_chime, 0.3f)
    }

    fun playNotification(context: Context) {
        // Subtle notification sound
        playSound(context, R.raw.splash_chime, 0.2f)
    }

    fun playEmergency(context: Context) {
        // Keep emergencies clear but not overly aggressive
        playSound(context, R.raw.splash_chime, 0.5f)
    }
}
