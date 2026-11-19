package com.example.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/**
 * Provides iOS Taptic-Engine-inspired haptic feedback patterns across launcher interactions.
 */
class AuraHapticEngine(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService()
    }

    private fun hasVibrator(): Boolean = runCatching {
        vibrator?.hasVibrator() == true
    }.getOrDefault(false)

    /** Subtle crisp tick for standard taps, page dots, and slider detents. */
    fun tapLight() {
        if (!hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12L, 60))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12L)
            }
        }
    }

    /** Signature two-stage "pop" for icon/widget long-press and entering Edit Mode. */
    fun longPressPop() {
        if (!hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 12L, 24L, 18L)
                val amplitudes = intArrayOf(0, 75, 0, 185)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0L, 12L, 24L, 18L), -1)
            }
        }
    }

    /** Cushioned double pulse when expanding or dismissing a folder or notification stack. */
    fun folderOpen() {
        if (!hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 14L, 22L, 10L)
                val amplitudes = intArrayOf(0, 110, 0, 65)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        }
    }

    /** Gentle boundary detent for page swipes, Control Center pull-down, and Lock Screen unlock. */
    fun navigationSwipe() {
        if (!hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(16L, 95))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(16L)
            }
        }
    }

    /** Tactile switch click with subtle rising or falling emphasis for Control Center & Focus toggles. */
    fun controlToggle(enabled: Boolean) {
        if (!hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 12L, 16L, 14L)
                val amplitudes = if (enabled) {
                    intArrayOf(0, 80, 0, 160)
                } else {
                    intArrayOf(0, 140, 0, 60)
                }
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15L)
            }
        }
    }
}
