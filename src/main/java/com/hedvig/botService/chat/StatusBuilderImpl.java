package com.hedvig.botService.chat;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;

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
public class StatusBuilderImpl implements StatusBuilder {
  private static String[] redDays = { "2019-01-01", "2019-01-06", "2019-04-19", "2019-04-21", "2019-04-22",
    "2019-05-01", "2019-05-30", "2019-06-06", "2019-06-21", "2019-06-22", "2019-12-25", "2019-12-26" };

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

  public Boolean redDay(String time) {
    int count = 0;

    for (String day : redDays) {
      if (count == 1) return true;
      if (day.equals(time)) count += 1;
    }
    return count == 1;
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

  public String getSummerWeekdayAnswerTimes(int hour, String dayOfWeek, int minute) {
    if(dayOfWeek.equals("FRIDAY") && hour == 11 && minute >= 0 && minute <= 45) {
      return getFridayRetroMeetingTime(minute, 45);
    } else {
      switch (dayOfWeek) {
        case "MONDAY":
        case "TUESDAY":
        case "WEDNESDAY":
        case "THURSDAY":
        case "FRIDAY": {
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
      }
      return "";
    }
  }

  private String dateToString(ZonedDateTime time) {
    return time.toString().substring(0, 10);
  }

  public boolean summerTime(String date) {
    LocalDate todayDate = LocalDate.parse(date);
    LocalDate summerStart = LocalDate.parse("2019-06-14");
    LocalDate summerEnd = LocalDate.parse("2019-08-16");
    return todayDate.isAfter(summerStart) && todayDate.isBefore(summerEnd);
  }

  @Override
  public String getStatusMessage(Clock c) {

    Instant now = Instant.now(c);
    ZonedDateTime time = now.atZone(ZoneId.of("Europe/Stockholm"));

    final DayOfWeek dayOfWeek = time.getDayOfWeek();
    final int hour = time.getHour();
    final int minute = time.getMinute();
    final String dateTodayStringFormat = dateToString(time);
    final String dayOfWeekStringFormat = dayOfWeek.toString();

    if (redDay(dateTodayStringFormat)) {
      return getRedDayAndWeekendAnswerTimes(hour);
    } else if (summerTime(dateTodayStringFormat) && (!dayOfWeekStringFormat.equals("SATURDAY") && !dayOfWeekStringFormat.equals("SUNDAY"))) {
      return getSummerWeekdayAnswerTimes(hour, dayOfWeekStringFormat, minute);
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
          if (hour <= 9) {
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

      }

      return "";

    }
  }
}
