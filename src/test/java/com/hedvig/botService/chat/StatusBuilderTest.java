package com.hedvig.botService.chat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusBuilderTest {

  @Test
  public void fridayMessageShouldReturnExpectedWaitTime() {
  StatusBuilderImpl fridayMessage = new StatusBuilderImpl();

    assertEquals("Hedvig svarar inom 15 min", fridayMessage.getFridayRetroMeetingTime(36, 45));
    assertEquals("Hedvig svarar inom 30 min", fridayMessage.getFridayRetroMeetingTime(24, 45));
    assertEquals("Hedvig svarar inom 10 min", fridayMessage.getFridayRetroMeetingTime(40, 45));
    assertEquals("Hedvig svarar inom 5 min", fridayMessage.getFridayRetroMeetingTime(45, 45));
    assertEquals("Hedvig svarar inom 10 min", fridayMessage.getFridayRetroMeetingTime(42, 45));
    assertEquals("Hedvig svarar inom 50 min", fridayMessage.getFridayRetroMeetingTime(4, 45));
  }

  @Test
  public void redDayShouldReturnTrueIfRedDay() {
    StatusBuilderImpl instance = new StatusBuilderImpl();
    boolean resTrue = instance.redDay("2019-01-01");
    assertTrue(resTrue);
  }

  @Test
  public void redDayShouldReturnFalseIfNotRedDay() {
    StatusBuilderImpl test = new StatusBuilderImpl();
    boolean resFalse = test.redDay("2019-02-01");
    assertFalse(resFalse);
  }

  @Test
  public void summerTimeShouldReturnFalseIfNotSummer() {
    StatusBuilderImpl test = new StatusBuilderImpl();
    boolean resFalse1 = test.summerTime("2019-06-14");
    boolean resFalse2 = test.summerTime("2019-08-16");
    assertFalse(resFalse1);
    assertFalse(resFalse2);
  }

  @Test
  public void summerTimeShouldReturnTrueIfSummer() {
    StatusBuilderImpl test = new StatusBuilderImpl();
    boolean resTrue1 = test.summerTime("2019-06-15");
    boolean resTrue2 = test.summerTime("2019-08-15");
    assertTrue(resTrue1);
    assertTrue(resTrue2);
  }

  @Test
  public void summerWaitingTimesToBeAsExpected() {
    StatusBuilderImpl summer = new StatusBuilderImpl();
    assertEquals("Hedvig svarar inom 15 min", summer.getSummerWeekdayAnswerTimes(12, "THURSDAY", 30));
    assertEquals("Hedvig svarar inom 5 min", summer.getSummerWeekdayAnswerTimes(11, "FRIDAY", 45));
    assertEquals("Hedvig svarar inom 15 min", summer.getSummerWeekdayAnswerTimes(9, "MONDAY", 15));
  }

  @Test
  public void redDayAndWeekendTimesAsExpected() {
    StatusBuilderImpl redDay = new StatusBuilderImpl();
    assertEquals("Hedvig svarar imorgon", redDay.getRedDayAndWeekendAnswerTimes(2));
    assertEquals("Hedvig svarar efter kl. 10", redDay.getRedDayAndWeekendAnswerTimes(9));
    assertEquals("Hedvig svarar inom en timme", redDay.getRedDayAndWeekendAnswerTimes(10));
    assertEquals("Hedvig svarar inom en timme", redDay.getRedDayAndWeekendAnswerTimes(20));
    assertEquals("Hedvig svarar imorgon", redDay.getRedDayAndWeekendAnswerTimes(23));
  }

}

