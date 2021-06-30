package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.libs.translations.Translations;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("bankidCollect")
@ToString
public class MessageBodyBankIdCollect extends MessageBody {
  public String referenceId;

  public MessageBodyBankIdCollect() {
  }

  public MessageBodyBankIdCollect(String referenceId) {
    super("");
    this.referenceId = referenceId;
  }

  @Override
  public void render(String globalId, Boolean fromUser, UserContext userContext, Translations translations) {
    this.referenceId = userContext.replaceWithContext(this.referenceId);
    super.render(globalId, fromUser, userContext, translations);
  }
}
