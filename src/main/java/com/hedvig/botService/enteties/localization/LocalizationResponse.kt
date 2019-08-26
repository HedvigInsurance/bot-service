package com.hedvig.botService.enteties.localization

import com.google.gson.Gson

data class LocalizationResponse(
    val data: LocalizationData
) {
    companion object {
        fun fromJson(json: String) = Gson().fromJson<LocalizationResponse>(json, LocalizationResponse::class.java)
    }
}
