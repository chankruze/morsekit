package `in`.geekofia.morsekit.feature.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.audio.PlaybackStatus
import `in`.geekofia.morsekit.ui.components.PrimaryButton
import `in`.geekofia.morsekit.ui.components.SecondaryButton

/** Play/Pause/Resume and Stop for audio. Stateless; the caller owns playback state. */
@Composable
fun PlaybackControls(
    status: PlaybackStatus,
    isPreparing: Boolean,
    canPlay: Boolean,
    errorMessage: String?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
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

        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
