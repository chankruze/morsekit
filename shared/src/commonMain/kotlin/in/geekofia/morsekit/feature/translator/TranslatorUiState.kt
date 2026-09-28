package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.TranslationIssue

data class TranslatorUiState(
    val direction: TranslationDirection = TranslationDirection.TextToMorse,
    val input: String = "",
    val output: String = "",
    val issues: List<TranslationIssue> = emptyList(),
) {
    val hasOutput: Boolean get() = output.isNotEmpty()
}
