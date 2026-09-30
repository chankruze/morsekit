package `in`.geekofia.morsekit.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseNotationTest {
    @Test
    fun displayGlyphsReplaceDotsAndDashes() {
        assertEquals("••• −−− ••• / •−", MorseNotation.toDisplayGlyphs("... --- ... / .-"))
    }

    @Test
    fun spokenFormPausesBetweenLetters() {
        assertEquals("dot dot dot, dash dash dash, dot dot dot", MorseNotation.toSpokenForm("... --- ..."))
    }

    @Test
    fun spokenFormSaysSpaceBetweenWords() {
        assertEquals("dot dash, space, dash dot dot dot", MorseNotation.toSpokenForm(".- / -..."))
    }

    @Test
    fun spokenFormOfOneLetterIsJustItsElements() {
        assertEquals("dot dash", MorseNotation.toSpokenForm(".-"))
    }

    @Test
    fun spokenFormOfEmptyOrUnexpectedInput() {
        assertEquals("", MorseNotation.toSpokenForm(""))
        assertEquals("dot x", MorseNotation.toSpokenForm(".x"))
    }
}
