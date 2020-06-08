package com.hedvig.common.localization

import com.hedvig.lokalise.client.LokaliseClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocalizationServiceImpl(
    @Value("\${lokalise.useFakes}")
    private val useFakes: Boolean,
    private val configuration: LokaliseConfigurationProperties
) : LocalizationService {

    val client = if (!useFakes) LokaliseClient(configuration.projectId, configuration.apiToken) else null

    override fun getTranslation(key: String, locale: Locale) =
        if (!useFakes) client!!.getTranslation(key, locale) else "lokalise configuration useFakes is set to true"
}
