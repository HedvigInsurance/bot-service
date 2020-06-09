package com.hedvig.common.localization

import java.util.*

interface LokaliseClientWrapper {
    fun getTranslation(key: String, locale: Locale): String?
}
