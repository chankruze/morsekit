package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCategory
import `in`.geekofia.morsekit.core.morse.MorseProsign

/**
 * One cell of the reference chart, from a [MorseAlphabet] mapping or a [MorseProsign]; this class
 * only adds presentation details.
 */
data class ReferenceEntry(
    /** The character (`A`, `?`), or a prosign's letters (`SOS`). */
    val symbol: String,
    val code: String,
    val category: MorseCategory,
    /** Punctuation's name or a prosign's meaning (searchable), `null` for letters and digits. */
    val name: String?,
) {
    val isProsign: Boolean get() = category == MorseCategory.Prosign

    /**
     * What a screen reader announces, e.g. "A, dot dash", "Comma, dash dash dot dot dash dash" or
     * "Prosign S O S, Distress signal, dot dot dot …". Prosign letters are spaced so they're
     * spelled out, not read as a word.
     */
    val accessibilityLabel: String
        get() {
            val elements = MorseNotation.toSpokenForm(code)
            return if (isProsign) {
                "Prosign ${symbol.toList().joinToString(" ")}, $name, $elements"
            } else {
                "${name ?: symbol}, $elements"
            }
        }
}

/** Builds the chart from the alphabet, in the alphabet's order. */
fun MorseAlphabet.referenceEntries(): List<ReferenceEntry> = mappings.map { mapping ->
    ReferenceEntry(
        symbol = mapping.character.toString(),
        code = mapping.code.code,
        category = mapping.category,
        name = punctuationNames[mapping.character],
    )
}

fun List<MorseProsign>.referenceEntries(): List<ReferenceEntry> = map { prosign ->
    ReferenceEntry(
        symbol = prosign.letters,
        code = prosign.code.code,
        category = MorseCategory.Prosign,
        name = prosign.meaning,
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
