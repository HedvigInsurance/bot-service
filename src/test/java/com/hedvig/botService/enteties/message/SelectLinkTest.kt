package com.hedvig.botService.enteties.message

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.serviceIntegration.localization.LocalizationClient
import com.hedvig.botService.services.LocalizationService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

@RunWith(MockitoJUnitRunner::class)
class SelectLinkTest {

    lateinit var userContext: UserContext

    @Mock
    internal var localizationService: LocalizationService? = null

    @Before
    fun setup() {
        userContext = UserContext()
    }

    @Test
    fun render_will_ignore_null_links() {

        val link = SelectLink("text", null, null, null, null, false)

        link.render("", userContext, localizationService)
    }
}
