package `in`.geekofia.morsekit.core.model

enum class TranslationDirection {
    TextToMorse,
    MorseToText;

    val reversed: TranslationDirection
        get() = when (this) {
            TextToMorse -> MorseToText
            MorseToText -> TextToMorse
        }
}
