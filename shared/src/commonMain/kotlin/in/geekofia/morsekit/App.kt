package `in`.geekofia.morsekit

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import `in`.geekofia.morsekit.feature.reference.ReferenceRoute
import `in`.geekofia.morsekit.feature.settings.SettingsScreen
import `in`.geekofia.morsekit.feature.translator.TranslatorRoute
import `in`.geekofia.morsekit.navigation.TopLevelDestination
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.theme.MorseKitTheme
import org.jetbrains.compose.resources.painterResource

/** Shared entry point, hosted by `MainActivity` on Android and `MainViewController` on iOS. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(platformServices: PlatformServices) {
    MorseKitTheme {
        var destination by rememberSaveable { mutableStateOf(TopLevelDestination.Translator) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(destination.label) })
            },
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
                TopLevelDestination.Translator -> TranslatorRoute(platformServices, contentModifier)
                TopLevelDestination.Reference -> ReferenceRoute(contentModifier)
                TopLevelDestination.Settings -> SettingsScreen(contentModifier)
            }
        }
    }
}
