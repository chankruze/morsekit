package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.morse.TranslationIssue

enum class TranslationStatus {
    /** Nothing (or only whitespace) typed yet. */
    Empty,

    /** Everything in the input was translated. */
    Complete,

    /** Translated, but some input was skipped or couldn't be read. */
    Partial,

    /** There is input, but none of it could be translated. */
    Invalid,
}

data class TranslatorUiState(
    val direction: TranslationDirection = TranslationDirection.TextToMorse,
    val input: String = "",
    val output: String = "",
    val issues: List<TranslationIssue> = emptyList(),
) {
    /** True when [output] contains something worth copying or sharing, not just placeholders. */
    val hasOutput: Boolean
        get() = output.any { it != MorseCodec.REPLACEMENT_CHAR && !it.isWhitespace() }

    val status: TranslationStatus
        get() = when {
            // Separator-only Morse such as " / " has nothing to translate and nothing wrong with it.
            input.isBlank() || (!hasOutput && issues.isEmpty()) -> TranslationStatus.Empty
            !hasOutput -> TranslationStatus.Invalid
            issues.isNotEmpty() -> TranslationStatus.Partial
            else -> TranslationStatus.Complete
        }
}
