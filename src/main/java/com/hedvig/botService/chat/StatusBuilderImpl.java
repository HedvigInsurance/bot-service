package com.hedvig.botService.chat;

import com.hedvig.common.localization.LocalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/*

*RedDays*
10-23 - Hedvig svarar inom en timme
23-02 - Hedvig svarar i morgon
02-09.59 - Hedvig svarar efter kl. 10

*Summer Hours (21 June - 11 August)*
*Måndag-Torsday*
09-18 - Hedvig svarar inom 15 minuter
18-02 - Hedvig svarar imorgon
02-08.59 - Hedvig svarar efter kl. 9

*Fredag*
09-18 - Hedvig svarar inom 15 minuter
11-11.45 - Hedvig svarar inom {11.45 - current time rounded to 5 minutes and with a 5 minute buffer} min
18-02 - Hedvig svarar imorgon
02-9.59 - Hedvig svarar efter kl. 10

*Helg och helgdag*
10-18 – Hedvig svarar inom en timme **
18-02 – Hedvig svarar i morgon
02-09.59 – Hedvig svarar efter kl. 10

*Weekends with no cover during summer*

*Ad hoc*

*Regular Working Hours*

*Måndag-Torsdag*
08-17 – Hedvig svarar inom 10 min **
17-20 - Hedvig svarar inom 20 min **
17-22 - Hedvig svarar inom 30 min **
22-02 – Hedvig svarar i morgon
02-07.59 – Hedvig svarar efter kl. 08

*Fredag*
08-17 – Hedvig svarar inom 10 min **
11-11.45 - Hedvig svarar inom {11.45 - current time rounded to 5 minutes and with a 5 minute buffer} min
17-20 - Hedvig svarar inom 20 min **
17-22 - Hedvig svarar inom 30 min **
22-02 – Hedvig svarar i morgon
02-08.59 – Hedvig svarar efter kl. 9

*Helg och helgdag* - same as redDays
09-22 – Hedvig svarar inom en timme **
22-02 – Hedvig svarar i morgon
02-08.59 – Hedvig svarar efter kl. 9
 */

@Component
@Slf4j
public class StatusBuilderImpl implements StatusBuilder {

  public StatusBuilderImpl(@Autowired LocalizationService localizationService) {
    this.localizationService = localizationService;
  }

  private LocalizationService localizationService;

  private static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

  private static ArrayList<LocalDate> redDays = new ArrayList<>(Arrays.asList(
    LocalDate.parse("2020-05-01"), LocalDate.parse("2020-05-21")
  ));

  private static LocalDate dayBeforeEasterHoliday = LocalDate.parse("2020-04-08");
  private static LocalDate dayAfterEasterHoliday = LocalDate.parse("2020-04-14");

  private static ArrayList<LocalDate> christmasRedDays = new ArrayList<>(Arrays.asList(
    LocalDate.parse("2019-12-24"), LocalDate.parse("2019-12-25"), LocalDate.parse("2019-12-26"),
    LocalDate.parse("2019-12-31"), LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-06")
  ));

  private static LocalDate midsommar = LocalDate.parse("2020-06-19");

  public String getFridayRetroMeetingTime(int currentMinute, int meetingEndTime, Locale locale) {
    int roundedTime = currentMinute;
    final int buffer = 5;

    if (currentMinute % 5 != 0) {
      int remainder = currentMinute % 5;
      roundedTime = currentMinute - remainder;
    }

    int timeToAnswer = (meetingEndTime + buffer) - roundedTime;

    return getRepliesWithinMinText(timeToAnswer, locale);
  }

  public String getChristmasPeriodAnswerTimes(int hour, LocalDate date, Locale locale) {
    if (hour <= 2) {
      return getRepliesTomorrow(locale);
    }
    if (hour < 10) {
      return getRepliesAfterHourOfDay(10, locale);
    }
    if (hour < 17) {
      return christmasRedDays.contains(date) ? getRepliesWithinHalfAnHour(locale) : getRepliesWithinMinText(10, locale);
    }
    if (hour < 20) {
      return christmasRedDays.contains(date) ? getRepliesWithinHalfAnHour(locale) : getRepliesWithinMinText(20, locale);
    } else {
      return getRepliesTomorrow(locale);
    }
  }

  public String getEasterPeriodAnswerTimes(int hour, Locale locale) {
    if (hour <= 2) {
      return getRepliesTomorrow(locale);
    }
    if (hour < 10) {
      return getRepliesAfterHourOfDay(10, locale);
    }
    if (hour < 22) {
      return getRepliesWithinAnHour(locale);
    } else {
      return getRepliesTomorrow(locale);
    }
  }

  public String getRedDayAndWeekendAnswerTimes(int hour, Locale locale) {
    if (hour <= 2) {
      return getRepliesTomorrow(locale);
    } else if (hour < 9) {
      return getRepliesAfterHourOfDay(9, locale);
    } else if (hour < 22) {
      return getRepliesWithinAnHour(locale);
    } else {
      return getRepliesTomorrow(locale);
    }
  }

