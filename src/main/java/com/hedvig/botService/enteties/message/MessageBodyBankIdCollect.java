package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.ToString;

@Entity
@DiscriminatorValue("bankidCollect")
@ToString
public class MessageBodyBankIdCollect extends MessageBody {
  public String referenceId;

  public MessageBodyBankIdCollect() {}

  public MessageBodyBankIdCollect(String referenceId) {
    super("");
    this.referenceId = referenceId;
  }

  @Override
  public void render(UserContext userContext) {
    this.referenceId = userContext.replaceWithContext(this.referenceId);
    super.render(userContext);
  }
}
