package com.hedvig.botService.chatv2.claims

import com.hedvig.botService.chatv2.structure.AbstractSerialMessage

class StartMessage : AbstractSerialMessage() {
    override val nextMessage = ClaimChatMessage()

    override val id = "message.claims.start"

    override val body = "Okej, det här löser vi på nolltid!"
}
