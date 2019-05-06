package com.hedvig.botService.chatv2.structure

abstract class AbstractFinalMessage : Message {
    override fun accept(conversation: ConversationVisitor) {
        conversation.visitFinalMessage(this)
    }
}
