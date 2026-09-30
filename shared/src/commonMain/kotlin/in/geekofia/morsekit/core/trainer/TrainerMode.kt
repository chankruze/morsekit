package `in`.geekofia.morsekit.core.trainer

import `in`.geekofia.morsekit.core.morse.MorseAlphabet

/** What the learner does with each question. Both feed the same [KochProgression]. */
enum class TrainerMode {
    /** Hear (and optionally see) the Morse, pick the character. */
    Listen,

    /** See the character, key its Morse (with the Tap key or buttons). */
    Key,
}

/** Whether [keyedCode] (dots and dashes, e.g. `-.-`) is [target]'s Morse. */
fun keyedCorrectly(target: Char, keyedCode: String, alphabet: MorseAlphabet = MorseAlphabet.International): Boolean =
    alphabet.codeFor(target)?.code == keyedCode
