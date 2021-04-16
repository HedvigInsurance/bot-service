package com.hedvig.common.localization

import com.hedvig.lokalise.repository.LokaliseRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class LokaliseClientWrapperImpl(
    @Value("\${lokalise.useFakes}")
    private val useFakes: Boolean,
    private val configuration: LokaliseConfigurationProperties
) : LokaliseClientWrapper {
    val client = if (!useFakes) LokaliseRepository(
        configuration.projectId,
        configuration.apiToken
    ) else null

    override fun getTranslation(key: String, locale: Locale) =
        if (!useFakes) client!!.getTranslation(key, locale) else "lokalise configuration useFakes is set to true"
}
