package com.hedvig.botService.Utils

import com.hedvig.botService.chat.Conversation.Companion.NOT_VALID_POST_FIX

object MessageUtil {
    fun getBaseMessageId(id: String): String {
        return if (id.matches("^.+?\\d$".toRegex())) {
            id.substring(0, id.lastIndexOf("."))
        } else id
    }

    fun removeNotValidFromId(id: String) =
        id.replace(NOT_VALID_POST_FIX, "")
}
