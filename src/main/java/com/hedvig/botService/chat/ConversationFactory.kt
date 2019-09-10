package com.hedvig.botService.chat

import java.util.*


interface ConversationFactory {
    fun createConversation(conversationClass: Class<*>, userLocale: Locale?): Conversation

    fun createConversation(conversationClassName: String, userLocale: Locale?): Conversation
}
