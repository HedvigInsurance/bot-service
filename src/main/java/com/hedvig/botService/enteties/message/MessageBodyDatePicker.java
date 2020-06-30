package com.hedvig.botService.enteties.message;

import java.time.LocalDateTime;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("datePicker")
@ToString
public class MessageBodyDatePicker extends MessageBody {
  private static Logger log = LoggerFactory.getLogger(MessageBodyDatePicker.class);

  public LocalDateTime date;

  public MessageBodyDatePicker(String content, LocalDateTime date) {
    super(content);
    this.date = date;
  }

  MessageBodyDatePicker() {
    log.debug("Instansiating MessageBodyDatePicker");
  }
}
