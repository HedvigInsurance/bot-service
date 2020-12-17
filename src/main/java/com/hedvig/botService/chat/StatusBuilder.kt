package com.hedvig.botService.chat

import com.hedvig.botService.utils.DateUtil
import java.time.LocalDateTime
import java.util.Locale

interface StatusBuilder {
    fun getStatusReplyMessage(now: LocalDateTime = DateUtil.nowInStockholm(), locale: Locale): String
}
