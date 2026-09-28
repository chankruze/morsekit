package `in`.geekofia.morsekit

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.geekofia.morsekit.feature.reference.ReferenceRoute
import `in`.geekofia.morsekit.feature.settings.SettingsRoute
import `in`.geekofia.morsekit.feature.translator.TranslatorRoute
import `in`.geekofia.morsekit.navigation.TopLevelDestination
import `in`.geekofia.morsekit.ui.theme.MorseKitTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Shared entry point, hosted by `MainActivity` on Android and `MainViewController` on iOS.
 *
 * [onSystemBarAppearanceChange] reports which icon colours the system bars need over the app's
 * header and bottom bar, so the host can style them (Android: `enableEdgeToEdge`).
 */
@Composable
fun App(
    container: AppContainer,
    onSystemBarAppearanceChange: (SystemBarAppearance) -> Unit = {},
) {
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle()
    val currentOnAppearanceChange by rememberUpdatedState(onSystemBarAppearanceChange)

    MorseKitTheme(themeMode = settings.themeMode) {
        val colors = MaterialTheme.colorScheme
        // Status bar icons sit on the (surface-coloured) header, navigation bar icons on the bottom bar.
        val appearance = SystemBarAppearance(
            lightStatusBarIcons = colors.surface.luminance() < 0.5f,
            lightNavigationBarIcons = colors.surfaceContainer.luminance() < 0.5f,
        )
        LaunchedEffect(appearance) { currentOnAppearanceChange(appearance) }

        var destination by rememberSaveable { mutableStateOf(TopLevelDestination.Translator) }

        // Each screen draws its own top bar (ScreenScaffold), so this Scaffold only owns the bottom
        // bar and applies no insets itself: NavigationBar pads for the system navigation bar.
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                NavigationBar {
                    TopLevelDestination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = item == destination,
                            onClick = { destination = item },
                            icon = { Icon(painterResource(item.icon), contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
            },
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            when (destination) {
                TopLevelDestination.Translator -> TranslatorRoute(
                    platformServices = container.platformServices,
                    settingsRepository = container.settingsRepository,
                    modifier = contentModifier,
                )
                TopLevelDestination.Reference -> ReferenceRoute(contentModifier)
                TopLevelDestination.Settings -> SettingsRoute(
                    settingsRepository = container.settingsRepository,
                    appInfo = container.platformServices.appInfo,
                    modifier = contentModifier,
                )
            }
        }
    }
}
