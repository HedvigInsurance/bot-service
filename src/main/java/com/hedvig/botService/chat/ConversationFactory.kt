package com.hedvig.botService.chat


interface ConversationFactory {
    fun createConversation(conversationClass: Class<*>, userLanguage: String?): Conversation

    fun createConversation(conversationClassName: String, userLanguage: String?): Conversation
}
