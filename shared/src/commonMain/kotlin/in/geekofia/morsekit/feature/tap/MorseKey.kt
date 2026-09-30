package `in`.geekofia.morsekit.feature.tap

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A straight key: goes down on touch and up on release (or when the touch is cancelled, so a
 * key can't stick). How long it's held is measured by the caller.
 *
 * Screen readers can't time a press (double-tap is one short gesture), so the key offers
 * actions instead: activating it adds a dot, and the actions menu has Dash, End letter and Space.
 */
@Composable
fun MorseKey(
    pressed: Boolean,
    heldAsDash: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onDot: () -> Unit,
    onDash: () -> Unit,
    onEndLetter: () -> Unit,
    onSpace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        when {
            heldAsDash -> colors.tertiary
            pressed -> colors.primary
            else -> colors.primaryContainer
        },
    )
    val content = when {
        heldAsDash -> colors.onTertiary
        pressed -> colors.onPrimary
        else -> colors.onPrimaryContainer
    }
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    currentOnPress()
                    waitForUpOrCancellation()
                    currentOnRelease()
                }
            }
            .clearAndSetSemantics {
                contentDescription = "Morse key"
                role = Role.Button
                onClick(label = "Dot") { onDot(); true }
                customActions = listOf(
                    CustomAccessibilityAction("Dash") { onDash(); true },
                    CustomAccessibilityAction("End letter") { onEndLetter(); true },
                    CustomAccessibilityAction("Space") { onSpace(); true },
                )
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = when {
                    heldAsDash -> "DASH"
                    pressed -> "DOT"
                    else -> "TAP · HOLD"
                },
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Tap for a dot, hold for a dash. Pause to end a letter.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
