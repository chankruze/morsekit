package `in`.geekofia.morsekit

import `in`.geekofia.morsekit.core.review.ReviewPrompter
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.trainer.TrainerRepository
import `in`.geekofia.morsekit.core.update.UpdateController
import `in`.geekofia.morsekit.core.update.UpdatePrompter
import `in`.geekofia.morsekit.platform.PlatformServices

/**
 * App-scoped objects, created once per process by the platform entry point
 * (`MorseKitApplication` on Android, `MainViewController` on iOS) and passed to [App].
 *
 * Anything that must be shared across screens and survive Android configuration changes
 * (e.g. settings) lives here. This is manual dependency injection, not a service locator.
 */
class AppContainer(val platformServices: PlatformServices) {
    val settingsRepository = SettingsRepository(platformServices.keyValueStore)
    val trainerRepository = TrainerRepository(platformServices.keyValueStore)

    val reviewPrompter = ReviewPrompter(platformServices.keyValueStore, nowMillis = ::currentTimeMillis).also {
        it.recordAppStart()
    }

    val updateController = UpdateController(
        service = platformServices.appUpdates,
        prompter = UpdatePrompter(platformServices.keyValueStore, nowMillis = ::currentTimeMillis),
    )
}

/** Wall-clock time in epoch milliseconds. */
private fun currentTimeMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
