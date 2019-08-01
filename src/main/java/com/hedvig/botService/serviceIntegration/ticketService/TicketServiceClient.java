package com.hedvig.botService.serviceIntegration.ticketService;

import com.hedvig.botService.serviceIntegration.ticketService.dto.TicketDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ticketServiceClient", url = "${hedvig.ticket-service.url:ticket-service}")
public interface TicketServiceClient {

  @PostMapping( value="/_/tickets/new/")
  void createNewTicket (@RequestBody TicketDto ticket ) ;

}
