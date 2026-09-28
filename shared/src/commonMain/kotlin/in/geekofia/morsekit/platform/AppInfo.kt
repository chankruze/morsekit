package `in`.geekofia.morsekit.platform

/** Version details read from the installed app (Android package info / iOS Info.plist). */
data class AppInfo(
    val versionName: String,
    val buildNumber: String,
)
