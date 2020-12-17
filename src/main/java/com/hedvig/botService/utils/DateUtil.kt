package com.hedvig.botService.utils

import java.time.LocalDateTime
import java.time.ZoneId

object DateUtil {
    private val STOCKHOLM_ZONE_ID = ZoneId.of("Europe/Stockholm")
    fun nowInStockholm(): LocalDateTime = LocalDateTime.now(STOCKHOLM_ZONE_ID)
}
