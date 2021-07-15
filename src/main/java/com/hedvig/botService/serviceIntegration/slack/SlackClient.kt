package com.hedvig.botService.serviceIntegration.slack

import com.hedvig.botService.config.FeignConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "SlackClient",
    url = "\${hedvig.notifications.slackUrl}",
    configuration = [FeignConfiguration::class]
)
@ConditionalOnProperty("hedvig.notifications.slackUrl")
interface SlackClient {
    @PostMapping
    fun post(@RequestBody body: SlackMessage)
}

data class SlackMessage(
    val text: String,
    val channel: String,
    val username: String,
    val icon_emoji: String
)
