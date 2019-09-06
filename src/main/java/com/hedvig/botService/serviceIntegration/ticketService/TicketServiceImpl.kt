package com.hedvig.botService.serviceIntegration.ticketService

import com.hedvig.botService.serviceIntegration.ticketService.dto.CreateCallMeTicketDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TicketServiceImpl @Autowired constructor(
    private val ticketServiceClient: TicketServiceClient
): TicketService {
    override fun createCallMeTicket(memberId: String, phoneNumber: String, firstName: String, lastName: String) {
        this.ticketServiceClient.createCallMeTicket(
            CreateCallMeTicketDto(
                createdBy = "bot-service",
                memberId = memberId,
                phoneNumber = phoneNumber,
                firstName = firstName,
                lastName = lastName
            )
        )
    }
}
