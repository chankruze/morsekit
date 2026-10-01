package `in`.geekofia.morsekit.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.geekofia.morsekit.core.history.HistoryRepository
import `in`.geekofia.morsekit.core.settings.AppSettings
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.settings.ThemeMode
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.feature.history.ClearHistoryDialog
import `in`.geekofia.morsekit.platform.AppInfo
import `in`.geekofia.morsekit.platform.ReviewService
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import `in`.geekofia.morsekit.ui.components.SectionCard
import `in`.geekofia.morsekit.ui.theme.spaceGroteskFontFamily
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_more_vert
import morsekit.shared.generated.resources.ic_star
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Connects the screen to [SettingsRepository]. There's no ViewModel: the repository already
 * holds the state, validates it and persists it, so a ViewModel would only forward calls.
 */
@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    appInfo: AppInfo,
    reviewService: ReviewService,
    historyRepository: HistoryRepository,
    modifier: Modifier = Modifier,
    onCheckForUpdates: (() -> Unit)? = null,
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    val history by historyRepository.entries.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        canClearHistory = history.any { !it.favorite },
        onSaveHistoryChange = settingsRepository::setSaveHistory,
        onClearHistory = historyRepository::clearRecent,
        appInfo = appInfo,
        onCheckForUpdates = onCheckForUpdates,
        rateStoreName = reviewService.storeName.takeIf { reviewService.canOpenStorePage },
        onRate = reviewService::openStorePage,
        onThemeModeChange = settingsRepository::setThemeMode,
        onWordsPerMinuteChange = settingsRepository::setWordsPerMinute,
        onToneFrequencyChange = settingsRepository::setToneFrequencyHz,
        onResetToDefaults = settingsRepository::resetToDefaults,
        modifier = modifier,
    )
}

