package `in`.geekofia.morsekit.navigation

import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppBackStackDetailTest {
    private val backStack = AppBackStack()
    private val scope = SaverScope { true }

    @Test
    fun aDetailScreenSitsOnTopOfItsTabAndBackClosesItFirst() {
        backStack.open(DetailScreen.History)
        assertEquals(DetailScreen.History, backStack.currentDetail)
        assertEquals(listOf<Any>(TopLevelDestination.Translator, DetailScreen.History), backStack.entries)
        assertTrue(backStack.goBack())
        assertNull(backStack.currentDetail)
        assertEquals(listOf<Any>(TopLevelDestination.Translator), backStack.entries)
        assertFalse(backStack.goBack(), "then back leaves the app")
    }

    @Test
    fun aTabKeepsItsDetailScreenWhileAnotherTabIsSelected() {
        backStack.open(DetailScreen.History)
        backStack.selectTab(TopLevelDestination.Learn)
        assertNull(backStack.currentDetail)
        assertEquals(listOf<Any>(TopLevelDestination.Translator, DetailScreen.History, TopLevelDestination.Learn), backStack.entries)
        assertTrue(backStack.goBack())
        assertEquals(TopLevelDestination.Translator, backStack.currentTab)
        assertEquals(DetailScreen.History, backStack.currentDetail, "back to the Translator's History")
    }

    @Test
    fun selectingTheCurrentTabAgainClosesItsDetailScreen() {
        backStack.open(DetailScreen.History)
        backStack.selectTab(TopLevelDestination.Translator)
        assertNull(backStack.currentDetail)
    }

    @Test
    fun detailScreensSurviveRestoring() {
        backStack.open(DetailScreen.History)
        backStack.selectTab(TopLevelDestination.Tap)
        val saved = with(AppBackStack.Saver) { scope.save(backStack) }!!
        val restored = AppBackStack.Saver.restore(saved)!!
        assertEquals(TopLevelDestination.Tap, restored.currentTab)
        assertEquals(backStack.entries, restored.entries)
    }

    @Test
    fun unknownSavedNamesAreDropped() {
        val restored = AppBackStack.Saver.restore("Tap|Translator=Nope|Gone=History|Translator=History")!!
        assertEquals(listOf<Any>(TopLevelDestination.Translator, DetailScreen.History, TopLevelDestination.Tap), restored.entries)
        assertEquals(TopLevelDestination.Translator, AppBackStack.Saver.restore("")!!.currentTab)
    }

    @Test
    fun detailContentKeysAreRecognisedAndTabsAreNot() {
        assertTrue(DetailScreen.isContentKey(DetailScreen.History.contentKey))
        assertFalse(DetailScreen.isContentKey("Translator"))
        assertFalse(DetailScreen.isContentKey(TopLevelDestination.Translator))
    }
}
