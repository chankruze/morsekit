package `in`.geekofia.morsekit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.timing.WPM_STEPS
import `in`.geekofia.morsekit.core.timing.stepWpm
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_add
import morsekit.shared.generated.resources.ic_remove
import org.jetbrains.compose.resources.painterResource

/**
 * `− 20 WPM +` along [WPM_STEPS]. The value is a polite live region, so screen readers announce
 * "[label] 20 words per minute" after each step.
 */
@Composable
fun SpeedStepper(
    wordsPerMinute: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Speed",
) {
    Surface(
        modifier = modifier,
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
                modifier = Modifier.semantics {
                    stateDescription = "$label $wordsPerMinute words per minute"
                    liveRegion = LiveRegionMode.Polite
                },
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
