package `in`.geekofia.morsekit.core.tap

import `in`.geekofia.morsekit.core.morse.MorseCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** At 8 WPM: 150 ms units, so dash from 300 ms, letter after 450 ms, word after 1050 ms. */
class TapDecoderTest {
    private val timing = TapTiming(8)
    private val decoder = TapDecoder(timing)

    private val dot = 100L
    private val dash = 400L
    private val elementGap = 150L

    /** Keys [elements] ("." / "-") starting at [start]; returns the state and the time after. */
    private fun key(state: TapState, start: Long, elements: String): Pair<TapState, Long> {
        var s = state
        var t = start
        elements.forEachIndexed { i, element ->
            if (i > 0) t += elementGap
            s = decoder.press(s, t)
            t += if (element == '.') dot else dash
            s = decoder.release(s, t)
        }
        return s to t
    }

    @Test
    fun thresholdsFollowTheTapSpeed() {
        assertEquals(300, timing.dashThresholdMillis)
        assertEquals(450, timing.letterGapMillis)
        assertEquals(1050, timing.wordGapMillis)
        assertEquals(TapTiming.DEFAULT_WPM, TapTiming().wordsPerMinute)
        assertEquals(TapTiming(16).dashThresholdMillis * 2, TapTiming(8).dashThresholdMillis)
    }

    @Test
    fun shortPressIsADotAndLongPressADash() {
        val (s, _) = key(TapState(), 0, ".-")
        assertEquals(".-", s.currentLetter)
        assertEquals("", s.text)
    }

    @Test
    fun theDashThresholdItselfCountsAsADash() {
        var s = decoder.press(TapState(), 0)
        s = decoder.release(s, 300)
        assertEquals("-", s.currentLetter)
        s = decoder.press(s, 400)
        s = decoder.release(s, 699)
        assertEquals("-.", s.currentLetter)
    }

    @Test
    fun isDashTurnsOnWhileHeldPastTheThreshold() {
        val s = decoder.press(TapState(), 1_000)
        assertFalse(decoder.isDash(s, 1_299))
        assertTrue(decoder.isDash(s, 1_300))
        assertFalse(decoder.isDash(decoder.release(s, 1_300), 2_000))
    }

    @Test
    fun aPauseEndsTheLetterAndALongerOneTheWord() {
        val (keyed, end) = key(TapState(), 0, "...")
        assertEquals("", decoder.advance(keyed, end + 449).text)
        val letter = decoder.advance(keyed, end + 450)
        assertEquals("S", letter.text)
        assertEquals("", letter.currentLetter)
        assertEquals("S", decoder.advance(letter, end + 1_049).text)
        assertEquals("S ", decoder.advance(letter, end + 1_050).text)
    }

    @Test
    fun aLateAdvanceStillEndsTheLetterThenTheWord() {
        val (keyed, end) = key(TapState(), 0, "...")
        assertEquals("S ", decoder.advance(keyed, end + 5_000).text)
    }

    @Test
    fun pressingAfterAPauseEndsTheLetterFirst() {
        // No advance() between letters: the next press notices the gap itself.
        val (s1, end1) = key(TapState(), 0, "....")
        val (s2, end2) = key(s1, end1 + 500, ".")
        assertEquals("H", s2.text)
        assertEquals(".", s2.currentLetter)
        assertEquals("HE", decoder.advance(s2, end2 + 450).text)
    }

    @Test
    fun keysAWholeSentenceWithMorse() {
        var s = TapState()
        var t = 0L
        for ((word, i) in listOf(listOf("....", ".."), listOf(".--", "---")).withIndex().map { it.value to it.index }) {
            if (i > 0) t += 1_100 // word gap
            word.forEachIndexed { j, letter ->
                if (j > 0) t += 500 // letter gap
                val (next, end) = key(s, t, letter)
                s = next
                t = end
            }
        }
        s = decoder.advance(s, t + 450)
        assertEquals("HI WO", s.text)
        assertEquals(".... .. / .-- ---", s.morse)
    }

    @Test
    fun unknownCodesBecomeTheReplacementCharacter() {
        val (keyed, end) = key(TapState(), 0, "......")
        val s = decoder.advance(keyed, end + 450)
        assertEquals(MorseCodec.REPLACEMENT_CHAR.toString(), s.text)
        assertEquals("......", s.morse)
    }

