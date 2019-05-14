package com.hedvig.botService.services

import com.hedvig.botService.enteties.message.Message
import com.hedvig.botService.services.events.MessageSentEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class RedisMessagePublisherImpl(private val redisTemplate: RedisTemplate<String, Any>) : RedisMessagePublisher {

    private val logger = LoggerFactory.getLogger(RedisMessagePublisherImpl::class.java)

    override fun publish(memberId: String, message: Message) {
        logger.info(
            "Publish message to Redis with topic {} and message {}",
            String.format("%s.%s", TOPIC_PREFIX, memberId), message
        )

        redisTemplate.convertAndSend(String.format("%s.%s", TOPIC_PREFIX, memberId), message)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onMessageSent(e: MessageSentEvent) {
        this.publish(e.memberId, e.message)
    }

    companion object {
        private val TOPIC_PREFIX = "MESSAGE_SENT"
    }
}
