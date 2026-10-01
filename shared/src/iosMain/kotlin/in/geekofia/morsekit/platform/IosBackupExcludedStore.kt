package `in`.geekofia.morsekit.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

/**
 * A [KeyValueStore] kept out of iCloud and computer backups: one UTF-8 file per key in
 * Application Support/[directoryName], with the folder and every file marked
 * `NSURLIsExcludedFromBackupKey`. `NSUserDefaults` can't be excluded (it's always backed up), so
 * History, which holds typed messages, uses this instead. Numbers and booleans are stored as
 * text; History itself only needs strings.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosBackupExcludedStore(directoryName: String) : KeyValueStore {
    private val directory: NSURL? = run {
        val support = NSFileManager.defaultManager
            .URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask)
            .firstOrNull() as? NSURL ?: return@run null
        val directory = support.URLByAppendingPathComponent(directoryName, isDirectory = true) ?: return@run null
        NSFileManager.defaultManager.createDirectoryAtURL(directory, withIntermediateDirectories = true, attributes = null, error = null)
        directory.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
        directory
    }

    override fun getString(key: String): String? =
        fileFor(key)?.let { NSString.stringWithContentsOfURL(it, encoding = NSUTF8StringEncoding, error = null) }

    override fun putString(key: String, value: String) {
        val file = fileFor(key) ?: return
        NSString.create(string = value).writeToURL(file, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        // Set after every write: an atomic write replaces the file, which can drop the flag.
        file.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    }

    override fun getInt(key: String): Int? = getString(key)?.toIntOrNull()

    override fun putInt(key: String, value: Int) = putString(key, value.toString())

    override fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()

    override fun putBoolean(key: String, value: Boolean) = putString(key, value.toString())

    /** Keys are MorseKit's own constants (`history.entries`), so they're safe file names. */
    private fun fileFor(key: String): NSURL? = directory?.URLByAppendingPathComponent("$key.txt")
}
