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

*Summer Hours (15 June - 15 August)*
*Måndag-Torsday*
09-18 - Hedvig svarar inom 15 minuter
18-23 - Hedvig svarar inom en timme
23-02 - Hedvig svarar i morgon
02-8.59 - Hedvig svarar efter kl. 09

*Fredag*
09-18 - Hedvig svarar inom 15 minuter
11-11.45 - Hedvig svarar inom {11.45 - current time rounded to 5 minutes and with a 5 minute buffer} min
18-23 - Hedvig svarar inom en timme
23-02 - Hedvig svarar i morgon
02-8.59 - Hedvig svarar efter kl. 10

*Helg och helgdag*
same as regular weekend and redDays

*Regular Working Hours*

*Måndag-Torsdag*
09-18 – Hedvig svarar inom 10 min **
18-20 - Hedvig svarar inom 20 min **
20-23 – Hedvig svarar inom 30 min **
23-02 – Hedvig svarar i morgon
02-08.59 – Hedvig svarar efter kl. 09

*Fredag*
09-18 – Hedvig svarar inom 10 min **
11-11.45 - Hedvig svarar inom {11.45 - current time rounded to 5 minutes and with a 5 minute buffer} min
18-23 – Hedvig svarar inom 30 min
23-02 – Hedvig svarar i morgon
02-09.59 – Hedvig svarar efter kl. 10

*Helg och helgdag* - same as redDays
10-23 – Hedvig svarar inom en timme **
23-02 – Hedvig svarar i morgon
02-09.59 – Hedvig svarar efter kl. 10
 */

@Component
@Slf4j
public class StatusBuilderImpl implements StatusBuilder {

  private static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

  private static ArrayList<LocalDate> redDays = new ArrayList<>(Arrays.asList(LocalDate.parse("2019-01-01"),
    LocalDate.parse("2019-01-06"), LocalDate.parse("2019-04-19"), LocalDate.parse("2019-04-21"),
    LocalDate.parse("2019-04-22"), LocalDate.parse("2019-05-01"), LocalDate.parse("2019-05-30"),
    LocalDate.parse("2019-06-06"), LocalDate.parse("2019-06-21"), LocalDate.parse("2019-06-22"),
    LocalDate.parse("2019-12-25"), LocalDate.parse("2019-12-26")));

  final private LocalDate valborgStartDate = LocalDate.parse("2019-04-30");
  final private LocalDate valborgEndDate = LocalDate.parse("2019-05-01");

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


  public String getRedDayAndWeekendAnswerTimes(int hour) {
    if (hour <= 2) {
      return "Hedvig svarar imorgon";
    } else if (hour < 10) {
      return "Hedvig svarar efter kl. 10";
    } else if (hour < 23) {
      return "Hedvig svarar inom en timme";
    }
    else {
      return "Hedvig svarar imorgon";
    }
  }

  public String getSummerWeekdayAnswerTimes(int hour, DayOfWeek dayOfWeek, int minute) {
    if(dayOfWeek.equals(DayOfWeek.FRIDAY) && hour == 11 && minute >= 0 && minute <= 45) {
        return getFridayRetroMeetingTime(minute, 45);
    } else {
      switch (dayOfWeek) {
        case MONDAY:
        case TUESDAY:
        case WEDNESDAY:
        case THURSDAY:
        case FRIDAY: {
          if (hour <= 2) {
            return "Hedvig svarar imorgon";
          }
          if (hour < 9) {
            return "Hedvig svarar efter kl. 9";
          }
          if (hour < 18) {
            return "Hedvig svarar inom 15 min";
          } else if (hour < 23) {
            return "Hedvig svarar inom en timme";
          } else {
            return "Hedvig svarar imorgon";
          }
        }
        default: {
          log.error("getSummerWeekdayAnswerTimes method has not returned a hedvig answer time");
          return "";
        }
      }
    }
  }

  public boolean isSummerTime(LocalDate todayDate) {
    LocalDate summerStart = LocalDate.parse(currentYear + "-06-14");
    LocalDate summerEnd = LocalDate.parse(currentYear + "-08-16");
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

    if ((todayDate.equals(valborgStartDate) && hour >= 18) || (todayDate.equals(valborgEndDate) && hour <= 23 )) {
      return "Hedvig svarar den 2a Maj";
    }
    else if (redDays.contains(todayDate)) {
      return getRedDayAndWeekendAnswerTimes(hour);
    } else if (isSummerTime(todayDate) && (!dayOfWeek.equals(DayOfWeek.SATURDAY)) && !dayOfWeek.equals(DayOfWeek.SUNDAY)) {
      return getSummerWeekdayAnswerTimes(hour, dayOfWeek, minute);
    } else {

      switch (dayOfWeek) {
        case MONDAY:
        case TUESDAY:
        case WEDNESDAY:
        case THURSDAY: {
          if (hour <= 2) {
            return "Hedvig svarar imorgon";
          }
          if (hour <= 9) {
            return "Hedvig svarar efter kl. 9";
          }
          if (hour < 18) {
            return "Hedvig svarar inom 10 min";
          }
          else if (hour < 20) {
            return "Hedvig svarar inom 20 min";
          }
          else if (hour < 23) {
            return "Hedvig svarar inom 30 min";
          } else {
            return "Hedvig svarar imorgon";
          }
        }
        case FRIDAY: {
          if (hour <= 2) {
            return "Hedvig svarar imorgon";
          }
          if (hour < 9) {
            return "Hedvig svarar efter kl. 9";
          }
          if (hour == 11 && minute >= 0 && minute <= 45) {
            int meetingEndTime = 45;
            return getFridayRetroMeetingTime(minute, meetingEndTime);
          }
          if (hour < 18) {
            return "Hedvig svarar inom 10 min";
          } else if (hour < 23) {
            return "Hedvig svarar inom 30 min";
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
