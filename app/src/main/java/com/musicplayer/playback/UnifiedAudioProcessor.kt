package com.musicplayer.playback

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

@UnstableApi
class UnifiedAudioProcessor : BaseAudioProcessor() {

    // Toggles
    var lufsEnabled = true
    var psychoEnabled = true
    var adaptiveEnabled = true
    var stereoEnabled = true
    var tiltEnabled = false
    var tiltValue = 0.0f

    // EQ Gains
    val bandGains = FloatArray(6) { 0f }
    private val filtersL = Array(6) { Biquad() }
    private val filtersR = Array(6) { Biquad() }
    private val freqs = floatArrayOf(60f, 150f, 400f, 1000f, 3000f, 8000f)

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != androidx.media3.common.C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        filtersL.forEach { it.prepare(inputAudioFormat.sampleRate.toFloat()) }
        filtersR.forEach { it.prepare(inputAudioFormat.sampleRate.toFloat()) }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val buffer = replaceOutputBuffer(remaining)
        
        // Simple 16-bit PCM to Float conversion for processing
        while (inputBuffer.hasRemaining()) {
            var left = inputBuffer.short.toFloat() / 32768.0f
            var right = if (inputAudioFormat.channelCount > 1) inputBuffer.short.toFloat() / 32768.0f else left

            // Apply DSP (simplified version of the C++ logic)
            
            // EQ
            for (i in 0 until 6) {
                filtersL[i].setPeaking(freqs[i], 1.0f, bandGains[i])
                filtersR[i].setPeaking(freqs[i], 1.0f, bandGains[i])
                left = filtersL[i].process(left)
                right = filtersR[i].process(right)
            }

            // Psychoacoustic
            if (psychoEnabled) {
                left = left + (left * left * 0.03f)
                right = right + (right * right * 0.03f)
            }

            // Stereo Widening
            if (stereoEnabled && inputAudioFormat.channelCount > 1) {
                val mid = (left + right) * 0.5f
                var side = (left - right) * 0.5f
                side *= 1.25f
                left = mid + side
                right = mid - side
            }

            // Clamp and convert back to Short
            buffer.putShort((left.coerceIn(-1f, 1f) * 32767).toInt().toShort())
            if (inputAudioFormat.channelCount > 1) {
                buffer.putShort((right.coerceIn(-1f, 1f) * 32767).toInt().toShort())
            }
        }
        buffer.flip()
    }

    private class Biquad {
        var a0 = 1f; var a1 = 0f; var a2 = 0f; var b1 = 0f; var b2 = 0f
        var z1 = 0f; var z2 = 0f
        var sampleRate = 44100f
        
        fun prepare(sr: Float) { sampleRate = sr }

        fun setPeaking(f: Float, q: Float, gainDb: Float) {
            val a = 10.0f.pow(gainDb / 40.0f)
            val w0 = 2.0f * PI.toFloat() * f / sampleRate
            val alpha = sin(w0) / (2.0f * q)
            val c = cos(w0)
            val a0v = 1 + alpha / a
            a0 = (1 + alpha * a) / a0v
            a1 = (-2 * c) / a0v
            a2 = (1 - alpha * a) / a0v
            b1 = (-2 * c) / a0v
            b2 = (1 - alpha / a) / a0v
        }

        fun process(x: Float): Float {
            val y = a0 * x + z1
            z1 = a1 * x - b1 * y + z2
            z2 = a2 * x - b2 * y
            return y
        }
    }
}
