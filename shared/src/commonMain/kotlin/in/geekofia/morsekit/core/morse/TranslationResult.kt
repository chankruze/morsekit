package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseMessage

/** Problems found while translating. Translation never throws; it reports issues instead. */
sealed interface TranslationIssue {
    /** A text character with no Morse code in the alphabet. It is omitted from the output. */
    data class UnsupportedCharacter(val character: Char) : TranslationIssue

    /** A well-formed dot/dash sequence that isn't in the alphabet, e.g. `........`. */
    data class UnknownCode(val code: String) : TranslationIssue

    /** A token containing something other than dots and dashes, e.g. `..x`. */
    data class MalformedCode(val token: String) : TranslationIssue
}

data class EncodeResult(
    val message: MorseMessage,
    val issues: List<TranslationIssue> = emptyList(),
) {
    /** Canonical notation, e.g. `... --- ...`. */
    val morse: String = message.toString()

    val isValid: Boolean get() = issues.isEmpty()
}

data class DecodeResult(
    val text: String,
    val issues: List<TranslationIssue> = emptyList(),
) {
    val isValid: Boolean get() = issues.isEmpty()
}
