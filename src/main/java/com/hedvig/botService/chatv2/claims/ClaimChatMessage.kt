package com.hedvig.botService.chatv2.claims

import com.hedvig.botService.chatv2.structure.AbstractSerialMessage
import com.hedvig.botService.chatv2.structure.Message

class ClaimChatMessage : AbstractSerialMessage() {
    override fun getNextMessage(): Message = FinalClamsConversationMessage()

    override fun getId(): String = "message.claims.chat"

    override fun getBody(): String = "Du ska få berätta vad som hänt genom att spela in ett röstmeddelande"
}
