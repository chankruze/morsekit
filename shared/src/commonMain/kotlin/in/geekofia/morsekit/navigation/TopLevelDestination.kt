package `in`.geekofia.morsekit.navigation

import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_reference
import morsekit.shared.generated.resources.ic_settings
import morsekit.shared.generated.resources.ic_tap
import morsekit.shared.generated.resources.ic_translate
import org.jetbrains.compose.resources.DrawableResource

/**
 * Destinations shown in the bottom navigation bar.
 *
 * Future top-level features (Trainer, History) are added as new entries here plus a branch in
 * `App`'s destination `when`.
 */
enum class TopLevelDestination(val label: String, val icon: DrawableResource) {
    Translator("Translator", Res.drawable.ic_translate),
    Tap("Tap", Res.drawable.ic_tap),
    Reference("Reference", Res.drawable.ic_reference),
    Settings("Settings", Res.drawable.ic_settings),
}
