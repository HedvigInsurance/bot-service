package com.hedvig.botService.services

import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
@Transactional
class LocaleResolver {
    fun resolveLocale(acceptLanguage: String?): Locale {
        if (acceptLanguage.isNullOrBlank()) {
            return Locale("sv")
        }

        val list = Locale.LanguageRange.parse(acceptLanguage)
        return Locale.lookup(list, LOCALES)
    }

    companion object {
        private val LOCALES = listOf(
            Locale("en"),
            Locale("sv")
        )
    }
}
