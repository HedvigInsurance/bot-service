package com.hedvig.botService.chat;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

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
13th - 14th July
20th - 21st July
27th - 28th July
3rd - 4th August
10th - 11th August
Day before these weekends i.e. 12th June, 19th June cover will stop from 6pm
21st is an exception see Midsummer note below

*Ad hoc*
No cover for Midsummer from 23:00 on 20th to 22th June
Summer party no cover after 3pm

*Regular Working Hours*

*Måndag-Torsdag*
08-17 – Hedvig svarar inom 10 min **
17-22 - Hedvig svarar inom 20 min **
22-02 – Hedvig svarar i morgon
02-07.59 – Hedvig svarar efter kl. 08

*Fredag*
08-17 – Hedvig svarar inom 10 min **
11-11.45 - Hedvig svarar inom {11.45 - current time rounded to 5 minutes and with a 5 minute buffer} min
17-22 – Hedvig svarar inom 20 min
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

  private static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

  private static ArrayList<LocalDate> redDays = new ArrayList<>(Arrays.asList(
      LocalDate.parse("2019-01-01"), LocalDate.parse("2019-01-06"), LocalDate.parse("2019-04-19"),
      LocalDate.parse("2019-04-21"), LocalDate.parse("2019-04-22"), LocalDate.parse("2019-05-01"),
      LocalDate.parse("2019-05-30"), LocalDate.parse("2019-06-06"), LocalDate.parse("2019-12-25"),
      LocalDate.parse("2019-12-26")
    )
  );

  private static ArrayList<LocalDate> summerWeekendsWithNoChatCover = new ArrayList<>(Arrays.asList(
      LocalDate.parse("2019-07-13"), LocalDate.parse("2019-07-14"), LocalDate.parse("2019-07-20"),
      LocalDate.parse("2019-07-21"), LocalDate.parse("2019-07-27"), LocalDate.parse("2019-07-28"),
      LocalDate.parse("2019-08-03"), LocalDate.parse("2019-08-04"), LocalDate.parse("2019-08-10"),
      LocalDate.parse("2019-08-11")
    )
  );

  private static LocalDate christmasStartDate = LocalDate.parse("2019-12-22");
  private static LocalDate christmasEndDate = LocalDate.parse("2020-01-07");

  private static ArrayList<LocalDate> christmasRedDays = new ArrayList<>(Arrays.asList(
    LocalDate.parse("2019-12-24"), LocalDate.parse("2019-12-25"), LocalDate.parse("2019-12-26"),
    LocalDate.parse("2019-12-31"), LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-06")
  ));

  final private LocalDate midsommarStartDate = LocalDate.parse("2019-06-20");
  final private LocalDate midsommarEndDate = LocalDate.parse("2019-06-21");

  public String getFridayRetroMeetingTime(int currentMinute, int meetingEndTime) {
    int roundedTime = currentMinute;
    final int buffer = 5;

    if(currentMinute % 5 != 0) {
      int remainder = currentMinute % 5;
      roundedTime = currentMinute - remainder;
    }

    int timeToAnswer = (meetingEndTime + buffer) - roundedTime;
    return "Hedvig svarar inom " + timeToAnswer + " min";
  }

  public String getChristmasPeriodAnswerTimes(int hour, LocalDate date) {
    if (hour <= 2) {
      return "Hedvig svarar imorgon";
    }
    if (hour < 10) {
      return "Hedvig svarar efter kl. 10";
    }
    if (hour < 17) {
      return christmasRedDays.contains(date) ? "Hedvig svarar inom en halvtimme": "Hedvig svarar inom 10 min";
    }
    if (hour < 20) {
      return christmasRedDays.contains(date) ? "Hedvig svarar inom en halvtimme": "Hedvig svarar inom 20 min";
    }
    else {
      return "Hedvig svarar imorgon";
    }
  }

  public String getRedDayAndWeekendAnswerTimes(int hour) {
    if (hour <= 2) {
      return "Hedvig svarar imorgon";
    } else if (hour < 9) {
      return "Hedvig svarar efter kl. 9";
    } else if (hour < 22) {
      return "Hedvig svarar inom en timme";
    }
    else {
      return "Hedvig svarar imorgon";
    }
  }

  public String hedvigWillAnswerOnMonday() {
    return "Hedvig svarar på Måndag";
  }

  public String getSummerWeekdayAnswerTimes(int hour, int minute, LocalDate todayDate) {
    final DayOfWeek dayOfWeek = todayDate.getDayOfWeek();
    final LocalDate tomorrowDate = todayDate.plusDays(1);
    final LocalDate summerParty = LocalDate.parse("2019-06-28");

    if(dayOfWeek.equals(DayOfWeek.FRIDAY) && hour == 11 && minute >= 0 && minute <= 45) {
        return getFridayRetroMeetingTime(minute, 45);
    }

    if(todayDate.isEqual(summerParty) && hour >= 15) {
      return "Hedvig svarar imorgon";
    }

    switch (dayOfWeek) {
      case MONDAY:
      case TUESDAY:
      case WEDNESDAY:
      case THURSDAY:
      case FRIDAY: {
        if (hour <= 2) {
          return "Hedvig svarar imorgon";
        }
        if (hour < 8) {
          return "Hedvig svarar efter kl. 8";
        }
        if (hour < 17) {
          return "Hedvig svarar inom 15 min";
        }
        if (summerWeekendsWithNoChatCover.contains(tomorrowDate)) {
          return hedvigWillAnswerOnMonday();
        }
        return "Hedvig svarar imorgon";
      }
      default: {
        log.error("getSummerWeekdayAnswerTimes method has not returned a hedvig answer time");
        return "";
      }
    }
  }

  public String getSummerWeekendTimes(int hour, LocalDate todayDate) {

    if (summerWeekendsWithNoChatCover.contains(todayDate)) {
      return hedvigWillAnswerOnMonday();
    }

    if (hour <= 2) {
      return "Hedvig svarar imorgon";
    }
    if (hour < 10) {
      return "Hedvig svarar efter kl. 10";
    }
    if (hour < 18) {
      return "Hedvig svarar inom en timme";
    }
    else {
      return "Hedvig svarar imorgon";
    }
  }

  public boolean isSummerTime(LocalDate todayDate) {
    LocalDate summerStart = LocalDate.of(currentYear,6,20);
    LocalDate summerEnd = LocalDate.of(currentYear,8,12);
    return todayDate.isAfter(summerStart) && todayDate.isBefore(summerEnd);
  }

  @Override
  public String getStatusMessage(Clock c) {

    Instant now = Instant.now(c);
    ZonedDateTime time = now.atZone(ZoneId.of("Europe/Stockholm"));

    final DayOfWeek dayOfWeek = time.getDayOfWeek();
    final int hour = time.getHour();
    final int minute = time.getMinute();
    final LocalDate todayDate = LocalDate.now();

    if(todayDate.isAfter(christmasStartDate) && todayDate.isBefore(christmasEndDate)) {
      return getChristmasPeriodAnswerTimes(hour, todayDate);
    }

    if ((todayDate.equals(midsommarStartDate) && hour >= 23) || (todayDate.equals(midsommarEndDate))) {
      return "Hedvig svarar den 22e juni";
    }

    if (isSummerTime(todayDate) && (dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY))) {
      return getSummerWeekendTimes(hour, todayDate);
    }

    if (isSummerTime(todayDate) && (!dayOfWeek.equals(DayOfWeek.SATURDAY)) && !dayOfWeek.equals(DayOfWeek.SUNDAY)) {
      return getSummerWeekdayAnswerTimes(hour, minute, todayDate);
    }

    if (redDays.contains(todayDate)) {
      return getRedDayAndWeekendAnswerTimes(hour);
    }
    else {
      switch (dayOfWeek) {
        case MONDAY:
        case TUESDAY:
        case WEDNESDAY:
        case THURSDAY: {
          if (hour <= 2) {
            return "Hedvig svarar imorgon";
          }
          if (hour < 8) {
            return "Hedvig svarar efter kl. 8";
          }
          if (hour < 17) {
            return "Hedvig svarar inom 10 min";
          }
          else if (hour < 22) {
            return "Hedvig svarar inom 20 min";
          } else {
            return "Hedvig svarar imorgon";
          }
        }
        case FRIDAY: {
          if (hour <= 2) {
            return "Hedvig svarar imorgon";
          }
          if (hour < 8) {
            return "Hedvig svarar efter kl. 8";
          }
          if (hour == 11 && minute >= 0 && minute <= 45) {
            int meetingEndTime = 45;
            return getFridayRetroMeetingTime(minute, meetingEndTime);
          }
          if (hour < 17) {
            return "Hedvig svarar inom 10 min";
          } else if (hour < 22) {
            return "Hedvig svarar inom 20 min";
          } else {
            return "Hedvig svarar imorgon";
          }
        }
        case SATURDAY:
        case SUNDAY: {
          return getRedDayAndWeekendAnswerTimes(hour);
        }
        default: {
          log.error("getstatusmessage method has not returned a hedvig answer time");
          return "";
        }
      }
    }
  }
}
