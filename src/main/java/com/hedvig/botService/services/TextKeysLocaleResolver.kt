package com.hedvig.botService.services

import org.slf4j.LoggerFactory
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

        return try {
            val list = Locale.LanguageRange.parse(acceptLanguage)
            Locale.lookup(list, LOCALES) ?: DEFAULT_LOCALE
        } catch (e: IllegalArgumentException) {
            log.error("IllegalArgumentException when parsing acceptLanguage: '$acceptLanguage' message: ${e.message}")
            DEFAULT_LOCALE
        }
    }

    companion object {
        private val LOCALES = listOf(
            Locale("en"),
            Locale("sv")
        )

        val DEFAULT_LOCALE = Locale("sv")
        val log = LoggerFactory.getLogger(this::class.java)
    }
}
