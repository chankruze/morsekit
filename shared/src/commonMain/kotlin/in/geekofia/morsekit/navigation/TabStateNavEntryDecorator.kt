package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Gives each tab its own saved state (scroll positions, open search, ...) that survives switching
 * tabs and rotation.
 *
 * Navigation 3's default `SaveableStateHolderNavEntryDecorator` removes an entry's saved state as
 * soon as it leaves the back stack. Tabs leave it whenever another tab is selected (see
 * [AppBackStack.entries]), so the default would reset a tab on every switch. This keeps it.
 * When detail screens are added, their state should still be removed on pop.
 */
@Composable
fun rememberTabStateNavEntryDecorator(): NavEntryDecorator<TopLevelDestination> {
    val stateHolder = rememberSaveableStateHolder()
    return remember(stateHolder) {
        NavEntryDecorator(
            onPop = { /* Tabs keep their state when another tab is selected. */ },
            decorate = { entry -> stateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() } },
        )
    }
}
