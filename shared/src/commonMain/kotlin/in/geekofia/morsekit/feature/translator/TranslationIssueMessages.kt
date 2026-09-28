package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.morse.TranslationIssue

private const val MAX_LISTED = 5

private val invisibleCategories = setOf(
    CharCategory.CONTROL,
    CharCategory.FORMAT,
    CharCategory.NON_SPACING_MARK,
    CharCategory.ENCLOSING_MARK,
)

/**
 * User-facing summaries of translation issues, one line per kind of problem.
 *
 * Kept out of the composable so it's unit-testable.
 */
internal fun issueMessages(issues: List<TranslationIssue>): List<String> {
    val unsupported = issues.filterIsInstance<TranslationIssue.UnsupportedCharacter>()
        .map { it.character.displayable() }
    val unknown = issues.filterIsInstance<TranslationIssue.UnknownCode>().map { it.code }
    val malformed = issues.filterIsInstance<TranslationIssue.MalformedCode>().map { it.token }
    return buildList {
        if (unsupported.isNotEmpty()) add("No Morse code for ${unsupported.quotedList()} (skipped)")
        if (unknown.isNotEmpty()) add("Unknown Morse code: ${unknown.quotedList()}")
        if (malformed.isNotEmpty()) add("Use only dots and dashes: ${malformed.quotedList()}")
    }
}

private fun List<String>.quotedList(): String {
    val listed = take(MAX_LISTED).joinToString(", ") { "“$it”" }
    return if (size > MAX_LISTED) "$listed and ${size - MAX_LISTED} more" else listed
}

/** Invisible characters (e.g. an emoji's variation selector) are shown as `U+FE0F`. */
private fun String.displayable(): String {
    val char = singleOrNull() ?: return this
    return if (char.category in invisibleCategories) {
        "U+" + char.code.toString(16).uppercase().padStart(4, '0')
    } else {
        this
    }
}
