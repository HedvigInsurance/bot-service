package com.hedvig.botService.Utils

object MessageUtil {
    fun getBaseMessageId(id: String): String {
        return if (id.matches("^.+?\\d$".toRegex())) {
            id.substring(0, id.lastIndexOf("."))
        } else id
    }
}
