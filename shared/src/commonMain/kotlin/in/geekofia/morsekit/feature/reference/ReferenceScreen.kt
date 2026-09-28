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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ReferenceScreen(
    state: ReferenceUiState,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )

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

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        label = { Text("Search") },
        placeholder = { Text("A, 7, .-, comma") },
        trailingIcon = if (query.isNotEmpty()) {
            { TextButton(onClick = { onQueryChange("") }) { Text("Clear") } }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
