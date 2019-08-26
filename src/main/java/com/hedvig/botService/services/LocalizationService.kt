package com.hedvig.botService.services

import com.hedvig.botService.enteties.localization.LocalizationData
import com.hedvig.botService.enteties.localization.LocalizationResponse
import okhttp3.*
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional


@Component
@Transactional
class LocalizationService {

    private var localizationData: LocalizationData? = null

    fun refreshLocalizations() {
        localizationData = fetchLocalizations()
    }

    fun getText(locale: Locale, key: String): String? {
        val language = parseLanguage(locale)
        localizationData?.let { data ->
            return data.languages.firstOrNull { it.code == language }?.translations?.firstOrNull { it.key.value == key }
                ?.text
        }
        return null
    }

    private fun parseLanguage(locale: Locale): String {
        return when (locale.isO3Language) {
            "en" -> "en_SE"
            "sv" -> "sv_SE"
            else -> "sv_SE"
        }
    }

    private fun fetchLocalizations(): LocalizationData? {

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api-euwest.graphcms.com/v1/cjmawd9hw036a01cuzmjhplka/master")
            .header("Accept", "application/json")
            .post(
                RequestBody.create(
                    MediaType.get("application/json"),
                    """{"query": "{\nlanguages {\ntranslations(where: { project: BotService }) {\ntext\nkey {\nvalue\n}\n}\ncode\n}\n}","variables": null}"""
                )
            )
            .build()
        val result = client.newCall(request).execute().body()?.string()
            ?: throw Error("Got no data from graphql endpoint")

        val response = LocalizationResponse.fromJson(result) ?: throw Error("Failed to parse body: $result")

        return response.data

    }
}

