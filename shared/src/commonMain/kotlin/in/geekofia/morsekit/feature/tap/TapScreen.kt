package `in`.geekofia.morsekit.feature.tap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.review.ReviewPrompter
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapState
import `in`.geekofia.morsekit.core.tap.TapTiming
import `in`.geekofia.morsekit.feature.translator.revealMessage
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import `in`.geekofia.morsekit.ui.components.SpeedStepper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_backspace
import morsekit.shared.generated.resources.ic_close
import morsekit.shared.generated.resources.ic_copy
import morsekit.shared.generated.resources.ic_share
import org.jetbrains.compose.resources.painterResource

@Composable
fun TapRoute(
    platformServices: PlatformServices,
    settingsRepository: SettingsRepository,
    reviewPrompter: ReviewPrompter,
    modifier: Modifier = Modifier,
    viewModel: TapViewModel = viewModel { TapViewModel(settingsRepository) },
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    val state = viewModel.state
    val heldAsDash = viewModel.heldAsDash
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Wakes up exactly when the decoder says something can change: no polling.
    LaunchedEffect(state, heldAsDash, settings.tapWordsPerMinute) {
        val wait = viewModel.millisUntilNextChange() ?: return@LaunchedEffect
        delay(wait)
        viewModel.advance()
    }
    // A second tick when a hold becomes a dash, so the difference can be felt.
    LaunchedEffect(heldAsDash) {
        if (heldAsDash) haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }

    val text = state.text.trimEnd()
    ScreenScaffold(title = { Text("Tap", modifier = Modifier.semantics { heading() }) }, modifier = modifier) { inner ->
        Box(inner.fillMaxSize()) {
            TapScreen(
                state = state,
                heldAsDash = heldAsDash,
                timing = viewModel.timing,
                onPress = {
                    haptics.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    viewModel.press()
                },
                onRelease = viewModel::release,
                onDot = viewModel::dot,
                onDash = viewModel::dash,
                onEndLetter = viewModel::endLetter,
                onSpace = viewModel::space,
                onBackspace = viewModel::backspace,
                onClear = viewModel::clear,
                onCopy = {
                    platformServices.clipboard.copyText(text)
                    if (!platformServices.clipboard.showsSystemConfirmation) {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("Copied to clipboard")
                        }
                    }
                    reviewPrompter.recordSuccessfulUse()
                },
                onShare = {
                    platformServices.share.shareText(
                        revealMessage(intro = "📡 I tapped out a Morse message with MorseKit:", text = text, morse = state.morse),
                    )
                    reviewPrompter.recordSuccessfulUse()
                },
                onTapSpeedChange = viewModel::setTapSpeed,
            )
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(8.dp))
        }
    }
}

/** Stateless: the decoded text, its tools, the key and the tap speed. */
@Composable
fun TapScreen(
    state: TapState,
    heldAsDash: Boolean,
    timing: TapTiming,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onDot: () -> Unit,
    onDash: () -> Unit,
    onEndLetter: () -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onTapSpeedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MessageCard(state)
        MessageActions(
            hasText = state.text.isNotBlank(),
            isEmpty = state.isEmpty,
            onBackspace = onBackspace,
            onClear = onClear,
            onCopy = onCopy,
            onShare = onShare,
        )
        MorseKey(
            pressed = state.isPressed,
            heldAsDash = heldAsDash,
            onPress = onPress,
            onRelease = onRelease,
            onDot = onDot,
            onDash = onDash,
            onEndLetter = onEndLetter,
            onSpace = onSpace,
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = KEY_MIN_HEIGHT),
        )
        TapSpeed(timing = timing, onChange = onTapSpeedChange)
    }
}

@Composable
private fun MessageCard(state: TapState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = state.text.ifEmpty { "Your message appears here." },
                style = if (state.text.isEmpty()) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.headlineSmall,
                color = if (state.text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                // Announces each finished letter to screen readers.
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            MorseDisplay(morse = state.morse, style = MaterialTheme.typography.bodyMedium)
            if (state.currentLetter.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Keying", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    MorseDisplay(morse = state.currentLetter, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun MessageActions(
    hasText: Boolean,
    isEmpty: Boolean,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        IconButton(onClick = onBackspace, enabled = !isEmpty) {
            Icon(painterResource(Res.drawable.ic_backspace), contentDescription = "Delete")
        }
        IconButton(onClick = onClear, enabled = !isEmpty) {
            Icon(painterResource(Res.drawable.ic_close), contentDescription = "Clear")
        }
        IconButton(onClick = onCopy, enabled = hasText) {
            Icon(painterResource(Res.drawable.ic_copy), contentDescription = "Copy")
        }
        IconButton(onClick = onShare, enabled = hasText) {
            Icon(painterResource(Res.drawable.ic_share), contentDescription = "Share")
        }
    }
}

/** The stepper, and what the speed means in milliseconds, so the thresholds aren't a mystery. */
@Composable
private fun TapSpeed(timing: TapTiming, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        SpeedStepper(wordsPerMinute = timing.wordsPerMinute, onChange = onChange, label = "Tap speed")
        Text(
            text = "Dash from ${timing.dashThresholdMillis} ms · letter ends after ${timing.letterGapMillis} ms · " +
                "word after ${timing.wordGapMillis} ms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private val KEY_MIN_HEIGHT = 160.dp
