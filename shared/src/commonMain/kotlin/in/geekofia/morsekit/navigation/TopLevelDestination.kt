package `in`.geekofia.morsekit.navigation

import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_learn
import morsekit.shared.generated.resources.ic_reference
import morsekit.shared.generated.resources.ic_settings
import morsekit.shared.generated.resources.ic_tap
import morsekit.shared.generated.resources.ic_translate
import org.jetbrains.compose.resources.DrawableResource

/**
 * Destinations shown in the bottom navigation bar.
 *
 * Five is Material's maximum for a bottom bar, so a future feature (e.g. History) goes inside an
 * existing tab rather than a sixth. Each entry has a branch in `App`'s destination `when`.
 */
enum class TopLevelDestination(val label: String, val icon: DrawableResource) {
    Translator("Translator", Res.drawable.ic_translate),
    Tap("Tap", Res.drawable.ic_tap),
    Learn("Learn", Res.drawable.ic_learn),
    Reference("Reference", Res.drawable.ic_reference),
    Settings("Settings", Res.drawable.ic_settings),
}
