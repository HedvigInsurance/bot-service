package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import lombok.ToString;

@ToString
public class SelectItemTrustly extends SelectItem {

  static final long serialVersionUID = 1L;

  public String id;

  public SelectItemTrustly(String text, String value) {
    super(false, text, value);
  }

  public SelectItemTrustly(String text, String value, Boolean selected, String id) {
    super(selected, text, value);
    this.id = id;
  }

  public SelectItemTrustly() {}

  @Override
  public void render(UserContext userContext) {
    super.render(userContext);

    this.id = userContext.replaceWithContext(UserContext.TRUSTLY_TRIGGER_ID);
  }
}
