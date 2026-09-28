package `in`.geekofia.morsekit.feature.translator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.TranslationIssue
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.MorseTextField
import `in`.geekofia.morsekit.ui.components.SecondaryButton
import `in`.geekofia.morsekit.ui.components.SectionCard

/** Wires the ViewModel and platform services to the stateless [TranslatorScreen]. */
@Composable
fun TranslatorRoute(
    platformServices: PlatformServices,
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel = viewModel { TranslatorViewModel() },
) {
    TranslatorScreen(
        state = viewModel.uiState,
        onInputChange = viewModel::onInputChange,
        onDirectionSelected = viewModel::onDirectionSelected,
        onSwap = viewModel::swapDirection,
        onClear = viewModel::onClear,
        onCopy = platformServices.clipboard::copyText,
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
) {
    val isMorseInput = state.direction == TranslationDirection.MorseToText
    Column(
        modifier = modifier
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
            supportingText = if (isMorseInput) "Separate letters with a space and words with /" else null,
            isMorse = isMorseInput,
        )

        SectionCard(title = if (isMorseInput) "Text" else "Morse") {
            if (isMorseInput) {
                Text(state.output, style = MaterialTheme.typography.headlineSmall)
            } else {
                MorseDisplay(morse = state.output, placeholder = "Translation appears here")
            }
            if (state.issues.isNotEmpty()) {
                Text(
                    text = state.issues.joinToString("\n") { it.message() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        ActionRow {
            SecondaryButton("Copy", onClick = { onCopy(state.output) }, enabled = state.hasOutput)
            SecondaryButton("Share", onClick = { onShare(state.output) }, enabled = state.hasOutput)
            SecondaryButton("Swap", onClick = onSwap)
            SecondaryButton("Clear", onClick = onClear, enabled = state.input.isNotEmpty())
        }
    }
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

private fun TranslationIssue.message(): String = when (this) {
    is TranslationIssue.UnsupportedCharacter -> "“$character” has no Morse code and was skipped"
    is TranslationIssue.UnknownCode -> "“$code” is not a known Morse code"
    is TranslationIssue.MalformedCode -> "“$token” should contain only dots and dashes"
}
