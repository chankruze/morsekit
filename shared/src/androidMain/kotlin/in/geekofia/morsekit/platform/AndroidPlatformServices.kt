package `in`.geekofia.morsekit.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit

/** Uses the application context so the services can safely outlive an Activity. */
fun androidPlatformServices(context: Context): PlatformServices {
    val appContext = context.applicationContext
    return PlatformServices(
        clipboard = AndroidClipboardService(appContext),
        share = AndroidShareService(appContext),
        keyValueStore = AndroidKeyValueStore(appContext),
        appInfo = androidAppInfo(appContext),
        audioPlayer = AndroidPcmAudioPlayer(),
        torch = AndroidTorchController(appContext),
        vibration = AndroidVibrationController(appContext),
    )
}

private class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val preferences = context.getSharedPreferences("morsekit", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }

    override fun getInt(key: String): Int? =
        if (preferences.contains(key)) preferences.getInt(key, 0) else null

    override fun putInt(key: String, value: Int) {
        preferences.edit { putInt(key, value) }
    }
}

private fun androidAppInfo(context: Context): AppInfo {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    return AppInfo(
        versionName = packageInfo.versionName ?: "unknown",
        buildNumber = packageInfo.longVersionCode.toString(),
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
