package com.hedvig.botService.serviceIntegration.ticketService

interface TicketService {
    fun createCallMeTicket(memberId: String, phoneNumber: String,  firstName: String, lastName: String)
}
