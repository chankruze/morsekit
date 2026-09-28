package `in`.geekofia.morsekit.feature.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.morse.MorseCategory
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.PlaceholderContent
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_arrow_back
import morsekit.shared.generated.resources.ic_close
import morsekit.shared.generated.resources.ic_search
import org.jetbrains.compose.resources.painterResource

/** Wires [ReferenceViewModel] to the stateless [ReferenceScreen]. */
@Composable
fun ReferenceRoute(
    modifier: Modifier = Modifier,
    viewModel: ReferenceViewModel = viewModel { ReferenceViewModel() },
) {
    ReferenceScreen(
        state = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        modifier = modifier,
    )
}

/**
 * The header shows "Reference" and a search action. Searching turns the header into the search
 * field; closing search clears the query, so the full chart comes back.
 */
@Composable
fun ReferenceScreen(
    state: ReferenceUiState,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Starts open if a query survived (e.g. returning to this tab while the ViewModel kept it).
    var searching by rememberSaveable { mutableStateOf(state.query.isNotEmpty()) }
    val closeSearch = {
        searching = false
        onQueryChange("")
    }

    ScreenScaffold(
        modifier = modifier,
        title = {
            if (searching) HeaderSearchField(query = state.query, onQueryChange = onQueryChange) else Text("Reference")
        },
        navigationIcon = {
            if (searching) {
                IconButton(onClick = closeSearch) {
                    Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Close search")
                }
            }
        },
        actions = {
            when {
                !searching -> IconButton(onClick = { searching = true }) {
                    Icon(painterResource(Res.drawable.ic_search), contentDescription = "Search")
                }
                state.query.isNotEmpty() -> IconButton(onClick = { onQueryChange("") }) {
                    Icon(painterResource(Res.drawable.ic_close), contentDescription = "Clear search")
                }
            }
        },
    ) { contentModifier ->
        Column(modifier = contentModifier.fillMaxSize()) {
            if (state.hasResults) {
                ReferenceGrid(sections = state.sections)
            } else {
                PlaceholderContent(
                    title = "No matches",
                    message = "Nothing matches “${state.query.trim()}”. Try a character (A), a code (.-) or a name (comma).",
                )
            }
        }
    }
}

/** A borderless search field for the header; focuses itself when shown. */
@Composable
private fun HeaderSearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine = true,
        placeholder = { Text("A, 7, .-, comma") },
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun ReferenceGrid(sections: List<ReferenceSection>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sections.forEach { section ->
            item(key = "header-${section.category}", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = section.category.title,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(section.entries, key = { it.character.toString() }) { entry ->
                ReferenceCell(entry)
            }
        }
    }
}

@Composable
private fun ReferenceCell(entry: ReferenceEntry) {
    Card(modifier = Modifier.clearAndSetSemantics { contentDescription = entry.accessibilityLabel }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = entry.character.toString(), style = MaterialTheme.typography.headlineMedium)
            MorseDisplay(morse = entry.code, style = MaterialTheme.typography.bodyLarge)
            if (entry.name != null) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val MorseCategory.title: String
    get() = when (this) {
        MorseCategory.Letter -> "Letters"
        MorseCategory.Digit -> "Numbers"
        MorseCategory.Punctuation -> "Punctuation"
    }
