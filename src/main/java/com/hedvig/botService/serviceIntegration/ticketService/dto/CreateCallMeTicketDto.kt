package com.hedvig.botService.serviceIntegration.ticketService.dto

data class CreateCallMeTicketDto(
    val createdBy: String,
    val memberId: String,
    val phoneNumber: String,
    val firstName: String?,
    val lastName: String?
)
