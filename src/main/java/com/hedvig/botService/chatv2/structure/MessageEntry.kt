package com.hedvig.botService.chatv2.structure

import java.util.*

data class MessageEntry(
    val id: UUID,
    val messageId: String,
    val body: String
)
