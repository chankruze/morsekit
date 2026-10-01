package `in`.geekofia.morsekit.core.history

import `in`.geekofia.morsekit.core.model.TranslationDirection

/** A translation that was used (copied, shared or sent), kept so it can be found again. */
data class HistoryEntry(
    val id: Long,
    val direction: TranslationDirection,
    /** What was typed, as typed. */
    val input: String,
    /** What it translated to. */
    val output: String,
    /** When it was last used, in epoch milliseconds. */
    val savedAt: Long,
    /** Favourites are kept whatever the history's size, and survive Clear history. */
    val favorite: Boolean = false,
)
