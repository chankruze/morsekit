package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.morse.MorseCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReferenceViewModelTest {
    private val viewModel = ReferenceViewModel()

    private val categories get() = viewModel.uiState.sections.map { it.category }

    @Test
    fun startsWithAllSectionsInOrder() {
        with(viewModel.uiState) {
            assertEquals("", query)
            assertEquals(listOf(MorseCategory.Letter, MorseCategory.Digit, MorseCategory.Punctuation), categories)
            assertEquals(listOf(26, 10, 18), sections.map { it.entries.size })
        }
    }

    @Test
    fun searchKeepsOnlySectionsWithMatches() {
        viewModel.onQueryChange("7")
        assertEquals(listOf(MorseCategory.Digit), categories)
        assertEquals("7", viewModel.uiState.query)
    }

    @Test
    fun searchCanSpanSections() {
        viewModel.onQueryChange("...")
        assertEquals(listOf(MorseCategory.Letter, MorseCategory.Digit, MorseCategory.Punctuation), categories)
    }

    @Test
    fun noMatchesMeansNoResults() {
        viewModel.onQueryChange("xyz")
        assertFalse(viewModel.uiState.hasResults)
    }

    @Test
    fun clearingTheQueryRestoresEverything() {
        viewModel.onQueryChange("xyz")
        viewModel.onQueryChange("")
        assertTrue(viewModel.uiState.hasResults)
        assertEquals(54, viewModel.uiState.sections.sumOf { it.entries.size })
    }
}
