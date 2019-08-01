package com.hedvig.botService.serviceIntegration.ticketService;

import com.hedvig.botService.serviceIntegration.ticketService.dto.TicketDto;
import org.springframework.stereotype.Service;

@Service
public interface TicketService {

  void createNewTicket (TicketDto ticket );

}
