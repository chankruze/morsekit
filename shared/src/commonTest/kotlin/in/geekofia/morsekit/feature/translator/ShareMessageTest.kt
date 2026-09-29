package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.MorseCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareMessageTest {
    private val viewModel = TranslatorViewModel()

    private fun textToMorse(text: String): TranslatorUiState {
        viewModel.onInputChange(text)
        return viewModel.uiState
    }

    private fun morseToText(morse: String): TranslatorUiState {
        viewModel.onDirectionSelected(TranslationDirection.MorseToText)
        viewModel.onInputChange(morse)
        return viewModel.uiState
    }

    @Test
    fun textToMorseIsAPuzzle() {
        assertEquals(
            """
            🕵️ Can you decode this secret message?

            ••• −−− •••

            (... --- ... in plain dots and dashes)

            Stuck? Decode it, and learn Morse, with MorseKit 📡
            Install it now 👉 $MORSEKIT_STORE_URL
            """.trimIndent(),
            shareMessage(textToMorse("sos")),
        )
    }

    @Test
    fun morseToTextIsTheReveal() {
        assertEquals(
            """
            📡 I decoded a Morse message with MorseKit:

            "SOS"

            ... --- ...

            Decode your own, and learn Morse, with MorseKit.
            Install it now 👉 $MORSEKIT_STORE_URL
            """.trimIndent(),
            shareMessage(morseToText("... --- ...")),
        )
    }

    @Test
    fun revealShowsCanonicalMorseEvenIfTypedDifferently() {
        val message = shareMessage(morseToText("···   −−−|···"))!!
        assertTrue("... / --- / ..." in message, message)
    }

    @Test
    fun bothMorseLinesDecodeBackToTheMessage() {
        val codec = MorseCodec()
        val message = shareMessage(textToMorse("Hello, World!"))!!
        val lines = message.lines()
        val glyphs = lines[2]
        val plain = lines[4].removePrefix("(").removeSuffix(" in plain dots and dashes)")
        assertEquals("HELLO, WORLD!", codec.decode(glyphs).text)
        assertEquals("HELLO, WORLD!", codec.decode(plain).text)
        assertTrue(glyphs.none { it == MorseNotation.DOT || it == MorseNotation.DASH })
    }

    @Test
    fun nothingToShareGivesNull() {
        assertNull(shareMessage(TranslatorUiState()))
        assertNull(shareMessage(textToMorse("###")))
        assertNull(shareMessage(morseToText("-x-")))
    }

    @Test
    fun bothMessagesEndWithTheInstallCallToAction() {
        val puzzle = shareMessage(textToMorse("hi"))!!
        val reveal = shareMessage(morseToText(".... .."))!!
        listOf(puzzle, reveal).forEach { message ->
            assertEquals("Install it now 👉 $MORSEKIT_STORE_URL", message.lines().last())
        }
    }

    @Test
    fun linkIsThePlayStoreListing() {
        assertEquals("https://play.google.com/store/apps/details?id=in.geekofia.morsekit", MORSEKIT_STORE_URL)
    }
}
