package com.hedvig.common.localization

import org.springframework.stereotype.Component
import java.util.*

@Component
class LocalizationServiceImpl(
    private val client: LokaliseClientWrapper
) : LocalizationService {

    override fun getTranslation(key: String, locale: Locale) = client.getTranslation(key, locale)?.let { translation ->
        var mutableTranslation = translation
        lokalisePlaceholderSyntaxMatcher.findAll(translation).forEach { matchResult ->
            mutableTranslation = lokalisePlaceholderSyntaxMatcher.replaceFirst(mutableTranslation, "{${matchResult.groups[1]?.value}}")
        }
        mutableTranslation
    }

    companion object {
        private val lokalisePlaceholderSyntaxMatcher = Regex("\\[\\%\\d+\\\$s\\:(.+?)\\]")
    }
}

