package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

/**
 * The app's navigation state and back rules, as plain testable logic. The UI renders [entries]
 * (Navigation 3's `NavDisplay`) and forwards system back to [goBack].
 *
 * Back follows Material's bottom-navigation rule: from any other tab, back returns to the
 * [start] tab; from the start tab, back leaves the app. [entries] has more than one item exactly
 * when back is handled in-app, which is the condition `NavDisplay` uses to intercept back.
 *
 * Every tab currently has a single screen. When detail screens arrive (e.g. History → entry),
 * each tab gets its own stack here and back pops it first.
 */
class AppBackStack(
    val start: TopLevelDestination = TopLevelDestination.Translator,
    initialTab: TopLevelDestination = start,
) {
    var currentTab by mutableStateOf(initialTab)
        private set

    /** What's on screen, bottom first: the start tab, then the current tab if it's another one. */
    val entries: List<TopLevelDestination>
        get() = if (currentTab == start) listOf(start) else listOf(start, currentTab)

    fun selectTab(tab: TopLevelDestination) {
        currentTab = tab
    }

    /** Handles back in-app if possible. Returns false when back should leave the app. */
    fun goBack(): Boolean {
        if (currentTab == start) return false
        currentTab = start
        return true
    }

    companion object {
        /** Survives rotation and process death: only the current tab needs saving. */
        val Saver: Saver<AppBackStack, String> = Saver(
            save = { it.currentTab.name },
            restore = { name ->
                AppBackStack(initialTab = TopLevelDestination.entries.firstOrNull { it.name == name } ?: TopLevelDestination.Translator)
            },
        )
    }
}
