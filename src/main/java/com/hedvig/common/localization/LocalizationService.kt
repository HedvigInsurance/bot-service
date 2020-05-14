package com.hedvig.common.localization

import java.util.*

interface LocalizationService {
    fun getTranslation(key: String, locale: Locale): String?
}

