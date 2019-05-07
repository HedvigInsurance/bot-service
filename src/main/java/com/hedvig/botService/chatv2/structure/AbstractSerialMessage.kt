package com.hedvig.botService.chatv2.structure

abstract class AbstractSerialMessage : Message {
    abstract val nextMessage: Message

    override fun accept(conversation: ConversationVisitor) {
        conversation.visitSerialMessage(this)
        nextMessage.accept(conversation)
    }
}
