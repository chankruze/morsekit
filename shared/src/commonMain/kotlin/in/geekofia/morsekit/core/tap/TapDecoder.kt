package `in`.geekofia.morsekit.core.tap

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.morse.MorseCodec

/** What has been keyed so far. Immutable, so the UI can hold it as state. */
data class TapState(
    /** Decoded text, e.g. `HELLO WO`. Unknown letters are [MorseCodec.REPLACEMENT_CHAR]. */
    val text: String = "",
    /** Canonical Morse of [text], e.g. `.... . .-.. .-.. --- / .-- ---`. */
    val morse: String = "",
    /** Dots and dashes of the letter still being keyed, e.g. `.-.`. */
    val currentLetter: String = "",
    /** When the key went down (clock millis), or `null` if it's up. */
    val pressedAt: Long? = null,
    /** When the key last came up; a pause is measured from here. */
    val releasedAt: Long? = null,
) {
    val isPressed: Boolean get() = pressedAt != null
    val isEmpty: Boolean get() = text.isEmpty() && currentLetter.isEmpty()
}

/**
 * Turns key presses into Morse and text, like a straight key: how long the key is held decides
 * dot or dash, and how long it stays up decides where letters and words end ([TapTiming]).
 *
 * Pure and synchronous: callers pass the time (`now`, in milliseconds from any monotonic clock),
 * so tests use fake time. Nothing happens by itself when the key is idle; the caller asks
 * [nextDeadline] when to call [advance] next.
 */
class TapDecoder(
    private val timing: TapTiming,
    private val codec: MorseCodec = MorseCodec(),
) {
    fun press(state: TapState, now: Long): TapState {
        if (state.isPressed) return state
        return advance(state, now).copy(pressedAt = now)
    }

    fun release(state: TapState, now: Long): TapState {
        val pressedAt = state.pressedAt ?: return state
        val element = if (now - pressedAt >= timing.dashThresholdMillis) MorseNotation.DASH else MorseNotation.DOT
        return state.copy(currentLetter = state.currentLetter + element, pressedAt = null, releasedAt = now)
    }

    /** The key is held long enough that releasing it now gives a dash. */
    fun isDash(state: TapState, now: Long): Boolean =
        state.pressedAt != null && now - state.pressedAt >= timing.dashThresholdMillis

    /** Ends the letter and then the word once their pauses have passed. Call at [nextDeadline]. */
    fun advance(state: TapState, now: Long): TapState {
        if (state.isPressed) return state
        val idle = now - (state.releasedAt ?: return state)
        var next = state
        if (next.currentLetter.isNotEmpty() && idle >= timing.letterGapMillis) next = commitLetter(next)
        if (idle >= timing.wordGapMillis) next = addSpace(next)
        return next
    }

    /**
     * The next time something can change (a hold becoming a dash, a letter or word ending), or
     * `null` if nothing is pending.
     */
    fun nextDeadline(state: TapState): Long? {
        state.pressedAt?.let { return it + timing.dashThresholdMillis }
        val releasedAt = state.releasedAt ?: return null
        return when {
            state.currentLetter.isNotEmpty() -> releasedAt + timing.letterGapMillis
            state.text.isNotEmpty() && !state.text.endsWith(' ') -> releasedAt + timing.wordGapMillis
            else -> null
        }
    }

    /**
     * Manual edits: also screen-reader actions, where timing can't be used. Each one cancels the
     * pending automatic breaks (`releasedAt = null`) until the next key press; otherwise, say,
     * a space removed with [backspace] would come straight back on the next [advance].
     */
    fun endLetter(state: TapState): TapState = commitLetter(state).copy(releasedAt = null)

    /** Drops any pending automatic break (e.g. when switching to [TapMode.Buttons]); keeps the text. */
    fun stopTimers(state: TapState): TapState = state.copy(pressedAt = null, releasedAt = null)

    /** What the letter being keyed decodes to so far (`.-` → `A`), or `null` if nothing matches yet. */
    fun preview(state: TapState): Char? =
        state.currentLetter.takeIf { it.isNotEmpty() }?.let(codec::decodeSymbol)

    /** Adds a dot or dash without timing ([TapMode.Buttons], screen-reader actions). */
    fun element(state: TapState, dash: Boolean): TapState = state.copy(
        currentLetter = state.currentLetter + if (dash) MorseNotation.DASH else MorseNotation.DOT,
        releasedAt = null,
    )

    fun space(state: TapState): TapState = addSpace(state).copy(releasedAt = null)

    /** Removes the last element of the letter being keyed, else the last character (or space). */
    fun backspace(state: TapState): TapState = when {
        state.currentLetter.isNotEmpty() -> state.copy(currentLetter = state.currentLetter.dropLast(1))
        state.text.endsWith(' ') -> state.copy(text = state.text.dropLast(1))
        state.text.isNotEmpty() -> state.copy(text = state.text.dropLast(1), morse = state.morse.dropLastLetter())
        else -> state
    }.copy(releasedAt = null)

    fun clear(state: TapState): TapState = TapState(pressedAt = state.pressedAt)

    private fun commitLetter(state: TapState): TapState {
        if (state.currentLetter.isEmpty()) return state
        val char = codec.decodeSymbol(state.currentLetter) ?: MorseCodec.REPLACEMENT_CHAR
        val morse = when {
            state.morse.isEmpty() -> state.currentLetter
            state.text.endsWith(' ') -> state.morse + MorseNotation.WORD_SEPARATOR + state.currentLetter
            else -> state.morse + MorseNotation.LETTER_SEPARATOR + state.currentLetter
        }
        return state.copy(text = state.text + char, morse = morse, currentLetter = "")
    }

    /** Ends the letter, then the word: one space at most, and never at the start. */
    private fun addSpace(state: TapState): TapState {
        val ended = commitLetter(state)
        if (ended.text.isEmpty() || ended.text.endsWith(' ')) return ended
        return ended.copy(text = ended.text + ' ')
    }

    /** `.... . / .--` → `.... .`: the last letter, and the word separator if it began a word. */
    private fun String.dropLastLetter(): String {
        val cut = lastIndexOf(MorseNotation.LETTER_SEPARATOR)
        return if (cut < 0) "" else substring(0, cut).removeSuffix(MorseNotation.WORD_SEPARATOR.trimEnd())
    }
}
