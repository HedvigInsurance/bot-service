package com.hedvig.botService.serviceIntegration.ticketService;

import com.hedvig.botService.serviceIntegration.ticketService.dto.TicketDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
public class TicketServiceImpl implements  TicketService {
  private TicketServiceClient ticketServiceclient;

  @Autowired
  public TicketServiceImpl ( TicketServiceClient c ) {
    this.ticketServiceclient = c;
  }

  @Override
  public void createNewTicket (TicketDto ticket ) {
    try {
      ResponseEntity response = ticketServiceclient.createNewTicket( ticket ) ;

    } catch (RestClientResponseException e ){
      log.info("Error when posting a 'Create New Ticket' request to ticket-service:" + e);
    }
  }
}
