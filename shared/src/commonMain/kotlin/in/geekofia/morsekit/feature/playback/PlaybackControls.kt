package `in`.geekofia.morsekit.feature.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.audio.PlaybackStatus
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.ui.components.PrimaryButton
import `in`.geekofia.morsekit.ui.components.SecondaryButton
import kotlin.math.roundToInt

/** Play/Pause/Resume, Stop and a speed slider. Stateless; the caller owns playback state. */
@Composable
fun PlaybackControls(
    status: PlaybackStatus,
    isPreparing: Boolean,
    canPlay: Boolean,
    wordsPerMinute: Int,
    errorMessage: String?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onWordsPerMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIdle = status == PlaybackStatus.Idle && !isPreparing
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (status) {
                PlaybackStatus.Idle -> PrimaryButton(
                    text = if (isPreparing) "Preparing…" else "Play",
                    onClick = onPlay,
                    enabled = canPlay && !isPreparing,
                )
                PlaybackStatus.Playing -> PrimaryButton("Pause", onClick = onPause)
                PlaybackStatus.Paused -> PrimaryButton("Resume", onClick = onResume)
            }
            SecondaryButton("Stop", onClick = onStop, enabled = !isIdle)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Speed", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "$wordsPerMinute WPM",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = wordsPerMinute.toFloat(),
            onValueChange = { onWordsPerMinuteChange(it.roundToInt()) },
            valueRange = MorseTiming.MIN_WPM.toFloat()..MorseTiming.MAX_WPM.toFloat(),
            // The audio is rendered at a fixed speed, so speed changes only while stopped.
            enabled = isIdle,
            modifier = Modifier.semantics { stateDescription = "$wordsPerMinute WPM" },
        )
        Text(
            text = if (isIdle) "Also the default in Settings." else "Stop playback to change the speed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
