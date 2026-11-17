package com.micro.gameportal

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.view.HapticFeedbackConstants
import android.view.View
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Game feedback: soft synthesized tones (sine notes with attack/release
 * envelopes, rendered to PCM in memory — no assets) plus haptics, gated by
 * the global "Sound & vibration" setting. All calls fail soft.
 */
object Feedback {

    private const val SAMPLE_RATE = 44100
    private const val AMPLITUDE = 0.35 // headroom so stacked notes never clip

    /** Named game sounds, each a short melodic phrase. */
    enum class Sound { MOVE, MATCH, MISMATCH, SUCCESS, ERROR, UNDO }

    private val phrases: Map<Sound, List<Note>> = mapOf(
        Sound.MOVE to listOf(Note(520.0, 90)),
        Sound.MATCH to listOf(Note(660.0, 100), Note(880.0, 150)),
        Sound.MISMATCH to listOf(Note(330.0, 110), Note(262.0, 140)),
        Sound.SUCCESS to listOf(Note(523.25, 110), Note(659.25, 110), Note(783.99, 190)),
        Sound.ERROR to listOf(Note(130.0, 260)),
        Sound.UNDO to listOf(Note(440.0, 90), Note(392.0, 110)),
    )

    /** One sine note with an exponential release envelope. */
    data class Note(val freqHz: Double, val durationMs: Int, val volume: Double = 1.0)

    fun enabled(context: Context): Boolean = GameSettings(context).feedbackEnabled

    /** Haptic strength tiers, mapped to system feedback constants. */
    enum class HapticStrength { LIGHT, MEDIUM, STRONG }

    /** Vibration on [view]; LIGHT = tick, MEDIUM = key, STRONG = long-press. */
    fun haptic(view: View, strength: HapticStrength = HapticStrength.MEDIUM) {
        if (!enabled(view.context)) return
        val constant = when (strength) {
            HapticStrength.LIGHT -> HapticFeedbackConstants.CLOCK_TICK
            HapticStrength.MEDIUM -> HapticFeedbackConstants.VIRTUAL_KEY
            HapticStrength.STRONG -> HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }

    fun play(context: Context, sound: Sound) {
        if (!enabled(context)) return
        try { startTrack(render(sound)) } catch (_: IllegalStateException) { /* audio busy */ }
    }

    /** Renders the phrase to 16-bit mono PCM with per-note envelopes. */
    private fun render(sound: Sound): ShortArray {
        val notes = phrases.getValue(sound)
        val total = notes.sumOf { it.durationMs }
        val samples = ShortArray(SAMPLE_RATE * total / 1000)
        var offset = 0
        for (note in notes) {
            val count = SAMPLE_RATE * note.durationMs / 1000
            val attack = min(count, SAMPLE_RATE * 8 / 1000)
            for (i in 0 until count) {
                val envelope = when {
                    i < attack -> i.toDouble() / attack // gentle attack
                    else -> exp(-(i - attack).toDouble() / (count * 0.35)) // smooth decay
                }
                val value = sin(2 * PI * note.freqHz * i / SAMPLE_RATE) * envelope * note.volume
                val index = offset + i
                if (index < samples.size) {
                    samples[index] = (value * AMPLITUDE * Short.MAX_VALUE).toInt().toShort()
                }
            }
            offset += count
        }
        return samples
    }

    private fun startTrack(pcm: ShortArray) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        track.play()
        // MODE_STATIC loops forever unless the loop is cleared; clear it, then stop after the phrase.
        track.setLoopPoints(0, pcm.size, 0)
        val durationMs = pcm.size * 1000L / SAMPLE_RATE + 60
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try { track.stop() } catch (_: IllegalStateException) {}
            track.release()
        }, durationMs)
    }
}
