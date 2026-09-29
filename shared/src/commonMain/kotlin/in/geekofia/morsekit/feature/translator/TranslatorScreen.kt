package `in`.geekofia.morsekit.feature.translator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import `in`.geekofia.morsekit.core.audio.PlaybackStatus
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.feature.playback.MorsePlaybackViewModel
import `in`.geekofia.morsekit.feature.playback.MorseTorchViewModel
import `in`.geekofia.morsekit.feature.playback.MorseVibrationViewModel
import `in`.geekofia.morsekit.feature.playback.TransmitFab
import `in`.geekofia.morsekit.feature.playback.TransmitOutput
import `in`.geekofia.morsekit.navigation.ExitConfirmation
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.MorseTextField
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import kotlinx.coroutines.launch
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_close
import morsekit.shared.generated.resources.ic_copy
import morsekit.shared.generated.resources.ic_share
import morsekit.shared.generated.resources.ic_translate
import morsekit.shared.generated.resources.morsekit_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Wires the ViewModels and platform services to the stateless [TranslatorScreen].
 *
 * Only one output transmits at a time. Transmission stops whenever the content changes, so it
 * never sends a stale message. The flashlight and vibration also stop when this screen leaves
 * composition (another tab) or the app goes to the background, so they can never keep running
 * unattended.
 */
@Composable
fun TranslatorRoute(
    platformServices: PlatformServices,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onExit: (() -> Unit)? = null,
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
    var showFlashWarning by remember { mutableStateOf(false) }

    val stopUnattendedOutputs = {
        torchViewModel.stop()
        vibrationViewModel.stop()
    }
    val stopTransmitting = {
        playbackViewModel.stop()
        stopUnattendedOutputs()
    }
    val start: (TransmitOutput) -> Unit = { output ->
        stopTransmitting()
        when (output) {
            TransmitOutput.Sound -> playbackViewModel.play(state.message)
            TransmitOutput.Flash -> torchViewModel.start(state.message)
            TransmitOutput.Vibrate -> vibrationViewModel.start(state.message)
        }
    }
    val activeOutput = when {
        playbackStatus == PlaybackStatus.Playing || playbackViewModel.isPreparing -> TransmitOutput.Sound
        torchViewModel.isTransmitting -> TransmitOutput.Flash
        vibrationViewModel.isTransmitting -> TransmitOutput.Vibrate
        else -> null
    }

    DisposableEffect(torchViewModel, vibrationViewModel) {
        onDispose { stopUnattendedOutputs() }
    }

    // The translator is the start screen, so back here would leave the app: ask for a second press.
    // Only where the host can exit (Android); iOS has no back button to confirm.
    val exitConfirmation = remember { ExitConfirmation() }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = onExit != null,
        onBackCompleted = {
            when (exitConfirmation.onBack()) {
                ExitConfirmation.Decision.Exit -> onExit?.invoke()
                ExitConfirmation.Decision.ShowHint -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Press back again to exit", duration = SnackbarDuration.Short)
                }
            }
        },
    )
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { stopUnattendedOutputs() }

    val transmitError = playbackViewModel.errorMessage ?: torchViewModel.errorMessage ?: vibrationViewModel.errorMessage
    LaunchedEffect(transmitError) {
        if (transmitError != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(transmitError)
        }
    }

    if (showFlashWarning) {
        FlashWarningDialog(
            onConfirm = {
                showFlashWarning = false
                settingsRepository.acknowledgeFlashWarning()
                start(TransmitOutput.Flash)
            },
            onDismiss = { showFlashWarning = false },
        )
    }

    ScreenScaffold(title = { AppTitle() }, modifier = modifier, centerTitle = true) { contentModifier ->
        TranslatorScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onInputChange = { stopTransmitting(); viewModel.onInputChange(it) },
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
            onShare = { shareMessage(state)?.let(platformServices.share::shareText) },
            modifier = contentModifier,
            transmitFab = {
                TransmitFab(
                    activeOutput = activeOutput,
                    canTransmit = !state.message.isEmpty,
                    isFlashAvailable = torchViewModel.isTorchAvailable,
                    isVibrationAvailable = vibrationViewModel.isVibrationAvailable,
                    wordsPerMinute = settings.wordsPerMinute,
                    onWordsPerMinuteChange = settingsRepository::setWordsPerMinute,
                    onStart = { output ->
                        if (output == TransmitOutput.Flash && !settings.flashWarningAcknowledged) {
                            showFlashWarning = true
                        } else {
                            start(output)
                        }
                    },
                    onStop = stopTransmitting,
                )
            },
        )
    }
}

