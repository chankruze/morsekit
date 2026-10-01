package `in`.geekofia.morsekit.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.geekofia.morsekit.core.history.HistoryEntry
import `in`.geekofia.morsekit.core.history.HistoryRepository
import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import kotlinx.coroutines.launch
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_arrow_back
import morsekit.shared.generated.resources.ic_delete
import morsekit.shared.generated.resources.ic_more_vert
import morsekit.shared.generated.resources.ic_star
import morsekit.shared.generated.resources.ic_star_border
import org.jetbrains.compose.resources.painterResource

private enum class HistoryFilter(val label: String) { All("All"), Favorites("Favourites") }

/**
 * The translations that were used, newest first. Tapping one opens it in the translator; each has
 * a favourite toggle and Delete (with Undo). The data lives in [HistoryRepository]; this screen
 * holds no state of its own beyond the filter and dialogs.
 */
@Composable
fun HistoryRoute(
    historyRepository: HistoryRepository,
    settingsRepository: SettingsRepository,
    onOpen: (HistoryEntry) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries by historyRepository.entries.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.All) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val delete: (HistoryEntry) -> Unit = { entry ->
        historyRepository.delete(entry.id)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar("Deleted", actionLabel = "Undo", duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) historyRepository.restore(entry)
        }
    }

    ScreenScaffold(
        title = { Text("History", modifier = Modifier.semantics { heading() }) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")
            }
        },
        actions = { ClearMenu(enabled = entries.any { !it.favorite }, onClear = { confirmClear = true }) },
    ) { inner ->
        Box(inner.fillMaxSize()) {
            HistoryScreen(
                entries = if (filter == HistoryFilter.Favorites) entries.filter { it.favorite } else entries,
                filter = filter,
                saving = settings.saveHistory,
                onFilterChange = { filter = it },
                onTurnOn = { settingsRepository.setSaveHistory(true) },
                onOpen = onOpen,
                onFavoriteChange = { entry, favorite -> historyRepository.setFavorite(entry.id, favorite) },
                onDelete = delete,
            )
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(8.dp))
        }
    }

    if (confirmClear) {
        ClearHistoryDialog(
            onConfirm = {
                confirmClear = false
                historyRepository.clearRecent()
            },
            onDismiss = { confirmClear = false },
        )
    }
}

@Composable
private fun HistoryScreen(
    entries: List<HistoryEntry>,
    filter: HistoryFilter,
    saving: Boolean,
    onFilterChange: (HistoryFilter) -> Unit,
    onTurnOn: () -> Unit,
    onOpen: (HistoryEntry) -> Unit,
    onFavoriteChange: (HistoryEntry, Boolean) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterSelector(filter, onFilterChange)
        if (!saving) SavingOffNotice(onTurnOn)
        if (entries.isEmpty()) {
            EmptyHistory(filter)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onOpen = { onOpen(entry) },
                        onFavoriteChange = { onFavoriteChange(entry, it) },
                        onDelete = { onDelete(entry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSelector(selected: HistoryFilter, onSelected: (HistoryFilter) -> Unit) {
    val options = HistoryFilter.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(option.label) },
            )
        }
    }
}

/** One entry: the whole card opens it; the star and delete are separate targets. */
@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    onOpen: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClickLabel = "Open in translator", role = Role.Button, onClick = onOpen)
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val fromMorse = entry.direction == TranslationDirection.MorseToText
                Text(
                    text = if (fromMorse) "Morse → Text" else "Text → Morse",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { contentDescription = if (fromMorse) "Morse to Text" else "Text to Morse" },
                )
                Text(
                    text = entry.input,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    // Morse as "dot dash", like the translator, not as punctuation.
                    modifier = if (fromMorse) {
                        Modifier.semantics { contentDescription = MorseNotation.toSpokenForm(entry.input) }
                    } else {
                        Modifier
                    },
                )
                if (!fromMorse) {
                    MorseDisplay(morse = entry.output, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        text = entry.output,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconToggleButton(checked = entry.favorite, onCheckedChange = onFavoriteChange) {
                Icon(
                    painter = painterResource(if (entry.favorite) Res.drawable.ic_star else Res.drawable.ic_star_border),
                    contentDescription = "Favourite",
                    tint = if (entry.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(painterResource(Res.drawable.ic_delete), contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun SavingOffNotice(onTurnOn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Saving history is off. New translations aren't added.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            )
            TextButton(onClick = onTurnOn) { Text("Turn on") }
        }
    }
}

@Composable
private fun EmptyHistory(filter: HistoryFilter) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp), contentAlignment = Alignment.Center) {
        Text(
            text = when (filter) {
                HistoryFilter.All -> "No history yet. Translations you copy, share or send appear here."
                HistoryFilter.Favorites -> "No favourites yet. Tap ☆ on an entry to keep it."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ClearMenu(enabled: Boolean, onClear: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(painterResource(Res.drawable.ic_more_vert), contentDescription = "More options")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Clear history") },
                enabled = enabled,
                onClick = {
                    open = false
                    onClear()
                },
            )
        }
    }
}

/** Shared by History and Settings. */
@Composable
fun ClearHistoryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear history?") },
        text = { Text("Removes every entry except your favourites. This can't be undone.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Clear") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
