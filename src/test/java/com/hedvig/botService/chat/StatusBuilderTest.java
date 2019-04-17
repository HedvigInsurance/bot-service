package com.hedvig.botService.chat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static junit.framework.TestCase.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusBuilderTest {

  private StatusBuilderImpl builder;
  @Before
  public void before() {
    builder = new StatusBuilderImpl();
  }

  @Test
  public void fridayMessageShouldReturnExpectedWaitTime() {
    assertEquals("Hedvig svarar inom 15 min", builder.getFridayRetroMeetingTime(36, 45));
    assertEquals("Hedvig svarar inom 30 min", builder.getFridayRetroMeetingTime(24, 45));
    assertEquals("Hedvig svarar inom 10 min", builder.getFridayRetroMeetingTime(40, 45));
    assertEquals("Hedvig svarar inom 5 min", builder.getFridayRetroMeetingTime(45, 45));
    assertEquals("Hedvig svarar inom 10 min", builder.getFridayRetroMeetingTime(42, 45));
    assertEquals("Hedvig svarar inom 50 min", builder.getFridayRetroMeetingTime(4, 45));
  }

  @Test
  public void summerTimeShouldReturnFalseIfNotSummer() {
    boolean resFalse1 = builder.isSummerTime(LocalDate.parse("2019-06-14"));
    boolean resFalse2 = builder.isSummerTime(LocalDate.parse("2019-08-16"));
    assertFalse(resFalse1);
    assertFalse(resFalse2);
  }

  @Test
  public void summerTimeShouldReturnTrueIfSummer() {
    boolean resTrue1 = builder.isSummerTime(LocalDate.parse("2019-06-15"));
    boolean resTrue2 = builder.isSummerTime(LocalDate.parse("2019-08-15"));
    assertTrue(resTrue1);
    assertTrue(resTrue2);
  }

  @Test
  public void summerWaitingTimesToBeAsExpected() {
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(12, DayOfWeek.THURSDAY, 30));
    assertEquals("Hedvig svarar inom 5 min", builder.getSummerWeekdayAnswerTimes(11, DayOfWeek.FRIDAY, 45));
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(9, DayOfWeek.MONDAY, 15));
  }

  @Test
  public void redDayAndWeekendTimesAsExpected() {
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(2));
    assertEquals("Hedvig svarar efter kl. 10", builder.getRedDayAndWeekendAnswerTimes(9));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(10));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(20));
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(23));
  }

}

