package com.hedvig.botService.services

import com.hedvig.botService.enteties.message.Message

interface RedisMessagePublisher {
    fun publish(memberId: String, message: Message)
}
