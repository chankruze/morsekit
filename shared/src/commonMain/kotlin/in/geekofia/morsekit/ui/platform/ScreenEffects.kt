package `in`.geekofia.morsekit.ui.platform

import androidx.compose.runtime.Composable

// Small, parameterless platform facts tied to the UI, so `expect`/`actual` rather than a service
// in PlatformServices (see docs/05-platform-services.md).

/** Keeps the screen from turning off while this is in composition. */
@Composable
expect fun KeepScreenOn()

/**
 * Whether the screen is only being recreated for a configuration change (rotation, window resize),
 * not left. Read it when stopping things "because the screen went away".
 */
@Composable
expect fun rememberIsChangingConfigurations(): () -> Boolean
