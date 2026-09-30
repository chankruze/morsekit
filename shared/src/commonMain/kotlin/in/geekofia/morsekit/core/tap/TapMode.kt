package `in`.geekofia.morsekit.core.tap

/** How the Tap screen turns input into Morse. */
enum class TapMode {
    /** One key: hold time gives dot or dash, pauses end letters and words ([TapTiming]). */
    Timing,

    /** Dot, Dash, Next letter and Space buttons: no timing at all. */
    Buttons,
}
