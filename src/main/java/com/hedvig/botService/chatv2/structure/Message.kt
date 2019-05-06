package com.hedvig.botService.chatv2.structure

interface Message {
    fun getId(): String
    fun getBody(): String

    fun accept(conversation: ConversationVisitor)
}
