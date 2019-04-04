package com.hedvig.botService.chat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StatusBuilderTest {

  @Test
  public void stringReturnedShouldReturnExpectedWaitTime() {
    StatusBuilderImpl fridayMessage = new StatusBuilderImpl();

    assertEquals("Hedvig svarar inom 15 min", fridayMessage.getFridayRetroMeetingTime(36, 45));
    assertEquals("Hedvig svarar inom 30 min", fridayMessage.getFridayRetroMeetingTime(24, 45));
    assertEquals("Hedvig svarar inom 10 min", fridayMessage.getFridayRetroMeetingTime(40, 45));
    assertEquals("Hedvig svarar inom 5 min", fridayMessage.getFridayRetroMeetingTime(45, 45));
    assertEquals("Hedvig svarar inom 10 min", fridayMessage.getFridayRetroMeetingTime(42, 45));
    assertEquals("Hedvig svarar inom 50 min", fridayMessage.getFridayRetroMeetingTime(4, 45));
  }

}

