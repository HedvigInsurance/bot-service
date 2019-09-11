package com.hedvig.botService.Utils

object ConversationUtils {

    fun getSplitFromIndex(text: String?, index: Int?): String? {
        val paragraphs = text?.split("\u000C".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        val numberOFParagaphs = paragraphs?.size ?: 0

        return when {
            numberOFParagaphs <= 1 -> text
            index == null -> ""
            index % 2 == 0 -> paragraphs?.get(index/2)
            index % 2 == 1 -> ""
            else -> text
        }
    }

    fun getSplitIndexFromText(text: String): Int? {
        return if (text.matches("^.+\\.\\d\$".toRegex())) {
            text.substring(text.lastIndexOf(".") + 1, text.length).toInt()
        } else null
    }
}
