package com.hedvig.botService.chatv2.structure

import com.hedvig.botService.enteties.UserContext
import java.util.*

class ConversationVisitorImpl(
    override val context: UserContext,
    override val conversationEntries: MutableList<MessageEntry>
) : ConversationVisitor {

    override fun visitSerialMessage(message: AbstractSerialMessage) {
        conversationEntries.add(
            MessageEntry(
                UUID.randomUUID(),
                message.id,
                message.body
            )
        )
    }

    override fun visitFinalMessage(message: AbstractFinalMessage) {
        conversationEntries.add(
            MessageEntry(
                UUID.randomUUID(),
                message.id,
                message.body
            )
        )
    }
}
