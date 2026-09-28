package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.morse.MorseCategory

data class ReferenceSection(
    val category: MorseCategory,
    val entries: List<ReferenceEntry>,
)

data class ReferenceUiState(
    val query: String = "",
    /** Non-empty sections in category order (letters, digits, punctuation). */
    val sections: List<ReferenceSection> = emptyList(),
) {
    val hasResults: Boolean get() = sections.isNotEmpty()
}

internal fun List<ReferenceEntry>.toSections(): List<ReferenceSection> {
    val byCategory = groupBy { it.category }
    return MorseCategory.entries.mapNotNull { category ->
        byCategory[category]?.let { ReferenceSection(category, it) }
    }
}
