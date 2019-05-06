package com.hedvig.botService.chatv2.structure

abstract class AbstractSerialMessage : Message {
    abstract fun getNextMessage(): Message

    override fun accept(conversation: ConversationVisitor) {
        conversation.visitSerialMessage(this)
        getNextMessage().accept(conversation)
    }
}
