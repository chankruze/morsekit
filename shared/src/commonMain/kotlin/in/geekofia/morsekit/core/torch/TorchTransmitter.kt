package `in`.geekofia.morsekit.core.torch

import `in`.geekofia.morsekit.platform.TorchController
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.TimeSource

/** The torch refused to switch, e.g. another app took the camera or it overheated. */
class TorchFailedException : Exception("The flashlight stopped responding")

/**
 * Flashes [plan] on [torch] until it finishes or the calling coroutine is cancelled.
 *
 * The torch is **always** left off: on completion, cancellation or failure. The torch is only
 * switched when its state changes. [clock] and [sleep] are injectable for tests.
 *
 * @throws TorchFailedException if the torch can't be switched on.
 */
suspend fun transmitWithTorch(
    plan: TorchPlan,
    torch: TorchController,
    clock: TimeSource = TimeSource.Monotonic,
    sleep: suspend (Duration) -> Unit = { delay(it) },
) {
    val start = clock.markNow()
    var isOn: Boolean? = null
    try {
        while (true) {
            val step = plan.stepAt(start.elapsedNow())
            if (step.isOn != isOn) {
                if (!torch.setTorch(step.isOn) && step.isOn) throw TorchFailedException()
                isOn = step.isOn
            }
            sleep(step.nextChangeIn ?: break)
        }
    } finally {
        torch.setTorch(false)
    }
}
