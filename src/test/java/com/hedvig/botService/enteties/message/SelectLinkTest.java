package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import org.junit.Before;
import org.junit.Test;

public class SelectLinkTest {

  UserContext userContext;

  @Before
  public void setup() {
    userContext = new UserContext();
  }

  @Test
  public void render_will_ignore_null_links() {

    SelectLink link = new SelectLink("text", null, null, null, null, false);

    link.render(userContext);
  }
}