  public String getSummerWeekendTimes(int hour, Locale locale) {
    if (hour <= 2) {
      return getRepliesTomorrow(locale);
    }
    if (hour < 10) {
      return getRepliesAfterHourOfDay(10, locale);
    }
    if (hour < 22) {
      return getRepliesWithinAnHour(locale);
    } else {
      return getRepliesTomorrow(locale);
    }
  }

  public String getMidsommarAnsweringTimes(int hour, Locale locale) {
    if (hour <= 2) {
      return getRepliesTomorrow(locale);
    }
    if (hour < 10) {
      return getRepliesAfterHourOfDay(10, locale);
    }
    if (hour < 18) {
      return getRepliesWithinAnHour(locale);
    } else {
      return getRepliesTomorrow(locale);
    }
  }

  public boolean isSummerTime(LocalDate todayDate) {
    LocalDate summerStart = LocalDate.of(currentYear, 6, 12);
    LocalDate summerEnd = LocalDate.of(currentYear, 8, 10);
    return todayDate.isAfter(summerStart) && todayDate.isBefore(summerEnd);
  }

  @Override
  public String getStatusMessage(Clock c, Locale locale) {

    Instant now = Instant.now(c);
    ZonedDateTime time = now.atZone(ZoneId.of("Europe/Stockholm"));

    final LocalDate noCoverDate = LocalDate.parse("2020-01-17");

    final DayOfWeek dayOfWeek = time.getDayOfWeek();
    final int hour = time.getHour();
    final int minute = time.getMinute();
    final LocalDate todayDate = LocalDate.now();

    if (todayDate.isAfter(dayBeforeEasterHoliday) && todayDate.isBefore(dayAfterEasterHoliday)) {
      return getEasterPeriodAnswerTimes(hour, locale);
    }

    if (todayDate.equals(noCoverDate) && hour >= 20) {
      return getRepliesTomorrow(locale);
    }

    if (todayDate.equals(midsommar)) {
      return getMidsommarAnsweringTimes(hour, locale);
    }

    if (isSummerTime(todayDate) && (dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY))) {
      return getSummerWeekendTimes(hour, locale);
    }

    if (redDays.contains(todayDate)) {
      return getRedDayAndWeekendAnswerTimes(hour, locale);
    } else {
      switch (dayOfWeek) {
        case MONDAY:
        case TUESDAY:
        case WEDNESDAY:
        case THURSDAY: {
          if (hour <= 2) {
            return getRepliesTomorrow(locale);
          }
          if (hour < 8) {
            return getRepliesAfterHourOfDay(8, locale);
          }
          if (hour < 17) {
            return getRepliesWithinMinText(10, locale);
          }
          if (hour < 20) {
            return getRepliesWithinMinText(20, locale);
          }
          if (hour < 22) {
            return getRepliesWithinMinText(30, locale);
          } else {
            return getRepliesTomorrow(locale);
          }
        }
        case FRIDAY: {
          if (hour <= 2) {
            return getRepliesTomorrow(locale);
          }
          if (hour < 8) {
            return getRepliesAfterHourOfDay(8, locale);
          }
          if (hour == 11 && minute >= 0 && minute <= 45) {
            int meetingEndTime = 45;
            return getFridayRetroMeetingTime(minute, meetingEndTime, locale);
          }
          if (hour < 17) {
            return getRepliesWithinMinText(10, locale);
          }
          if (hour < 20) {
            return getRepliesWithinMinText(20, locale);
          }
          if (hour < 22) {
            return getRepliesWithinMinText(30, locale);
          } else {
            return getRepliesTomorrow(locale);
          }
        }
        case SATURDAY:
        case SUNDAY: {
          return getRedDayAndWeekendAnswerTimes(hour, locale);
        }
        default: {
          log.error("getstatusmessage method has not returned a hedvig answer time");
          return "";
        }
      }
    }
  }

  private String getRepliesWithinMinText(int min, Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN", locale);
    if (text == null) {
      text = "Hedvig svarar inom {MINUTES} min";
    }

    return text.replace("{MINUTES}", String.valueOf(min));
  }

  private String getRepliesAfterHourOfDay(int hourOfDay, Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY", locale);
    if (text == null) {
      text = "Hedvig svarar efter kl. {HOUR_OF_DAY}";
    }

    return text.replace("{HOUR_OF_DAY}", String.valueOf(hourOfDay));
  }

  private String getRepliesTomorrow(Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_TOMORROW", locale);
    if (text == null) {
      text = "Hedvig svarar imorgon";
    }

    return text;
  }

  private String getRepliesWithinHalfAnHour(Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_WITHIN_HALF_AN_HOUR", locale);
    if (text == null) {
      text = "Hedvig svarar inom en halvtimme";
    }

    return text;
  }

  private String getRepliesWithinAnHour(Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR", locale);
    if (text == null) {
      text = "Hedvig svarar inom en timme";
    }

    return text;
  }


  private String getRepliesOnMonday(Locale locale) {
    String text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_ON_MONDAY", locale);
    if (text == null) {
      text = "Hedvig svarar på Måndag";
    }

    return text;
  }
}
