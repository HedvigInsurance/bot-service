package com.hedvig.botService.chatv2.claims

import com.hedvig.botService.chatv2.structure.AbstractSerialMessage

class ClaimChatMessage : AbstractSerialMessage() {
    override val nextMessage = FinalClamsConversationMessage()

    override val id = "message.claims.chat"

    override val body = "Du ska få berätta vad som hänt genom att spela in ett röstmeddelande"
}
