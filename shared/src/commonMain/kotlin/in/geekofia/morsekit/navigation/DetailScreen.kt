package `in`.geekofia.morsekit.navigation

/** Screens opened from a tab, on top of it (not in the bottom bar). */
enum class DetailScreen {
    /** Opened from the Translator. */
    History;

    /**
     * The explicit `NavEntry` content key (`entry<DetailScreen>(clazzContentKey = …)`), so the
     * state decorator can tell a closed detail screen from a tab without relying on Navigation
     * 3's default key format.
     */
    val contentKey: String get() = "$CONTENT_KEY_PREFIX$name"

    companion object {
        private const val CONTENT_KEY_PREFIX = "detail:"

        fun isContentKey(key: Any): Boolean = key is String && key.startsWith(CONTENT_KEY_PREFIX)
    }
}
