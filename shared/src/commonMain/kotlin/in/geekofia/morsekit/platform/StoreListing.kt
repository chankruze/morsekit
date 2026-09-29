package `in`.geekofia.morsekit.platform

/** Where MorseKit is listed. One place for the share text, the rating button and the prompts. */
object StoreListing {
    /** The release application ID (debug builds add `.debug`, which has no store listing). */
    const val PLAY_PACKAGE = "in.geekofia.morsekit"

    /** Resolves once the app is published on Google Play. */
    const val PLAY_URL = "https://play.google.com/store/apps/details?id=$PLAY_PACKAGE"

    /** Numeric App Store ID, known once the app is created in App Store Connect. */
    val APP_STORE_ID: String? = null
}
