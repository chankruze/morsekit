package `in`.geekofia.morsekit.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build

/** Uses the application context so the services can safely outlive an Activity. */
fun AndroidPlatformServices(context: Context): PlatformServices {
    val appContext = context.applicationContext
    return PlatformServices(
        clipboard = AndroidClipboardService(appContext),
        share = AndroidShareService(appContext),
    )
}

private class AndroidClipboardService(private val context: Context) : ClipboardService {
    override val showsSystemConfirmation: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun copyText(text: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("MorseKit", text))
    }
}

private class AndroidShareService(private val context: Context) : ShareService {
    override fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        val chooser = Intent.createChooser(send, null)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
