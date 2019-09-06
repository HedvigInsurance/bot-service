package com.hedvig.botService.services

import org.springframework.stereotype.Component
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional

@Component
class TextKeysLocaleResolver {

    fun resolveLocale(acceptLanguage: String?): Locale {
        if (acceptLanguage.isNullOrBlank()) {
            return DEFAULT_LOCALE
        }

        val list = Locale.LanguageRange.parse(acceptLanguage)
        return Locale.lookup(list, LOCALES) ?: DEFAULT_LOCALE
    }

    companion object {
        private val LOCALES = listOf(
            Locale("en"),
            Locale("sv")
        )

        val DEFAULT_LOCALE = Locale("sv")
    }
}
