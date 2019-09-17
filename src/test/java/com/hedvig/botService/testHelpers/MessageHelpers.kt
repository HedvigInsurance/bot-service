package com.hedvig.botService.testHelpers

import com.hedvig.botService.chat.ConversationTest.Companion.TESTMESSAGE_ID

import com.hedvig.botService.enteties.message.Message
import com.hedvig.botService.enteties.message.MessageBody
import com.hedvig.botService.enteties.message.MessageBodySingleSelect
import com.hedvig.botService.enteties.message.MessageBodyText
import com.hedvig.botService.enteties.message.MessageHeader
import com.hedvig.botService.enteties.message.SelectItem
import java.util.Arrays

object MessageHelpers {

    fun createSingleSelectMessage(text: String, fromHedvig: Boolean = false, vararg items: SelectItem): Message {
        Arrays.asList(*items)

        return createMessage(MessageBodySingleSelect(text, Arrays.asList(*items)), fromHedvig)
    }

    fun createTextMessage(text: String, fromHedvig: Boolean = false): Message {
        return createMessage(MessageBodyText(text), fromHedvig)
    }

    fun createMessage(body: MessageBody, fromHedvig: Boolean = false): Message {
        val m = Message()
        m.id = TESTMESSAGE_ID
        m.globalId = 1
        m.header = MessageHeader()
        m.header.fromId = if (fromHedvig) 1L else -1L
        m.header.messageId = 1
        m.body = body
        return m
    }
}
