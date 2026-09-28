package `in`.geekofia.morsekit.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.geekofia.morsekit.core.settings.AppSettings
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.settings.ThemeMode
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.AppInfo
import `in`.geekofia.morsekit.ui.components.SectionCard
import kotlin.math.roundToInt

/**
 * Connects the screen to [SettingsRepository]. There's no ViewModel: the repository already
 * holds the state, validates it and persists it, so a ViewModel would only forward calls.
 */
@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    appInfo: AppInfo,
    modifier: Modifier = Modifier,
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        appInfo = appInfo,
        onThemeModeChange = settingsRepository::setThemeMode,
        onWordsPerMinuteChange = settingsRepository::setWordsPerMinute,
        onToneFrequencyChange = settingsRepository::setToneFrequencyHz,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    appInfo: AppInfo,
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
                text = "Used when playing Morse audio in the translator. Flashlight and vibration are coming in a future update.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "About") {
            LabeledValue(label = "Version", value = "${appInfo.versionName} (${appInfo.buildNumber})")
            HorizontalDivider()
            Text("Privacy", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = PRIVACY_NOTICE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    }
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