    @Test
    fun nextDeadlineIsTheNextThingThatCanChange() {
        assertNull(decoder.nextDeadline(TapState()))
        val held = decoder.press(TapState(), 1_000)
        assertEquals(1_300, decoder.nextDeadline(held))
        val released = decoder.release(held, 1_100)
        assertEquals(1_550, decoder.nextDeadline(released))
        val letter = decoder.advance(released, 1_550)
        assertEquals(2_150, decoder.nextDeadline(letter))
        assertNull(decoder.nextDeadline(decoder.advance(letter, 2_150)))
    }

    @Test
    fun noSpaceAtTheStartAndOnlyOneBetweenWords() {
        assertEquals("", decoder.space(TapState()).text)
        val (keyed, end) = key(TapState(), 0, ".")
        val s = decoder.advance(decoder.advance(keyed, end + 2_000), end + 9_000)
        assertEquals("E ", s.text)
        assertEquals("E ", decoder.space(s).text)
    }

    @Test
    fun manualEndLetterAndSpaceWorkWithoutTiming() {
        var s = decoder.release(decoder.press(TapState(), 0), 100)
        s = decoder.endLetter(s)
        assertEquals("E", s.text)
        s = decoder.space(decoder.release(decoder.press(s, 5_000), 5_100))
        assertEquals("EE ", s.text)
        assertEquals(". .", s.morse)
    }

    @Test
    fun manualEditsCancelPendingBreaks() {
        val (keyed, end) = key(TapState(), 0, ".")
        val spaced = decoder.advance(keyed, end + 2_000)
        // Removing the automatic space must not bring it straight back.
        val edited = decoder.backspace(spaced)
        assertEquals("E", edited.text)
        assertNull(decoder.nextDeadline(edited))
        assertEquals("E", decoder.advance(edited, end + 10_000).text)
    }

    @Test
    fun backspaceRemovesElementsThenLettersAndSpaces() {
        var s = decoder.space(decoder.endLetter(key(TapState(), 0, "....").first))
        s = decoder.endLetter(key(s, 10_000, ".--").first)
        s = key(s, 20_000, "-.").first
        assertEquals("H W", s.text)
        assertEquals("-.", s.currentLetter)

        s = decoder.backspace(s)
        assertEquals("-", s.currentLetter)
        s = decoder.backspace(decoder.backspace(s))
        assertEquals("H ", s.text)
        assertEquals("....", s.morse)
        s = decoder.backspace(s)
        assertEquals("H", s.text)
        s = decoder.backspace(s)
        assertTrue(s.isEmpty)
        assertEquals("", s.morse)
        assertEquals(s, decoder.backspace(s))
    }

    @Test
    fun letterTypedAfterBackspacingIntoAWordRejoinsIt() {
        var s = decoder.space(decoder.endLetter(key(TapState(), 0, "....").first))
        s = decoder.backspace(s) // "H " → "H"
        s = decoder.endLetter(key(s, 10_000, "..").first)
        assertEquals("HI", s.text)
        assertEquals(".... ..", s.morse)
    }

    @Test
    fun clearEmptiesEverythingButKeepsAHeldKey() {
        val keyed = decoder.endLetter(key(TapState(), 0, "...").first)
        assertTrue(decoder.clear(keyed).isEmpty)
        val held = decoder.press(keyed, 5_000)
        assertTrue(decoder.clear(held).isPressed)
    }

    @Test
    fun pressWhilePressedAndReleaseWhileUpAreIgnored() {
        val held = decoder.press(TapState(), 0)
        assertEquals(held, decoder.press(held, 50))
        val up = TapState()
        assertEquals(up, decoder.release(up, 100))
    }

    @Test
    fun elementAddsDotsAndDashesWithoutStartingTimers() {
        var s = decoder.element(TapState(), dash = false)
        s = decoder.element(s, dash = true)
        assertEquals(".-", s.currentLetter)
        assertNull(decoder.nextDeadline(s))
        assertEquals("A", decoder.endLetter(s).text)
    }

    @Test
    fun stopTimersKeepsTheTextButCancelsBreaksAndAHeldKey() {
        val (keyed, end) = key(TapState(), 0, ".-")
        val held = decoder.press(keyed, end + 100) // within the letter gap: still the same letter
        val stopped = decoder.stopTimers(held)
        assertEquals(".-", stopped.currentLetter)
        assertFalse(stopped.isPressed)
        assertNull(decoder.nextDeadline(stopped))
    }

    @Test
    fun previewShowsWhatTheLetterDecodesToSoFar() {
        assertNull(decoder.preview(TapState()))
        assertEquals('E', decoder.preview(decoder.element(TapState(), dash = false)))
        val a = decoder.element(decoder.element(TapState(), dash = false), dash = true)
        assertEquals('A', decoder.preview(a))
        assertNull(decoder.preview(TapState(currentLetter = "........-")))
    }
}
