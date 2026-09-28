package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppBackStackTest {
    private val backStack = AppBackStack()

    @Test
    fun startsOnTheTranslator() {
        assertEquals(TopLevelDestination.Translator, backStack.currentTab)
        assertEquals(listOf(TopLevelDestination.Translator), backStack.entries)
    }

    @Test
    fun otherTabsSitOnTopOfTheStartTab() {
        backStack.selectTab(TopLevelDestination.Settings)
        assertEquals(listOf(TopLevelDestination.Translator, TopLevelDestination.Settings), backStack.entries)
    }

    @Test
    fun backFromAnotherTabReturnsToTheStartTab() {
        backStack.selectTab(TopLevelDestination.Reference)
        assertTrue(backStack.goBack())
        assertEquals(TopLevelDestination.Translator, backStack.currentTab)
        assertEquals(listOf(TopLevelDestination.Translator), backStack.entries)
    }

    @Test
    fun backOnTheStartTabLeavesTheApp() {
        assertFalse(backStack.goBack())
        assertEquals(TopLevelDestination.Translator, backStack.currentTab)
    }

    @Test
    fun switchingBetweenOtherTabsDoesNotBuildHistory() {
        backStack.selectTab(TopLevelDestination.Reference)
        backStack.selectTab(TopLevelDestination.Settings)
        assertTrue(backStack.goBack())
        assertEquals(TopLevelDestination.Translator, backStack.currentTab)
        assertFalse(backStack.goBack())
    }

    @Test
    fun inAppBackIsExactlyWhenThereIsMoreThanOneEntry() {
        TopLevelDestination.entries.forEach { tab ->
            val stack = AppBackStack(initialTab = tab)
            val handles = stack.entries.size > 1
            assertEquals(handles, stack.goBack(), "tab $tab")
        }
    }

    @Test
    fun saverRestoresTheCurrentTab() {
        backStack.selectTab(TopLevelDestination.Settings)
        val saved = with(AppBackStack.Saver) { SaverScope { true }.save(backStack) }!!
        val restored = AppBackStack.Saver.restore(saved)!!
        assertEquals(TopLevelDestination.Settings, restored.currentTab)
    }

    @Test
    fun unknownSavedTabFallsBackToTheStart() {
        assertEquals(TopLevelDestination.Translator, AppBackStack.Saver.restore("Trainer")!!.currentTab)
    }
}
