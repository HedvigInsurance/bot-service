package com.hedvig.botService.chatv2.structure

import com.hedvig.botService.enteties.UserContext

interface ConversationVisitor {
    val context: UserContext
    val conversationEntries: MutableList<MessageEntry>

    fun visitSerialMessage(message: AbstractSerialMessage)
    fun visitFinalMessage(message: AbstractFinalMessage)
}
