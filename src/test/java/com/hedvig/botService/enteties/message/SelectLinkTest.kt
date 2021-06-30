package com.hedvig.botService.enteties.message

import com.hedvig.botService.enteties.UserContext
import com.hedvig.libs.translations.Translations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SelectLinkTest {

    lateinit var userContext: UserContext

    @Mock
    internal lateinit var translations: Translations

    @Before
    fun setup() {
        userContext = UserContext()
    }

    @Test
    fun render_will_ignore_null_links() {

        val link = SelectLink("text", null, null, null, null, false)

        link.render("", userContext, translations)
    }
}
