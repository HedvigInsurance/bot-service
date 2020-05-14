package com.hedvig.botService.resolvers

import com.hedvig.resolver.LocaleResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


class TextKeysLocaleResolverTest {

    @Test
    fun givenFrenchWithEnglishLowerQFactor_thenReturnEnglish() {
        val locale = LocaleResolver.resolveLocale("fr-CH, fr;q=0.9, en;q=0.8")

        assertThat(locale).isEqualTo(LocaleResolver.DEFAULT_LOCALE)
    }

    @Test
    fun givenSwedishWithEnglishLowerQFactor_thenReturnSwedish() {
        val locale = LocaleResolver.resolveLocale("sv, en;q=0.8")

        assertThat(locale).isEqualTo(Locale("sv", "se"))
    }

    @Test
    fun givenOnlyFrench_thenDefaultsToSwedish() {
        val locale = LocaleResolver.resolveLocale("fr")

        assertThat(locale).isEqualTo(Locale("sv", "se"))
    }

}