/** The home screen's title: the app icon and name. */
@Composable
private fun AppTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Decorative: the name next to it already identifies the app to screen readers.
        Image(painterResource(Res.drawable.morsekit_logo), contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Text("MorseKit")
    }
}

@Composable
fun TranslatorScreen(
    state: TranslatorUiState,
    onInputChange: (String) -> Unit,
    onSwap: () -> Unit,
    onClear: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    transmitFab: @Composable () -> Unit = {},
) {
    val isMorseInput = state.direction == TranslationDirection.MorseToText
    val issueText = remember(state.issues) { issueMessages(state.issues).joinToString("\n") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DirectionBar(direction = state.direction, onSwap = onSwap)
            InputCard(
                state = state,
                isMorseInput = isMorseInput,
                issueText = issueText,
                onInputChange = onInputChange,
                onClear = onClear,
            )
            OutputCard(state = state, isMorseInput = isMorseInput, onCopy = onCopy, onShare = onShare)
            // Keeps the last card's actions clear of the floating Transmit button.
            Spacer(Modifier.height(FAB_CLEARANCE))
        }

        transmitFab()

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = FAB_CLEARANCE),
        )
    }
}

private val FAB_CLEARANCE = 88.dp

/** `Text ⇄ Morse`: source on the left, target on the right, swap in between. */
@Composable
private fun DirectionBar(direction: TranslationDirection, onSwap: () -> Unit) {
    val (from, to) = when (direction) {
        TranslationDirection.TextToMorse -> "Text" to "Morse"
        TranslationDirection.MorseToText -> "Morse" to "Text"
    }
    // Half a turn per swap, so the arrows visibly flip.
    val rotation by animateFloatAsState(if (direction == TranslationDirection.TextToMorse) 0f else 180f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DirectionPill(from, Modifier.weight(1f))
        IconButton(onClick = onSwap) {
            Icon(
                painter = painterResource(Res.drawable.ic_translate),
                contentDescription = "Swap to $to to $from",
                modifier = Modifier.rotate(rotation),
            )
        }
        DirectionPill(to, Modifier.weight(1f))
    }
}

@Composable
private fun DirectionPill(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InputCard(
    state: TranslatorUiState,
    isMorseInput: Boolean,
    issueText: String,
    onInputChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        CardHeader(label = if (isMorseInput) "Morse" else "Text") {
            if (state.input.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(painterResource(Res.drawable.ic_close), contentDescription = "Clear")
                }
            }
        }
        MorseTextField(
            value = state.input,
            onValueChange = onInputChange,
            label = if (isMorseInput) "Morse" else "Text",
            placeholder = if (isMorseInput) "... --- ..." else "Enter text",
            supportingText = issueText.ifEmpty {
                if (isMorseInput) "Separate letters with a space and words with /" else null
            },
            isError = issueText.isNotEmpty(),
            isMorse = isMorseInput,
            borderless = true,
        )
        // TextField puts no space under its supporting text, so the hint would sit on the card edge.
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OutputCard(
    state: TranslatorUiState,
    isMorseInput: Boolean,
    onCopy: (String) -> Unit,
    onShare: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        CardHeader(label = if (isMorseInput) "Text" else "Morse")
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            TranslationOutput(state = state, isMorseInput = isMorseInput)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = { onCopy(state.output) }, enabled = state.hasOutput) {
                Icon(painterResource(Res.drawable.ic_copy), contentDescription = "Copy")
            }
            IconButton(onClick = onShare, enabled = state.hasOutput) {
                Icon(painterResource(Res.drawable.ic_share), contentDescription = "Share")
            }
        }
    }
}

/** Card title on the left, optional action on the right, at a constant height. */
@Composable
private fun CardHeader(label: String, action: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            color = LocalContentColor.current.copy(alpha = 0.72f),
        )
        action()
    }
}

@Composable
private fun TranslationOutput(state: TranslatorUiState, isMorseInput: Boolean) {
    when (state.status) {
        TranslationStatus.Empty -> OutputMessage(
            text = if (isMorseInput) "The text appears here." else "The Morse code appears here.",
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
private fun OutputMessage(text: String, color: Color = LocalContentColor.current.copy(alpha = 0.72f)) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color)
}

@Composable
private fun FlashWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Flashing light") },
        text = {
            Text(
                "The flashlight will flash repeatedly. Don't use it near anyone sensitive to " +
                    "flashing lights, including people with photosensitive epilepsy.",
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Flash") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
