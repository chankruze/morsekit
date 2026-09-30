package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseLetter

/**
 * A procedural sign: [letters] sent as one character, with no gaps between them (written with a
 * bar over the letters, e.g. S̅O̅S̅). Some share a code with punctuation: AR is `+`, BT is `=`.
 */
data class MorseProsign(
    val letters: String,
    val code: MorseLetter,
    val meaning: String,
)

object MorseProsigns {
    /** The common ITU and amateur-radio prosigns. */
    val Common: List<MorseProsign> = listOf(
        prosign("SOS", "Distress signal"),
        prosign("AR", "End of message"),
        prosign("SK", "End of contact"),
        prosign("BT", "Break, new paragraph"),
        prosign("KN", "Go ahead, named station only"),
        prosign("AS", "Wait"),
        prosign("CT", "Start of message"),
        prosign("VE", "Understood"),
        prosign("HH", "Error, correction follows"),
        prosign("CL", "Closing down"),
    )

    /** The code is the letters' codes run together, so it can never disagree with the alphabet. */
    private fun prosign(letters: String, meaning: String): MorseProsign = MorseProsign(
        letters = letters,
        code = MorseLetter(
            letters.map { requireNotNull(MorseAlphabet.International.codeFor(it)) { "No code for '$it'" }.code }
                .joinToString(""),
        ),
        meaning = meaning,
    )
}
