package `in`.geekofia.morsekit.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import `in`.geekofia.morsekit.core.vibration.VibrationPattern

/**
 * Plays the whole pattern as one [VibrationEffect.createWaveform], so the OS does the timing.
 * Requires the `VIBRATE` permission (declared in the app manifest; granted at install).
 */
internal class AndroidVibrationController(context: Context) : VibrationController {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    override val isAvailable: Boolean get() = vibrator?.hasVibrator() == true

    override fun vibrate(pattern: VibrationPattern): Boolean {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return false
        // Waveform timings alternate off/on and start with an initial delay (off); ours starts on.
        val timings = LongArray(pattern.segments.size + 1)
        pattern.segments.forEachIndexed { index, segment -> timings[index + 1] = segment.durationMillis }
        return try {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, NO_REPEAT))
            true
        } catch (e: SecurityException) {
            false // VIBRATE permission missing
        }
    }

    override fun cancel() {
        vibrator?.cancel()
    }

    private companion object {
        const val NO_REPEAT = -1
    }
}
