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
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.ui.components.PrimaryButton
import kotlin.math.roundToInt

/** Transmission speed shared by audio and flashlight. Locked while either is running. */
@Composable
fun SpeedControl(
    wordsPerMinute: Int,
    onWordsPerMinuteChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
            enabled = enabled,
            modifier = Modifier.semantics { stateDescription = "$wordsPerMinute WPM" },
        )
        Text(
            text = if (enabled) "Also the default in Settings." else "Stop transmitting to change the speed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Flash / Stop for the torch, with a photosensitivity warning. */
@Composable
fun TorchControls(
    isAvailable: Boolean,
    isTransmitting: Boolean,
    canTransmit: Boolean,
    errorMessage: String?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isTransmitting) {
            PrimaryButton("Stop flashing", onClick = onStop)
        } else {
            PrimaryButton("Flash", onClick = onStart, enabled = isAvailable && canTransmit)
        }
        Text(
            text = if (isAvailable) {
                "Caution: flashing light. Don't use it near anyone sensitive to flashing lights, " +
                    "including people with photosensitive epilepsy."
            } else {
                "This device doesn't have a flashlight."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
