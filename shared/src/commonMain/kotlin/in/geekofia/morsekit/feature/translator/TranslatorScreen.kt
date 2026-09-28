package `in`.geekofia.morsekit.feature.translator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.audio.PlaybackStatus
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.feature.playback.MorsePlaybackViewModel
import `in`.geekofia.morsekit.feature.playback.MorseTorchViewModel
import `in`.geekofia.morsekit.feature.playback.MorseVibrationViewModel
import `in`.geekofia.morsekit.feature.playback.SoundControls
import `in`.geekofia.morsekit.feature.playback.SpeedControl
import `in`.geekofia.morsekit.feature.playback.TorchControls
import `in`.geekofia.morsekit.feature.playback.VibrationControls
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.MorseTextField
import `in`.geekofia.morsekit.ui.components.SecondaryButton
import `in`.geekofia.morsekit.ui.components.SectionCard
import kotlinx.coroutines.launch

/**
 * Wires the ViewModels and platform services to the stateless [TranslatorScreen].
 *
 * Transmission stops whenever the content changes, so it never sends a stale message. The
 * flashlight and vibration also stop when this screen leaves composition (another tab) or the app
 * goes to the background, so they can never keep running unattended.
 */
@Composable
fun TranslatorRoute(
    platformServices: PlatformServices,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel = viewModel { TranslatorViewModel() },
    playbackViewModel: MorsePlaybackViewModel = viewModel {
        MorsePlaybackViewModel(platformServices.audioPlayer, settingsRepository)
    },
    torchViewModel: MorseTorchViewModel = viewModel {
        MorseTorchViewModel(platformServices.torch, settingsRepository)
    },
    vibrationViewModel: MorseVibrationViewModel = viewModel {
        MorseVibrationViewModel(platformServices.vibration, settingsRepository)
    },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = platformServices.clipboard
    val state = viewModel.uiState
    val playbackStatus by playbackViewModel.status.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    val stopUnattendedOutputs = {
        torchViewModel.stop()
        vibrationViewModel.stop()
    }
    val stopTransmitting = {
        playbackViewModel.stop()
        stopUnattendedOutputs()
    }

    DisposableEffect(torchViewModel, vibrationViewModel) {
        onDispose { stopUnattendedOutputs() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { stopUnattendedOutputs() }

    TranslatorScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onInputChange = { stopTransmitting(); viewModel.onInputChange(it) },
        onDirectionSelected = { stopTransmitting(); viewModel.onDirectionSelected(it) },
        onSwap = { stopTransmitting(); viewModel.swapDirection() },
        onClear = { stopTransmitting(); viewModel.onClear() },
        onCopy = { text ->
            clipboard.copyText(text)
            if (!clipboard.showsSystemConfirmation) {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Copied to clipboard")
                }
            }
        },
        onShare = platformServices.share::shareText,
        modifier = modifier,
        transmitControls = {
            val soundRunning = playbackStatus == PlaybackStatus.Playing || playbackViewModel.isPreparing
            SpeedControl(
                wordsPerMinute = settings.wordsPerMinute,
                onWordsPerMinuteChange = settingsRepository::setWordsPerMinute,
                // Both outputs are timed at start, so speed only changes while nothing runs.
                enabled = !soundRunning && !torchViewModel.isTransmitting && !vibrationViewModel.isTransmitting,
            )
            HorizontalDivider()
            Text("Sound", style = MaterialTheme.typography.titleSmall)
            SoundControls(
                isRunning = soundRunning,
                canTransmit = !state.message.isEmpty,
                errorMessage = playbackViewModel.errorMessage,
                onStart = { playbackViewModel.play(state.message) },
                onStop = playbackViewModel::stop,
            )
            HorizontalDivider()
            Text("Flashlight", style = MaterialTheme.typography.titleSmall)
            TorchControls(
                isAvailable = torchViewModel.isTorchAvailable,
                isTransmitting = torchViewModel.isTransmitting,
                canTransmit = !state.message.isEmpty,
                errorMessage = torchViewModel.errorMessage,
                onStart = { torchViewModel.start(state.message) },
                onStop = torchViewModel::stop,
            )
            HorizontalDivider()
            Text("Vibration", style = MaterialTheme.typography.titleSmall)
            VibrationControls(
                isAvailable = vibrationViewModel.isVibrationAvailable,
                isTransmitting = vibrationViewModel.isTransmitting,
                canTransmit = !state.message.isEmpty,
                errorMessage = vibrationViewModel.errorMessage,
                onStart = { vibrationViewModel.start(state.message) },
                onStop = vibrationViewModel::stop,
            )
        },
    )
}

@Composable
fun TranslatorScreen(
    state: TranslatorUiState,
    onInputChange: (String) -> Unit,
    onDirectionSelected: (TranslationDirection) -> Unit,
    onSwap: () -> Unit,
    onClear: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    transmitControls: @Composable ColumnScope.() -> Unit = {},
) {
    val isMorseInput = state.direction == TranslationDirection.MorseToText
    val issueText = remember(state.issues) { issueMessages(state.issues).joinToString("\n") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DirectionSelector(selected = state.direction, onSelected = onDirectionSelected)

            MorseTextField(
                value = state.input,
                onValueChange = onInputChange,
                label = if (isMorseInput) "Morse" else "Text",
                placeholder = if (isMorseInput) "... --- ..." else "SOS",
                supportingText = issueText.ifEmpty {
                    if (isMorseInput) "Separate letters with a space and words with /" else null
                },
                isError = issueText.isNotEmpty(),
                isMorse = isMorseInput,
            )

            SectionCard(title = if (isMorseInput) "Text" else "Morse") {
                TranslationOutput(state = state, isMorseInput = isMorseInput)
            }

            SectionCard(title = "Transmit") {
                transmitControls()
            }

            ActionRow {
                SecondaryButton("Copy", onClick = { onCopy(state.output) }, enabled = state.hasOutput)
                SecondaryButton("Share", onClick = { onShare(state.output) }, enabled = state.hasOutput)
                SecondaryButton("Swap", onClick = onSwap)
                SecondaryButton("Clear", onClick = onClear, enabled = state.input.isNotEmpty())
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

@Composable
private fun TranslationOutput(state: TranslatorUiState, isMorseInput: Boolean) {
    when (state.status) {
        TranslationStatus.Empty -> OutputMessage(
            text = if (isMorseInput) "Type Morse above to see it as text." else "Type text above to see it in Morse.",
        )
        TranslationStatus.Invalid -> OutputMessage(
            text = "Nothing here could be translated.",
            color = MaterialTheme.colorScheme.error,
        )
        TranslationStatus.Complete, TranslationStatus.Partial ->
            if (isMorseInput) {
                SelectionContainer {
                    Text(state.output, style = MaterialTheme.typography.headlineSmall)
                }
            } else {
                MorseDisplay(morse = state.output)
            }
    }
}

@Composable
private fun OutputMessage(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionRow(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectionSelector(
    selected: TranslationDirection,
    onSelected: (TranslationDirection) -> Unit,
) {
    val options = TranslationDirection.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, direction ->
            SegmentedButton(
                selected = direction == selected,
                onClick = { onSelected(direction) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(direction.label) },
            )
        }
    }
}

private val TranslationDirection.label: String
    get() = when (this) {
        TranslationDirection.TextToMorse -> "Text → Morse"
        TranslationDirection.MorseToText -> "Morse → Text"
    }
