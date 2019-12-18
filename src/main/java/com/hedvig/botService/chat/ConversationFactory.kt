package com.hedvig.botService.chat

import com.hedvig.botService.enteties.UserContext


interface ConversationFactory {
    fun createConversation(conversationClass: Class<*>, userContext: UserContext): Conversation

    fun createConversation(conversationClassName: String, userContext: UserContext): Conversation
}
