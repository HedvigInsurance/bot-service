package com.hedvig.botService.serviceIntegration.ticketService;

import com.hedvig.botService.serviceIntegration.ticketService.dto.TicketDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
public class TicketService  {
  private TicketServiceClient ticketServiceclient;

  @Autowired
  public TicketService ( TicketServiceClient c ) {
    this.ticketServiceclient = c;
  }

  public void createNewTicket (TicketDto ticket ) {
    try {
      ticketServiceclient.createNewTicket( ticket ) ;

    } catch (RestClientResponseException e ){
      log.info("Error when posting a 'Create New Ticket' request to ticket-service:" + e);
    }
  }
}
