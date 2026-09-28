package `in`.geekofia.morsekit.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A screen with its own top app bar (left-aligned, or centred with [centerTitle]), for use inside
 * the app's bottom-navigation Scaffold. The bar uses the default surface colours and extends under
 * the status bar, which the host styles to match (see `SystemBarAppearance`).
 *
 * Insets are split so nothing is applied twice: the outer Scaffold handles the bottom bar, the
 * [TopAppBar] handles the status bar, and this Scaffold adds no insets of its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    centerTitle: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (centerTitle) {
                CenterAlignedTopAppBar(title = title, navigationIcon = navigationIcon, actions = actions)
            } else {
                TopAppBar(title = title, navigationIcon = navigationIcon, actions = actions)
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
