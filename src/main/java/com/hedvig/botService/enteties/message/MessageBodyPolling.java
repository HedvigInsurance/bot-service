package com.hedvig.botService.enteties.message;

/*
 * This message is put on in the chat so the client can start polling for new messages.
 * Is does this until a new message is fetched.
 * */

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.ToString;

@Entity
@DiscriminatorValue("polling")
@ToString
public class MessageBodyPolling extends MessageBody {

  public MessageBodyPolling() {}
}
