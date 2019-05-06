package com.hedvig.botService.chatv2.claims

import com.hedvig.botService.chatv2.structure.AbstractFinalMessage

class FinalClamsConversationMessage : AbstractFinalMessage() {
    override fun getId(): String = "an.id"

    override fun getBody(): String = "Ok that's it"
}
