package com.hedvig.botService.chatv2.structure

interface Message {
    val id: String
    val body: String

    fun accept(conversation: ConversationVisitor)
}
