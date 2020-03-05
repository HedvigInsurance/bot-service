package com.hedvig.botService.resolvers

import com.hedvig.localization.service.TextKeysLocaleResolver
import com.hedvig.localization.service.TextKeysLocaleResolverImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


class TextKeysLocaleResolverTest {

    lateinit var textKeysLocaleResolver: TextKeysLocaleResolver

    @Before
    fun setUp() {
        textKeysLocaleResolver = TextKeysLocaleResolverImpl()
    }

    @Test
    fun givenFrenchWithEnglishLowerQFactor_thenReturnEnglish() {
        val locale = textKeysLocaleResolver.resolveLocale("fr-CH, fr;q=0.9, en;q=0.8")

        assertThat(locale).isEqualTo(TextKeysLocaleResolverImpl.DEFAULT_LOCALE)
    }

    @Test
    fun givenSwedishWithEnglishLowerQFactor_thenReturnSwedish() {
        val locale = textKeysLocaleResolver.resolveLocale("sv, en;q=0.8")

        assertThat(locale).isEqualTo(Locale("sv", "se"))
    }

    @Test
    fun givenOnlyFrench_thenDefaultsToSwedish() {
        val locale = textKeysLocaleResolver.resolveLocale("fr")

        assertThat(locale).isEqualTo(Locale("sv", "se"))
    }

}
