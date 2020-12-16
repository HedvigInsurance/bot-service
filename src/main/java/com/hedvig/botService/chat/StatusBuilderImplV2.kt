package com.hedvig.botService.chat

import com.hedvig.common.localization.LocalizationService
import java.time.*
import java.util.*

class StatusBuilderImplV2(
    private val localizationService: LocalizationService
) : StatusBuilder {
    override fun getStatusMessage(clock: Clock, locale: Locale): String {
        val today = LocalDate.now(clock)
        val dayOfWeek = today.dayOfWeek!!
        val now = LocalDateTime.now(clock)
        val hour = now.hour
        val minute = now.minute
        if (isChristmasPeriod(today)) {
            return getRepliesOnChristmasDay(locale)
        }
        if (isSingleStudentDay(today)) {
            return getSingleStudentWorkdayReply(locale, hour)
        }
        if (isRedDay(today)) {
            return getRegularRedDayAndWeekendReply(locale, hour)
        }
        return when (dayOfWeek) {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY -> getRegularWeekdayReply(locale, dayOfWeek, hour, minute)
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY -> getRegularRedDayAndWeekendReply(locale, hour)
        }
    }

    private fun getRegularWeekdayReply(
        locale: Locale,
        dayOfWeek: DayOfWeek,
        hour: Int,
        minute: Int
    ) = when {
        isRetroMeeting(dayOfWeek, hour, minute) ->
            getRepliesWithinMinutes(locale, maxOf(RETRO_END_MINUTE - minute + minute % 5, 10))
        hour < 3 -> getRepliesTomorrow(locale)
        hour < 8 -> getRepliesAfterHour(locale, 8)
        hour < 17 -> getRepliesWithinMinutes(locale, 10)
        hour < 20 -> getRepliesWithinMinutes(locale, 20)
        hour < 22 -> getRepliesWithinMinutes(locale, 30)
        else -> getRepliesTomorrow(locale)
    }

    private fun getRegularRedDayAndWeekendReply(
        locale: Locale,
        hour: Int
    ) = when {
        hour < 3 -> getRepliesTomorrow(locale)
        hour < 9 -> getRepliesAfterHour(locale, 9)
        hour < 22 -> getRepliesWithinAnHour(locale)
        else -> getRepliesTomorrow(locale)
    }

    private fun getSingleStudentWorkdayReply(
        locale: Locale,
        hour: Int
    ) = when {
        hour < 3 -> getRepliesTomorrow(locale)
        hour < 10 -> getRepliesAfterHour(locale, 10)
        hour < 18 -> getRepliesWithinAnHour(locale)
        else -> getRepliesTomorrow(locale)
    }

    private fun isRetroMeeting(dayOfWeek: DayOfWeek, hour: Int, minute: Int) =
        dayOfWeek == RETRO_DAY && hour == RETRO_START_HOUR && minute <= RETRO_END_MINUTE

    private fun getRepliesTomorrow(locale: Locale): String =
        localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_TOMORROW", locale)
            ?: "Hedvig svarar imorgon"

    private fun getRepliesAfterHour(locale: Locale, hour: Int): String {
        val text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY", locale)
            ?: "Hedvig svarar efter kl. {HOUR_OF_DAY}"
        text.replace("{HOUR_OF_DAY}", hour.toString())
        return text
    }

    private fun getRepliesWithinAnHour(locale: Locale) =
        localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR", locale)
            ?: "Hedvig svarar inom en timme"

    private fun getRepliesWithinMinutes(locale: Locale, minutes: Int): String {
        val text = localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN", locale)
            ?: "Hedvig svarar inom {MINUTES} min"
        text.replace("{MINUTES}", minutes.toString())
        return text
    }

    private fun getRepliesOnChristmasDay(locale: Locale) =
        localizationService.getTranslation("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY", locale)
            ?: "Hedvig återkommer på juldagen"

    private fun isChristmasPeriod(date: LocalDate) =
        date.month == Month.DECEMBER && (date.dayOfMonth == 23 || date.dayOfMonth == 24)

    private fun isSingleStudentDay(date: LocalDate) = SINGLE_STUDENT_DAYS.contains(date)

    private fun isRedDay(date: LocalDate) = RED_DAYS.contains(date)

    companion object {
        private val RETRO_DAY = DayOfWeek.FRIDAY
        private const val RETRO_START_HOUR = 11
        private const val RETRO_END_MINUTE = 45

        private val SINGLE_STUDENT_DAYS = setOf(
            LocalDate.of(2020, 12, 25),
            LocalDate.of(2020, 12, 26),
            LocalDate.of(2020, 12, 27),
            LocalDate.of(2020, 12, 31),
            LocalDate.of(2021, 1, 1)
        )

        private val RED_DAYS = setOf(
            LocalDate.of(2021, 1, 1),
            LocalDate.of(2021, 1, 6),
            LocalDate.of(2021, 4, 2),
            LocalDate.of(2021, 4, 4),
            LocalDate.of(2021, 4, 5),
            LocalDate.of(2021, 5, 1),
            LocalDate.of(2021, 5, 13),
            LocalDate.of(2021, 5, 23),
            LocalDate.of(2021, 6, 6),
            LocalDate.of(2021, 6, 26),
            LocalDate.of(2021, 11, 6),
            LocalDate.of(2021, 12, 25),
            LocalDate.of(2021, 12, 26)
        )
    }
}
