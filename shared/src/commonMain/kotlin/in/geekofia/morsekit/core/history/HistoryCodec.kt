package `in`.geekofia.morsekit.core.history

import `in`.geekofia.morsekit.core.model.TranslationDirection

/**
 * Stores history as one string without a JSON library: entries are separated by [RECORD] and
 * fields by [FIELD] (ASCII control characters nobody types). Free text escapes `\`, [RECORD] and
 * [FIELD], so any message survives the round trip. Entries that don't parse are skipped, so a
 * corrupt value loses at most those entries, never the app.
 */
internal object HistoryCodec {
    private const val VERSION = "h1"
    private const val RECORD = '\u001E'
    private const val FIELD = '\u001F'
    private const val FIELDS = 6

    fun encode(entries: List<HistoryEntry>): String = buildString {
        append(VERSION)
        entries.forEach { e ->
            append(RECORD)
            append(listOf(e.id.toString(), e.direction.name, e.savedAt.toString(), if (e.favorite) "1" else "0", escape(e.input), escape(e.output)).joinToString(FIELD.toString()))
        }
    }

    fun decode(encoded: String?): List<HistoryEntry> {
        val records = encoded?.split(RECORD) ?: return emptyList()
        if (records.firstOrNull() != VERSION) return emptyList()
        return records.drop(1).mapNotNull { record ->
            val f = record.split(FIELD)
            if (f.size != FIELDS) return@mapNotNull null
            HistoryEntry(
                id = f[0].toLongOrNull() ?: return@mapNotNull null,
                direction = TranslationDirection.entries.firstOrNull { it.name == f[1] } ?: return@mapNotNull null,
                savedAt = f[2].toLongOrNull() ?: return@mapNotNull null,
                favorite = f[3] == "1",
                input = unescape(f[4]) ?: return@mapNotNull null,
                output = unescape(f[5]) ?: return@mapNotNull null,
            )
        }
    }

    private fun escape(text: String): String = buildString {
        text.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                RECORD -> append("\\r")
                FIELD -> append("\\f")
                else -> append(c)
            }
        }
    }

    /** `null` if [text] has a broken escape. */
    private fun unescape(text: String): String? = buildString {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c != '\\') {
                append(c)
                i++
                continue
            }
            when (text.getOrNull(i + 1)) {
                '\\' -> append('\\')
                'r' -> append(RECORD)
                'f' -> append(FIELD)
                else -> return null
            }
            i += 2
        }
    }
}
