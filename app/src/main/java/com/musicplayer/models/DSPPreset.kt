package com.musicplayer.models

data class DSPPreset(
    val name: String,
    val gains: FloatArray,
    val description: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DSPPreset
        if (name != other.name) return false
        if (!gains.contentEquals(other.gains)) return false
        return description == other.description
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + gains.contentHashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}

object DSPPresets {
    val Neutral = DSPPreset(
        "Neutral",
        floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f),
        "Flat reference response with no coloration"
    )

    val VocalForward = DSPPreset(
        "Vocal Forward",
        floatArrayOf(-3f, -2f, 0f, 3f, 4f, 1f),
        "Enhanced vocal presence with reduced bass masking"
    )

    val DeepRumble = DSPPreset(
        "Deep Rumble",
        floatArrayOf(6f, 4f, 0f, 0f, 0f, 1f),
        "Heavy sub-bass emphasis with clean mids"
    )

    val ClubPunch = DSPPreset(
        "Club Punch",
        floatArrayOf(2f, 4f, -2f, -1f, 1f, 1f),
        "Tight punchy bass with reduced muddiness"
    )

    val AirAndShimmer = DSPPreset(
        "Air & Shimmer",
        floatArrayOf(0f, 0f, 0f, 1f, 3f, 6f),
        "Open airy treble with enhanced detail"
    )

    val Cinematic = DSPPreset(
        "Cinematic",
        floatArrayOf(4f, 3f, -2f, -1f, 2f, 4f),
        "Wide immersive sound with enhanced lows and highs"
    )

    val SpeechClarity = DSPPreset(
        "Speech Clarity",
        floatArrayOf(-6f, -3f, -1f, 2f, 5f, 2f),
        "Focused speech enhancement and dialogue intelligibility"
    )

    val ReducedLow = DSPPreset(
        "Reduced Low",
        floatArrayOf(-5f, -4f, -2f, 0f, 1f, 1f),
        "Reduces excessive low-end energy"
    )

    val ReducedHighMids = DSPPreset(
        "Reduced High Mids",
        floatArrayOf(0f, 0f, -1f, -2f, -4f, -1f),
        "Smooths harsh upper-mid frequencies"
    )

    val CleanSeparation = DSPPreset(
        "Clean Separation",
        floatArrayOf(-2f, -1f, -3f, 1f, 3f, 2f),
        "Cleaner instrument separation and reduced masking"
    )

    val LowVolumeCompensation = DSPPreset(
        "Low Volume Compensation",
        floatArrayOf(4f, 2f, 0f, 0f, 2f, 4f),
        "Restores perceived bass and treble at low listening levels"
    )

    val WarmSmooth = DSPPreset(
        "Warm & Smooth",
        floatArrayOf(2f, 2f, 1f, 0f, -2f, -4f),
        "Warm relaxed sound with softer highs"
    )

    val DynamicImpact = DSPPreset(
        "Dynamic Impact",
        floatArrayOf(5f, 3f, 0f, 1f, 2f, 3f),
        "Enhanced punch, dynamics, and transient energy"
    )

    val AnalyticalDetail = DSPPreset(
        "Analytical Detail",
        floatArrayOf(-1f, 0f, 1f, 2f, 4f, 5f),
        "Enhanced micro-detail and upper-frequency resolution"
    )

    val SoftNightListening = DSPPreset(
        "Soft Night Listening",
        floatArrayOf(1f, 1f, 0f, -1f, -3f, -5f),
        "Relaxed low-fatigue tuning for quiet listening"
    )

    val BassIsolation = DSPPreset(
        "Bass Isolation",
        floatArrayOf(5f, 4f, -4f, -2f, 0f, 1f),
        "Focused isolated bass response with reduced bleed"
    )

    val TrebleDetail = DSPPreset(
        "Treble Detail",
        floatArrayOf(-1f, 0f, 0f, 1f, 3f, 6f),
        "Crisp detailed treble with expanded air region"
    )

    val ALL = listOf(
        Neutral, VocalForward, DeepRumble, ClubPunch, AirAndShimmer,
        Cinematic, SpeechClarity, ReducedLow, ReducedHighMids,
        CleanSeparation, LowVolumeCompensation, WarmSmooth,
        DynamicImpact, AnalyticalDetail, SoftNightListening,
        BassIsolation, TrebleDetail
    )
}
