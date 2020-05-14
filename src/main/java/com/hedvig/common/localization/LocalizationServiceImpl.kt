package com.hedvig.common.localization

import com.hedvig.lokalise.client.LokaliseClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocalizationServiceImpl(
    @Value("\${lokalise.projectId}")
    private val projectId: String,
    @Value("\${lokalise.apiToken}")
    private val apiToken: String
) : LocalizationService {

    val client = LokaliseClient(
        projectId,
        apiToken
    )

    override fun getTranslation(key: String, locale: Locale) =
        client.getTranslation(key, locale)
}
