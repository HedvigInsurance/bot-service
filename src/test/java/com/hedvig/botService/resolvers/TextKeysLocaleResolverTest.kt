package com.hedvig.botService.resolvers

import com.hedvig.botService.services.TextKeysLocaleResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class TextKeysLocaleResolverTest {

    lateinit var textKeysLocaleResolver: TextKeysLocaleResolver

    @Before
    fun setUp() {
        textKeysLocaleResolver = TextKeysLocaleResolver()
    }

    @Test
    fun givenFrenchWithEnglishLowerQFactor_thenReturnEnglish() {
        val locale = textKeysLocaleResolver.resolveLocale("fr-CH, fr;q=0.9, en;q=0.8")

        assertThat(locale.language).isEqualTo(Locale("en").language)
    }

    @Test
    fun givenSwedishWithEnglishLowerQFactor_thenReturnSwedish() {
        val locale = textKeysLocaleResolver.resolveLocale("sv, en;q=0.8")

        assertThat(locale.language).isEqualTo(Locale("sv").language)
    }

    @Test
    fun givenOnlyFrench_thenDefaultsToSwedish() {
        val locale = textKeysLocaleResolver.resolveLocale("fr")

        assertThat(locale.language).isEqualTo(Locale("sv").language)
    }

}
