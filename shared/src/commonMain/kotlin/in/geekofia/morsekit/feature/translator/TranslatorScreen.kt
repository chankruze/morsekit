package `in`.geekofia.morsekit.feature.translator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.MorseTextField
import `in`.geekofia.morsekit.ui.components.SecondaryButton
import `in`.geekofia.morsekit.ui.components.SectionCard
import kotlinx.coroutines.launch

/** Wires the ViewModel and platform services to the stateless [TranslatorScreen]. */
@Composable
fun TranslatorRoute(
    platformServices: PlatformServices,
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel = viewModel { TranslatorViewModel() },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = platformServices.clipboard

    TranslatorScreen(
        state = viewModel.uiState,
        snackbarHostState = snackbarHostState,
        onInputChange = viewModel::onInputChange,
        onDirectionSelected = viewModel::onDirectionSelected,
        onSwap = viewModel::swapDirection,
        onClear = viewModel::onClear,
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
