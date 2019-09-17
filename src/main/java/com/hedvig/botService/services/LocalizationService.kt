package com.hedvig.botService.services

import com.hedvig.botService.enteties.localization.LocalizationData
import com.hedvig.botService.serviceIntegration.localization.LocalizationClient
import com.hedvig.botService.serviceIntegration.localization.GraphQLQueryWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional
import kotlin.concurrent.timer


@Component
class LocalizationService @Autowired constructor(val localizationClient: LocalizationClient) {

    private var localizationData: LocalizationData? = null

    init {
        timer("refreshLocalizations", false, 0, TEN_MINUTES_MS) {
            this@LocalizationService.refreshLocalizations()
        }
    }

    fun refreshLocalizations() {
        fetchLocalizations()?.let { data ->
            if (data != localizationData) {
                synchronized(this@LocalizationService) {
                    localizationData = data
                }
            }
        }
    }

    fun getText(locale: Locale?, key: String): String? {
        val language = parseLanguage(locale)
        localizationData?.let { data ->
            return data.getText(language, key)
        } ?: run {
            // Let's retry
            refreshLocalizations()
            return localizationData?.getText(language, key)
        }
    }

    private fun parseLanguage(locale: Locale?): String {
        return when {
            locale.isLanguage("en") -> "en_SE"
            locale.isLanguage("sv") -> "sv_SE"
            else -> "sv_SE"
        }
    }

    private fun Locale?.isLanguage(language: String) =
        this?.language.equals(Locale(language).language)

    private fun fetchLocalizations(): LocalizationData? =
        localizationClient.fetchLocalization(GRAPHCMS_TEXT_KEYS_QUERY).data

    private fun LocalizationData.getText(language: String, key: String) =
        languages
            .firstOrNull { it.code == language }
            ?.translations
            ?.firstOrNull { it.key?.value == key }
            ?.text

    companion object {
        val GRAPHCMS_TEXT_KEYS_QUERY = GraphQLQueryWrapper(
            "{languages {translations(where: { project: BotService }) {text key {value}} code}}"
        )

        private const val TEN_MINUTES_MS = 600000L
    }
}
