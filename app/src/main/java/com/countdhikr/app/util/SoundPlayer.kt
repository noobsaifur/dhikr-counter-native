package com.countdhikr.app.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

object SoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                return // Respect system silent/vibrate state!
            }

            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (e: Exception) {
            try {
                // Fallback secondary creation
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
