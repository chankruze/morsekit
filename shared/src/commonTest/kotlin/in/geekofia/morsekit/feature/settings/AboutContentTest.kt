package `in`.geekofia.morsekit.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AboutContentTest {

    @Test
    fun creditLinksPointToTheDeveloperAndOrganization() {
        assertEquals("https://github.com/chankruze", DEVELOPER_URL)
        assertEquals("https://geekofia.in", ORGANIZATION_URL)
        assertTrue(DEVELOPER_URL.endsWith(DEVELOPER_NAME))
    }

    @Test
    fun bundledFontIsCreditedWithItsLicense() {
        // The Space Grotesk font ships in the app, so its OFL licence must be credited.
        val font = openSourceLibraries.single { "Space Grotesk" in it.name }
        assertEquals("SIL Open Font License 1.1", font.license)
    }
}
