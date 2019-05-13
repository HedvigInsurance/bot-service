package com.hedvig.botService.services.events

import com.hedvig.botService.enteties.message.Message
import lombok.Value

@Value
data class MessageSentEvent(
  val memberId: String,
  val message: Message
)
