package com.hedvig.botService.chat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;

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
    boolean resFalse1 = builder.isSummerTime(LocalDate.parse("2019-06-12"));
    boolean resFalse2 = builder.isSummerTime(LocalDate.parse("2019-08-20"));
    assertFalse(resFalse1);
    assertFalse(resFalse2);
  }

  @Test
  public void summerTimeShouldReturnTrueIfSummer() {
    boolean resTrue1 = builder.isSummerTime(LocalDate.parse("2019-06-21"));
    boolean resTrue2 = builder.isSummerTime(LocalDate.parse("2019-08-11"));
    assertTrue(resTrue1);
    assertTrue(resTrue2);
  }

  @Test
  public void summerWaitingTimesToBeAsExpected() {
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(12,30, LocalDate.parse("2019-07-04")));
    assertEquals("Hedvig svarar inom 5 min", builder.getSummerWeekdayAnswerTimes(11, 45, LocalDate.parse("2019-07-05")));
    assertEquals("Hedvig svarar imorgon", builder.getSummerWeekdayAnswerTimes(15, 00, LocalDate.parse("2019-06-28")));
  }

  @Test
  public void returnHedvigSvararMåndagAfter6pmIfNextDayIsNonWorkingDay() {
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekdayAnswerTimes(19, 00, LocalDate.parse("2019-07-26")));
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekdayAnswerTimes(18, 00, LocalDate.parse("2019-08-02")));
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekdayAnswerTimes(18, 30, LocalDate.parse("2019-06-21")));
  }

  @Test
  public void returnUsualAnswerTimesBefore6pmDayBeforeNonWorkingWeekend() {
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(9, 00, LocalDate.parse("2019-07-26")));
    assertEquals("Hedvig svarar efter kl. 9", builder.getSummerWeekdayAnswerTimes(8, 30, LocalDate.parse("2019-08-02")));
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(12, 00, LocalDate.parse("2019-08-02")));
    assertEquals("Hedvig svarar inom 15 min", builder.getSummerWeekdayAnswerTimes(12, 30, LocalDate.parse("2019-06-21")));
  }

  @Test
  public void redDayAndWeekendTimesAsExpected() {
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(2, LocalDate.parse("2019-07-05")));
    assertEquals("Hedvig svarar efter kl. 10", builder.getRedDayAndWeekendAnswerTimes(9, LocalDate.parse("2019-07-05")));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(10, LocalDate.parse("2019-07-05")));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(20, LocalDate.parse("2019-07-05")));
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(23, LocalDate.parse("2019-07-05")));
  }

  @Test
  public void returnHedvigSvararMåndagWhenNonWorkingDay() {
    assertEquals("Hedvig svarar på Måndag", builder.getRedDayAndWeekendAnswerTimes(2, LocalDate.parse("2019-07-13")));
    assertEquals("Hedvig svarar på Måndag", builder.getRedDayAndWeekendAnswerTimes(19, LocalDate.parse("2019-06-22")));
    assertEquals("Hedvig svarar på Måndag", builder.getRedDayAndWeekendAnswerTimes(11, LocalDate.parse("2019-06-22")));
  }
}

