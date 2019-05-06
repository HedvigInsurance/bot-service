package com.hedvig.botService.chatv2.claims

import com.hedvig.botService.chatv2.structure.AbstractSerialMessage
import com.hedvig.botService.chatv2.structure.Message

class StartMessage : AbstractSerialMessage() {
    override fun getNextMessage(): Message = ClaimChatMessage()

    override fun getId(): String = "message.claims.start"

    override fun getBody(): String = "Okej, det här löser vi på nolltid!"
}
