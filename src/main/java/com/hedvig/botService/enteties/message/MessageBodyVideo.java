package com.hedvig.botService.enteties.message;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("video")
@ToString
public class MessageBodyVideo extends MessageBody {

  public String URL;
  private static Logger log = LoggerFactory.getLogger(MessageBodyVideo.class);

  public MessageBodyVideo(String content, String URL) {
    super(content);
    this.URL = URL;
  }

  MessageBodyVideo() {
    log.info("Instansiating MessageBodyVideo");
  }
}
