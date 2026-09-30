package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.morse.MorseNormalizer

/**
 * Filters the chart for a search query, keeping the original order. An entry matches if:
 *
 * - the query is that single character (`a`, `7`, `?`), case-insensitively, or a prosign's
 *   letters (`sos`);
 * - the query is made of dots and dashes and the entry's code starts with it (`.-` → A, J, L, P, R, W, ...).
 *   Typographic dots and dashes (`·−`) are accepted;
 * - the query has two or more characters and is part of the entry's name or meaning
 *   (`comma`, `quest`, `distress`).
 *
 * A blank query matches everything.
 */
internal fun filterReference(entries: List<ReferenceEntry>, query: String): List<ReferenceEntry> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return entries

    val character = trimmed.singleOrNull()?.let(MorseNormalizer::normalizeTextChar)
    val codePrefix = trimmed.map(MorseNormalizer::normalizeMorseChar).joinToString("")
        .takeIf { prefix -> prefix.all { it == MorseNotation.DOT || it == MorseNotation.DASH } }
    val namePart = trimmed.takeIf { it.length > 1 }

    return entries.filter { entry ->
        (character != null && entry.symbol == character.toString()) ||
            (entry.isProsign && entry.symbol.equals(trimmed, ignoreCase = true)) ||
            (codePrefix != null && entry.code.startsWith(codePrefix)) ||
            (namePart != null && entry.name?.contains(namePart, ignoreCase = true) == true)
    }
}
