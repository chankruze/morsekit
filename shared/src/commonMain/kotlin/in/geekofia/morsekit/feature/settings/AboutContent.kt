package `in`.geekofia.morsekit.feature.settings

// Static content for the About section.

// Settings can also reach the user's own device backup (android:allowBackup, iCloud on iOS), so
// the notice says so; the web privacy policy says the same.
internal const val PRIVACY_NOTICE =
    "MorseKit works entirely on your device. It has no accounts, ads or analytics, and doesn't use " +
        "the network. What you type stays on your device unless you copy or share it; with Save " +
        "history on, translations you copy, share or send are kept in History, on this device only " +
        "and never backed up. Settings are stored on this device, and in your device's backup if " +
        "you've turned backup on."

internal const val PRIVACY_POLICY_URL = "https://morsekit.geekofia.in/privacy/"

internal data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
)

/** Third-party code shipped in the app, grouped by project. Keep in sync with the dependencies. */
internal val openSourceLibraries = listOf(
    OpenSourceLibrary("Kotlin, kotlinx.coroutines & JetBrains annotations", "JetBrains", "Apache License 2.0"),
    OpenSourceLibrary("Compose Multiplatform", "JetBrains", "Apache License 2.0"),
    OpenSourceLibrary("Jetpack Compose & AndroidX", "Google", "Apache License 2.0"),
    OpenSourceLibrary("Guava ListenableFuture (Android)", "Google", "Apache License 2.0"),
    OpenSourceLibrary("JSpecify annotations (Android)", "JSpecify", "Apache License 2.0"),
    OpenSourceLibrary("Skiko (iOS)", "JetBrains", "Apache License 2.0"),
    OpenSourceLibrary("Skia, via Skiko (iOS)", "Google", "BSD 3-Clause License"),
    OpenSourceLibrary("Space Grotesk (font)", "The Space Grotesk Project Authors", "SIL Open Font License 1.1"),
)

internal const val DEVELOPER_NAME = "chankruze"
internal const val DEVELOPER_URL = "https://github.com/chankruze"
internal const val ORGANIZATION_NAME = "geekofia"
internal const val ORGANIZATION_URL = "https://geekofia.in"
