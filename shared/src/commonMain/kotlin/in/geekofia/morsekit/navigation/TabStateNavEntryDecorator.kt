package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Gives each tab its own saved state (scroll positions, open search, ...) that survives switching
 * tabs and rotation, and clears a detail screen's state when it's closed.
 *
 * Navigation 3's default `SaveableStateHolderNavEntryDecorator` removes an entry's saved state as
 * soon as it leaves the back stack. Tabs leave it whenever another tab is selected (see
 * [AppBackStack.entries]), so the default would reset a tab on every switch. A closed
 * [DetailScreen] should start fresh next time, so its state is removed.
 */
@Composable
fun rememberTabStateNavEntryDecorator(): NavEntryDecorator<Any> {
    val stateHolder = rememberSaveableStateHolder()
    return remember(stateHolder) {
        NavEntryDecorator(
            // Tabs keep their state when another tab is selected; closed detail screens don't.
            // onPop gets the entry's content key, not the route: see DetailScreen.contentKey.
            onPop = { contentKey -> if (DetailScreen.isContentKey(contentKey)) stateHolder.removeState(contentKey) },
            decorate = { entry -> stateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() } },
        )
    }
}
