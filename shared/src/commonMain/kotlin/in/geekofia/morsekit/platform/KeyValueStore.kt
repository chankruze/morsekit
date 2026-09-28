package `in`.geekofia.morsekit.platform

/**
 * Small persistent key-value storage for preferences: `SharedPreferences` on Android,
 * `NSUserDefaults` on iOS. Getters return `null` for keys that were never written.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getInt(key: String): Int?
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String): Boolean?
    fun putBoolean(key: String, value: Boolean)
}

/** A non-persistent [KeyValueStore] for tests and previews. */
class InMemoryKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val ints = mutableMapOf<String, Int>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun getString(key: String): String? = strings[key]
    override fun putString(key: String, value: String) { strings[key] = value }
    override fun getInt(key: String): Int? = ints[key]
    override fun putInt(key: String, value: Int) { ints[key] = value }
    override fun getBoolean(key: String): Boolean? = booleans[key]
    override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
}
