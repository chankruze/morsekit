package `in`.geekofia.morsekit.platform

/**
 * Writes plain text to the system clipboard.
 *
 * Named to avoid clashing with `android.content.ClipboardManager` and Compose's `ClipboardManager`.
 */
fun interface ClipboardService {
    fun copyText(text: String)
}

/** Opens the platform share sheet for plain text. */
fun interface ShareService {
    fun shareText(text: String)
}

/**
 * Platform capabilities the shared code depends on. Built by each platform's entry point
 * (`MainActivity` / `MainViewController`) and passed down explicitly — no DI framework or globals.
 */
class PlatformServices(
    val clipboard: ClipboardService,
    val share: ShareService,
)
