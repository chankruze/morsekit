package `in`.geekofia.morsekit.core.trainer

/**
 * The Koch method's order for learning Morse, in the 41-character version used by LCWO
 * (lcwo.net): start with two characters at full speed and add one at a time. Early characters
 * have distinct rhythms, so they're easy to tell apart by ear before similar ones arrive.
 */
object KochOrder {
    val characters: List<Char> = "KMURESNAPTLWI.JZ=FOY,VG5/Q92H38B?47C1D60X".toList()

    /** Learners start with K and M. */
    const val START_LEVEL = 2
}
