package `in`.geekofia.morsekit.feature.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import `in`.geekofia.morsekit.core.update.UpdateController

/**
 * App-level update UI: checks whenever the app comes to the foreground, and shows the
 * "Update ready" and "Up to date" dialogs. A dialog rather than a snackbar because a download can
 * finish on any screen, and a snackbar would sit on top of the translator's Transmit button.
 */
@Composable
fun UpdateDialogs(controller: UpdateController) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { controller.onAppResumed() }

    if (controller.readyToInstall) {
        AlertDialog(
            onDismissRequest = controller::postponeInstall,
            title = { Text("Update ready") },
            text = { Text("A new version of MorseKit has downloaded. Restart now to finish updating.") },
            confirmButton = { TextButton(onClick = controller::installNow) { Text("Restart") } },
            dismissButton = { TextButton(onClick = controller::postponeInstall) { Text("Later") } },
        )
    }

    if (controller.manualResult == UpdateController.ManualResult.UpToDate) {
        AlertDialog(
            onDismissRequest = controller::dismissManualResult,
            title = { Text("You're up to date") },
            text = { Text("You have the latest version of MorseKit.") },
            confirmButton = { TextButton(onClick = controller::dismissManualResult) { Text("OK") } },
        )
    }
}
