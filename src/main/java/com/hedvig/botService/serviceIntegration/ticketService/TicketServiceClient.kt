package com.hedvig.botService.serviceIntegration.ticketService

import com.hedvig.botService.serviceIntegration.ticketService.dto.CreateCallMeTicketDto
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "ticketServiceClient", url = "\${hedvig.ticket-service.url:ticket-service}")
interface TicketServiceClient {
    @PostMapping
    fun createCallMeTicket(@RequestBody createCallMeTicketRequest: CreateCallMeTicketDto)
}
