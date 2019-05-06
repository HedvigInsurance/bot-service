package com.hedvig.botService.chatv2.structure

import com.hedvig.botService.enteties.UserContext

interface ConversationVisitor {
    fun visitSerialMessage(message: AbstractSerialMessage)
    fun getContext(): UserContext
    fun getConversationEntries(): MutableList<MessageEntry>
    fun visitFinalMessage(message: AbstractFinalMessage)
}
