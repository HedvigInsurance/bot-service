package com.hedvig.botService.chatv2.structure

import com.hedvig.botService.enteties.UserContext
import java.util.*

class ConversationVisitorImpl(
    private val userContext: UserContext,
    private val conversation: MutableList<MessageEntry>
) : ConversationVisitor {
    override fun getContext(): UserContext = userContext

    override fun getConversationEntries(): MutableList<MessageEntry> = conversation

    override fun visitSerialMessage(message: AbstractSerialMessage) {
        getConversationEntries().add(
            MessageEntry(
                UUID.randomUUID(),
                message.getId(),
                message.getBody()
            )
        )
    }

    override fun visitFinalMessage(message: AbstractFinalMessage) {
        getConversationEntries().add(
            MessageEntry(
                UUID.randomUUID(),
                message.getId(),
                message.getBody()
            )
        )
    }
}
