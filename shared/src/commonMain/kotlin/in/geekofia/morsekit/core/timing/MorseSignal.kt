package `in`.geekofia.morsekit.core.timing

import `in`.geekofia.morsekit.core.model.MorseElement
import `in`.geekofia.morsekit.core.model.MorseMessage

/** One on/off segment of a Morse transmission, measured in timing units. */
data class MorseSignal(val isOn: Boolean, val units: Int) {
    init {
        require(units > 0) { "A signal must last at least one unit" }
    }

    companion object {
        const val DOT_UNITS = 1
        const val DASH_UNITS = 3
        const val ELEMENT_GAP_UNITS = 1
        const val LETTER_GAP_UNITS = 3
        const val WORD_GAP_UNITS = 7
    }
}

/**
 * Flattens a message into alternating on/off signals, starting and ending with an "on" signal.
 * Output-agnostic: the same sequence drives the torch, haptics or audio.
 */
fun MorseMessage.toSignals(): List<MorseSignal> = buildList {
    words.forEachIndexed { wordIndex, word ->
        if (wordIndex > 0) add(MorseSignal(isOn = false, units = MorseSignal.WORD_GAP_UNITS))
        word.letters.forEachIndexed { letterIndex, letter ->
            if (letterIndex > 0) add(MorseSignal(isOn = false, units = MorseSignal.LETTER_GAP_UNITS))
            letter.elements.forEachIndexed { elementIndex, element ->
                if (elementIndex > 0) add(MorseSignal(isOn = false, units = MorseSignal.ELEMENT_GAP_UNITS))
                val units = when (element) {
                    MorseElement.Dot -> MorseSignal.DOT_UNITS
                    MorseElement.Dash -> MorseSignal.DASH_UNITS
                }
                add(MorseSignal(isOn = true, units = units))
            }
        }
    }
}
