package `in`.geekofia.morsekit.feature.playback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_add
import morsekit.shared.generated.resources.ic_close
import morsekit.shared.generated.resources.ic_flashlight
import morsekit.shared.generated.resources.ic_remove
import morsekit.shared.generated.resources.ic_stop
import morsekit.shared.generated.resources.ic_transmit
import morsekit.shared.generated.resources.ic_vibration
import morsekit.shared.generated.resources.ic_volume
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Transmit button as a speed dial. Collapsed: a FAB (hidden when there's nothing to send).
 * Expanded: a speed stepper and one option per [TransmitOutput]. While an output runs
 * ([activeOutput] non-null) it becomes an extended "Stop …" FAB.
 *
 * Fills its parent so it can draw the dismiss scrim; place it last in a Box over the content.
 * Stateless apart from whether the menu is open.
 */
@Composable
fun TransmitFab(
    activeOutput: TransmitOutput?,
    canTransmit: Boolean,
    isFlashAvailable: Boolean,
    isVibrationAvailable: Boolean,
    wordsPerMinute: Int,
    onWordsPerMinuteChange: (Int) -> Unit,
    onStart: (TransmitOutput) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val menuOpen = expanded && activeOutput == null && canTransmit

    Box(modifier = modifier.fillMaxSize()) {
        if (menuOpen) {
            // Tap anywhere outside to close. No semantics: the FAB's close button covers accessibility.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .pointerInput(Unit) { detectTapGestures { expanded = false } },
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(visible = menuOpen, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpeedStepper(wordsPerMinute, onWordsPerMinuteChange)
                    OptionRow("Sound", null, Res.drawable.ic_volume, available = true) {
                        expanded = false
                        onStart(TransmitOutput.Sound)
                    }
                    OptionRow(
                        label = "Flash",
                        note = if (isFlashAvailable) "Flashing light" else "No flashlight",
                        icon = Res.drawable.ic_flashlight,
                        available = isFlashAvailable,
                    ) {
                        expanded = false
                        onStart(TransmitOutput.Flash)
                    }
                    OptionRow(
                        label = "Vibrate",
                        note = if (isVibrationAvailable) null else "Can't vibrate",
                        icon = Res.drawable.ic_vibration,
                        available = isVibrationAvailable,
                    ) {
                        expanded = false
                        onStart(TransmitOutput.Vibrate)
                    }
                }
            }

            when {
                activeOutput != null -> ExtendedFloatingActionButton(
                    onClick = onStop,
                    icon = { Icon(painterResource(Res.drawable.ic_stop), contentDescription = null) },
                    text = { Text(activeOutput.stopLabel) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
                canTransmit -> FloatingActionButton(onClick = { expanded = !expanded }) {
                    Icon(
                        painter = painterResource(if (menuOpen) Res.drawable.ic_close else Res.drawable.ic_transmit),
                        contentDescription = if (menuOpen) "Close transmit options" else "Transmit",
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedStepper(wordsPerMinute: Int, onChange: (Int) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onChange(stepWpm(wordsPerMinute, faster = false)) },
                enabled = wordsPerMinute > WPM_STEPS.first(),
            ) {
                Icon(painterResource(Res.drawable.ic_remove), contentDescription = "Slower")
            }
            Text(
                text = "$wordsPerMinute WPM",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.semantics { stateDescription = "Speed $wordsPerMinute words per minute" },
            )
            IconButton(
                onClick = { onChange(stepWpm(wordsPerMinute, faster = true)) },
                enabled = wordsPerMinute < WPM_STEPS.last(),
            ) {
                Icon(painterResource(Res.drawable.ic_add), contentDescription = "Faster")
            }
        }
    }
}

/** A labelled mini FAB. Both the label and the button start the output. */
@Composable
private fun OptionRow(
    label: String,
    note: String?,
    icon: DrawableResource,
    available: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.alpha(if (available) 1f else 0.5f),
    ) {
        Surface(
            onClick = onClick,
            enabled = available,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 2.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                Text(label, style = MaterialTheme.typography.labelLarge)
                if (note != null) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        SmallFloatingActionButton(
            onClick = { if (available) onClick() },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = if (available) Modifier else Modifier.semantics { disabled() },
        ) {
            Icon(painterResource(icon), contentDescription = label)
        }
    }
}

private val TransmitOutput.stopLabel: String
    get() = when (this) {
        TransmitOutput.Sound -> "Stop sound"
        TransmitOutput.Flash -> "Stop flashing"
        TransmitOutput.Vibrate -> "Stop vibrating"
    }
