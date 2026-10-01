package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

/**
 * The app's navigation state and back rules, as plain testable logic. The UI renders [entries]
 * (Navigation 3's `NavDisplay`) and forwards system back to [goBack].
 *
 * Back follows Material's bottom-navigation rules:
 *
 * - a detail screen ([DetailScreen], e.g. History) sits on top of the tab it was opened from, and
 *   back closes it first;
 * - from any other tab, back returns to the [start] tab; from the start tab, back leaves the app;
 * - each tab keeps its detail screen while another tab is selected; selecting the tab you're on
 *   again closes it (back to the tab's root).
 *
 * [entries] has more than one item exactly when back is handled in-app, which is the condition
 * `NavDisplay` uses to intercept back.
 */
class AppBackStack(
    val start: TopLevelDestination = TopLevelDestination.Translator,
    initialTab: TopLevelDestination = start,
    initialDetails: Map<TopLevelDestination, DetailScreen> = emptyMap(),
) {
    var currentTab by mutableStateOf(initialTab)
        private set

    private var details by mutableStateOf(initialDetails)

    /** The detail screen open on the current tab, if any. */
    val currentDetail: DetailScreen? get() = details[currentTab]

    /**
     * What's on the stack, bottom first: the start tab and its detail screen, then (if another
     * tab is selected) that tab and its detail screen. Entries are [TopLevelDestination]s and
     * [DetailScreen]s.
     */
    val entries: List<Any>
        get() = buildList {
            add(start)
            details[start]?.let(::add)
            if (currentTab != start) {
                add(currentTab)
                details[currentTab]?.let(::add)
            }
        }

    fun selectTab(tab: TopLevelDestination) {
        if (tab == currentTab) details = details - tab
        currentTab = tab
    }

    /** Opens [detail] on top of the current tab. */
    fun open(detail: DetailScreen) {
        details = details + (currentTab to detail)
    }

    /** Handles back in-app if possible. Returns false when back should leave the app. */
    fun goBack(): Boolean {
        if (details[currentTab] != null) {
            details = details - currentTab
            return true
        }
        if (currentTab == start) return false
        currentTab = start
        return true
    }

    companion object {
        /**
         * Survives rotation and process death: the current tab, then each tab's detail screen,
         * e.g. `Tap|Translator=History`. Unknown names (from an older version) are dropped.
         */
        val Saver: Saver<AppBackStack, String> = Saver(
            save = { stack ->
                (listOf(stack.currentTab.name) + stack.details.map { (tab, detail) -> "${tab.name}=${detail.name}" }).joinToString("|")
            },
            restore = { saved ->
                val parts = saved.split('|')
                val tab = TopLevelDestination.entries.firstOrNull { it.name == parts.first() } ?: TopLevelDestination.Translator
                val details = parts.drop(1).mapNotNull { part ->
                    val (tabName, detailName) = part.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
                    val owner = TopLevelDestination.entries.firstOrNull { it.name == tabName } ?: return@mapNotNull null
                    val detail = DetailScreen.entries.firstOrNull { it.name == detailName } ?: return@mapNotNull null
                    owner to detail
                }.toMap()
                AppBackStack(initialTab = tab, initialDetails = details)
            },
        )
    }
}
