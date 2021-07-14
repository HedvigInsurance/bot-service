package com.hedvig.botService.serviceIntegration.slackService

import com.hedvig.botService.serviceIntegration.slackService.dto.SlackData
import com.hedvig.botService.services.events.HouseUnderwritingLimitCallMeExceedsEvent
import com.hedvig.botService.services.events.OnboardingCallForQuoteEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("slack")
class SlackService @Autowired
internal constructor(
    private val slackServiceClient: SlackServiceClient,
    private val slackUnderwritingServiceClient: SlackUnderwritingServiceClient
) {

    @EventListener
    fun on(event: OnboardingCallForQuoteEvent) {
        val slackChannel = "#call_member_for_quote"

        val message =
            "Potential member ${event.memberId} - ${event.firstName} ${event.lastName} tried to sign-up, give them a call on ${event.phoneNumber}"

        try {
            val slackData = SlackData(
                slackChannel,
                message
            )

            slackServiceClient.sendNotification(slackData)
        } catch (e: Exception) {
            logger.error("Cannot send notification to slack channel {} with message {}", slackChannel, message)
        }

    }

    @EventListener
    fun on(event: HouseUnderwritingLimitCallMeExceedsEvent) {
        val slackChannel = "#call_member_underwriting_guidelines"

        val message =
            "Member ${event.memberId} - ${event.firstName} ${event.lastName} house exceeds underwriting guidelines" +
              " on reason [${event.reason}], call the member on ${event.phoneNumber}"

        try {
            val slackData = SlackData(
                slackChannel,
                message
            )

            slackUnderwritingServiceClient.sendNotification(slackData)
        } catch (e: Exception) {
            logger.error("Cannot send notification to slack channel {} with message {}", slackChannel, message)
        }

    }

    private val logger = LoggerFactory.getLogger(javaClass)
}
