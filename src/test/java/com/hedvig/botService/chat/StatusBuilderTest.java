package com.hedvig.botService.chat;
import com.hedvig.common.localization.LocalizationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

import static junit.framework.TestCase.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusBuilderTest {

  private final Locale DEFAULT_LOCALE = new Locale("sv-SE");
  private static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

  private StatusBuilderImpl builder;

  @Mock
  private LocalizationService localizationService;

  @Before
  public void before() {
    builder = new StatusBuilderImpl(localizationService);
  }

  @Test
  public void fridayMessageShouldReturnExpectedWaitTime() {

    assertEquals("Hedvig svarar inom 15 min", builder.getFridayRetroMeetingTime(36, 45, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 30 min", builder.getFridayRetroMeetingTime(24, 45, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 10 min", builder.getFridayRetroMeetingTime(40, 45, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 5 min", builder.getFridayRetroMeetingTime(45, 45, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 10 min", builder.getFridayRetroMeetingTime(42, 45, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 50 min", builder.getFridayRetroMeetingTime(4, 45, DEFAULT_LOCALE));
  }

  @Test
  public void summerTimeShouldReturnFalseIfNotSummer() {
    boolean resFalse1 = builder.isSummerTime(LocalDate.of(currentYear, 6, 12));
    boolean resFalse2 = builder.isSummerTime(LocalDate.of(currentYear, 8, 20));
    assertFalse(resFalse1);
    assertFalse(resFalse2);
  }

  @Test
  public void summerTimeShouldReturnTrueIfSummer() {
    boolean resTrue1 = builder.isSummerTime(LocalDate.of(currentYear, 6, 21));
    boolean resTrue2 = builder.isSummerTime(LocalDate.of(currentYear, 8, 11));
    assertTrue(resTrue1);
    assertTrue(resTrue2);
  }

  @Test
  public void christmasAnsweringTimesToBeAsExcepted() {
    assertEquals("Hedvig svarar inom 10 min", builder.getChristmasPeriodAnswerTimes(10, LocalDate.parse("2019-12-23"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar efter kl. 10", builder.getChristmasPeriodAnswerTimes(8, LocalDate.parse("2019-12-23"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar imorgon", builder.getChristmasPeriodAnswerTimes(20, LocalDate.parse("2019-12-23"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 10 min", builder.getChristmasPeriodAnswerTimes(16, LocalDate.parse("2019-12-23"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom en halvtimme", builder.getChristmasPeriodAnswerTimes(16, LocalDate.parse("2019-12-26"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom 20 min", builder.getChristmasPeriodAnswerTimes(18, LocalDate.parse("2019-12-23"), DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom en halvtimme", builder.getChristmasPeriodAnswerTimes(18, LocalDate.parse("2019-12-26"), DEFAULT_LOCALE));
  }

  @Test
  public void easterAnsweringTimesToBeAsExpected() {
    assertEquals("Hedvig svarar inom en timme", builder.getEasterPeriodAnswerTimes(16, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar imorgon", builder.getEasterPeriodAnswerTimes(22, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar efter kl. 10", builder.getEasterPeriodAnswerTimes(6, DEFAULT_LOCALE));
  }

  @Test
  public void summerWeekendTimesWithCoverToBeAsExpected() {
    assertEquals("Hedvig svarar inom en timme", builder.getSummerWeekendTimes(12, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar imorgon", builder.getSummerWeekendTimes(18, DEFAULT_LOCALE));
  }

  @Test
  public void redDayAndWeekendTimesAsExpected() {
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(2, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar efter kl. 9", builder.getRedDayAndWeekendAnswerTimes(8, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(10, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar inom en timme", builder.getRedDayAndWeekendAnswerTimes(20, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar imorgon", builder.getRedDayAndWeekendAnswerTimes(23, DEFAULT_LOCALE));
  }

  @Test
  public void returnHedvigSvararMandagWhenNonWorkingDay() {
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekendTimes(2, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekendTimes(19, DEFAULT_LOCALE));
    assertEquals("Hedvig svarar på Måndag", builder.getSummerWeekendTimes(11, DEFAULT_LOCALE));
  }
}

