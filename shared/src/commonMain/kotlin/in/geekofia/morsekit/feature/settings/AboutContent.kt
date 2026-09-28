package `in`.geekofia.morsekit.feature.settings

// Static content for the About section.

internal const val PRIVACY_NOTICE =
    "MorseKit works entirely on your device. It has no accounts, ads or analytics, and doesn't use " +
        "the network. What you type stays on your device unless you copy or share it, and settings " +
        "are stored only on this device."

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
)
