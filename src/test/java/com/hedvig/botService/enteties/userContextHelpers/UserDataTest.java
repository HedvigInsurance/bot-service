package com.hedvig.botService.enteties.userContextHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedvig.botService.enteties.UserContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UserDataTest {

  @Test
  public void testsetAddressCity() {
    UserContext context = new UserContext();

    UserData ud = context.getOnBoardingData();

    ud.setAddressCity("Sollefteå");

    assertThat(ud.getAddressCity()).isEqualTo("Sollefteå");
  }
}