/** Header with an overflow menu (Reset to defaults, confirmed first) above the settings. */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    canClearHistory: Boolean,
    onSaveHistoryChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    appInfo: AppInfo,
    onCheckForUpdates: (() -> Unit)?,
    rateStoreName: String?,
    onRate: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWordsPerMinuteChange: (Int) -> Unit,
    onToneFrequencyChange: (Int) -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var confirmClearHistory by remember { mutableStateOf(false) }

    if (confirmClearHistory) {
        ClearHistoryDialog(
            onConfirm = {
                confirmClearHistory = false
                onClearHistory()
            },
            onDismiss = { confirmClearHistory = false },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset settings?") },
            text = { Text("Theme, speed and tone go back to their default values.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onResetToDefaults()
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }

    ScreenScaffold(
        modifier = modifier,
        title = { Text("Settings", modifier = Modifier.semantics { heading() }) },
        actions = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(painterResource(Res.drawable.ic_more_vert), contentDescription = "More options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Reset to defaults") },
                        onClick = {
                            menuOpen = false
                            confirmReset = true
                        },
                    )
                }
            }
        },
    ) { contentModifier ->
        SettingsContent(
            settings = settings,
                canClearHistory = canClearHistory,
                onSaveHistoryChange = onSaveHistoryChange,
                onClearHistory = { confirmClearHistory = true },
            appInfo = appInfo,
            onCheckForUpdates = onCheckForUpdates,
            rateStoreName = rateStoreName,
            onRate = onRate,
            onThemeModeChange = onThemeModeChange,
            onWordsPerMinuteChange = onWordsPerMinuteChange,
            onToneFrequencyChange = onToneFrequencyChange,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun SettingsContent(
    settings: AppSettings,
    canClearHistory: Boolean,
    onSaveHistoryChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    appInfo: AppInfo,
    onCheckForUpdates: (() -> Unit)?,
    rateStoreName: String?,
    onRate: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWordsPerMinuteChange: (Int) -> Unit,
    onToneFrequencyChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "Appearance") {
            Text("Theme", style = MaterialTheme.typography.bodyLarge)
            ThemeSelector(selected = settings.themeMode, onSelected = onThemeModeChange)
        }

        SectionCard(title = "Playback") {
            SliderSetting(
                label = "Speed",
                valueText = "${settings.wordsPerMinute} WPM",
                value = settings.wordsPerMinute,
                range = MorseTiming.MIN_WPM..MorseTiming.MAX_WPM,
                onValueChange = onWordsPerMinuteChange,
                supportingText = "A dot lasts ${MorseTiming(settings.wordsPerMinute).unit.inWholeMilliseconds} ms at this speed.",
            )
            SliderSetting(
                label = "Tone",
                valueText = "${settings.toneFrequencyHz} Hz",
                value = settings.toneFrequencyHz,
                range = AppSettings.MIN_TONE_HZ..AppSettings.MAX_TONE_HZ,
                step = AppSettings.TONE_STEP_HZ,
                onValueChange = onToneFrequencyChange,
                supportingText = "Pitch of the audio tone.",
            )
            Text(
                text = "Used for sound, flashlight and vibration transmission in the translator.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "History") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Save history", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Translations you copy, share or send. Kept only on this device, never backed up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.saveHistory,
                    onCheckedChange = onSaveHistoryChange,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            TextButton(onClick = onClearHistory, enabled = canClearHistory) { Text("Clear history") }
        }

        SectionCard(title = "About") {
            if (rateStoreName != null) {
                RateBanner(storeName = rateStoreName, onClick = onRate)
            }
            LabeledValue(label = "Version", value = "${appInfo.versionName} (${appInfo.buildNumber})")
            if (onCheckForUpdates != null) {
                TextButton(onClick = onCheckForUpdates, modifier = Modifier.fillMaxWidth()) {
                    Text("Check for updates")
                }
            }
            HorizontalDivider()
            Text("Privacy", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = PRIVACY_NOTICE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PrivacyPolicyLink()
            HorizontalDivider()
            Text("Open-source libraries", style = MaterialTheme.typography.bodyLarge)
            openSourceLibraries.forEach { library ->
                Column {
                    Text(library.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${library.author} · ${library.license}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DeveloperCredits()
    }
}

/**
 * Opens the store page to rate the app: an explicit tap, allowed on both stores. Deliberately no
 * "Do you like the app?" question, which the stores' rating guidelines discourage.
 */
@Composable
private fun RateBanner(storeName: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Rate MorseKit", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "A quick rating on $storeName helps others find it.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/**
 * Opens the published privacy policy in the browser (the same page Play Console links to). A
 * button rather than an inline link, so it gets a full 48 dp touch target.
 */
@Composable
private fun PrivacyPolicyLink() {
    val uriHandler = LocalUriHandler.current
    TextButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
        Text("Read the full privacy policy")
    }
}

/**
 * "Developed with ❤️ by chankruze at geekofia", flat at the bottom of the screen (not a card),
 * in Space Grotesk. Both names are links; tapping one opens the browser.
 */
@Composable
private fun DeveloperCredits() {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold),
    )
    val credits = buildAnnotatedString {
        append("Developed with ❤️ by ")
        withLink(LinkAnnotation.Url(DEVELOPER_URL, linkStyles)) { append(DEVELOPER_NAME) }
        append(" at ")
        withLink(LinkAnnotation.Url(ORGANIZATION_URL, linkStyles)) { append(ORGANIZATION_NAME) }
    }
    Text(
        text = credits,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = spaceGroteskFontFamily()),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    val options = ThemeMode.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(mode.label) },
            )
        }
    }
}

/** A labelled slider over whole numbers. [step] > 1 snaps to multiples of it. */
@Composable
private fun SliderSetting(
    label: String,
    valueText: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    supportingText: String,
    step: Int = 1,
) {
    Column {
        LabeledValue(label = label, value = valueText)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            // Tick marks only when snapping; a 1-step slider would draw dozens of them.
            steps = if (step > 1) (range.last - range.first) / step - 1 else 0,
            // The default inactive track (secondaryContainer) is the same brightness as the card in
            // the light theme (1.00:1). Outline at 85% reaches >= 3:1 against the card in both themes.
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.85f),
            ),
            modifier = Modifier.semantics { stateDescription = valueText },
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }
