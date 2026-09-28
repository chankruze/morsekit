package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCategory

/**
 * One row of the reference chart. [character], [code] and [category] come straight from a
 * [MorseAlphabet] mapping; this class only adds presentation details.
 */
data class ReferenceEntry(
    val character: Char,
    val code: String,
    val category: MorseCategory,
    /** Human-readable name for punctuation (searchable), `null` for letters and digits. */
    val name: String?,
) {
    /** What a screen reader announces, e.g. "A, dot dash" or "Comma, dash dash dot dot dash dash". */
    val accessibilityLabel: String
        get() = "${name ?: character}, " + code.map {
            if (it == MorseNotation.DOT) "dot" else "dash"
        }.joinToString(" ")
}

/** Builds the chart from the alphabet, in the alphabet's order. */
fun MorseAlphabet.referenceEntries(): List<ReferenceEntry> = mappings.map { mapping ->
    ReferenceEntry(
        character = mapping.character,
        code = mapping.code.code,
        category = mapping.category,
        name = punctuationNames[mapping.character],
    )
}

/** Display names only; Morse codes live exclusively in [MorseAlphabet]. */
internal val punctuationNames: Map<Char, String> = mapOf(
    '.' to "Period (full stop)",
    ',' to "Comma",
    '?' to "Question mark",
    '\'' to "Apostrophe",
    '!' to "Exclamation mark",
    '/' to "Slash",
    '(' to "Open parenthesis",
    ')' to "Close parenthesis",
    '&' to "Ampersand",
    ':' to "Colon",
    ';' to "Semicolon",
    '=' to "Equals sign",
    '+' to "Plus sign",
    '-' to "Hyphen (minus)",
    '_' to "Underscore",
    '"' to "Quotation mark",
    '$' to "Dollar sign",
    '@' to "At sign",
)
