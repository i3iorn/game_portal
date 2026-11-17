package com.micro.gameportal

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Haptic + tone feedback for game events, generated on the fly (no assets),
 * gated by the global "Sound & vibration" setting. All calls fail soft.
 */
object Feedback {

    private const val TONE_VOLUME = 60
    private var toneGenerator: ToneGenerator? = null

    fun enabled(context: Context): Boolean = GameSettings(context).feedbackEnabled

    /** Short vibration on [view] (no-ops when the setting is off). */
    fun haptic(view: View) {
        if (!enabled(view.context)) return
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    /** Short system tone; [toneType] is a ToneGenerator.TONE_* constant. */
    fun play(context: Context, toneType: Int = ToneGenerator.TONE_PROP_BEEP, durationMs: Int = 150) {
        if (!enabled(context)) return
        try {
            val gen = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, TONE_VOLUME).also { toneGenerator = it }
            gen.startTone(toneType, durationMs)
        } catch (_: RuntimeException) {
            // ToneGenerator unavailable (e.g. audio resource busy) — ignore.
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
