package com.hedvig.botService.chat

import com.hedvig.botService.chat.StatusBuilderImpl.Companion.RETRO_END_MINUTE
import com.hedvig.botService.chat.StatusBuilderImpl.Companion.RETRO_START_HOUR
import com.hedvig.libs.translations.Translations
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.LocalDate
import java.util.Locale
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class StatusBuilderImplTest {
    private val DEFAULT_LOCALE = Locale("sv-SE")

    @MockK
    private lateinit var translations: Translations

    private lateinit var statusBuilderToTest: StatusBuilderImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { translations.get(any(), any()) } returns null
        statusBuilderToTest = StatusBuilderImpl(translations)
    }

    @Test
    fun `Returns correct reply for regular weekday`() {
        val today = LocalDate.of(2020, 12, 17)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyEarlyMorning).isEqualTo("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY")
        assertThat(replyMiddleOfTheDay).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyEarlyEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyLateEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyLateNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
    }

    @Test
    fun `Returns correct reply for retro meeting`() {
        val today = LocalDate.of(2020, 12, 18) // regular Friday
        val replyBeforeRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR - 1, 59), DEFAULT_LOCALE)
        val replyAtRetroStart = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, 0), DEFAULT_LOCALE)
        val replyAt5MinIntoRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, 5), DEFAULT_LOCALE)
        val replyAt10MinIntoRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, 10), DEFAULT_LOCALE)
        val replyAt15MinIntoRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, 15), DEFAULT_LOCALE)
        val replyAt13MinIntoRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, 13), DEFAULT_LOCALE)
        val replyBeforeRetroEnd = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, RETRO_END_MINUTE - 1), DEFAULT_LOCALE)
        val replyAtEndOfRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, RETRO_END_MINUTE), DEFAULT_LOCALE)
        val replyAfterRetro = statusBuilderToTest.getStatusReplyMessage(today.atTime(RETRO_START_HOUR, RETRO_END_MINUTE + 1), DEFAULT_LOCALE)
        assertThat(replyBeforeRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAtRetroStart).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAt5MinIntoRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAt10MinIntoRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAt13MinIntoRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAt15MinIntoRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyBeforeRetroEnd).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAtEndOfRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
        assertThat(replyAfterRetro).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_MIN")
    }

    @Test
    fun `Returns correct reply for regular weekend`() {
        val today = LocalDate.of(2020, 12, 19)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyEarlyMorning).isEqualTo("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY")
        assertThat(replyMiddleOfTheDay).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR")
        assertThat(replyEarlyEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR")
        assertThat(replyLateEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR")
        assertThat(replyLateNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
    }

    @Test
    fun `Returns correct reply for understaffed day`() {
        val today = LocalDate.of(2020, 12, 25)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyEarlyMorning).isEqualTo("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY")
        assertThat(replyMiddleOfTheDay).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR")
        assertThat(replyEarlyEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyLateEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyLateNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
    }

    @Test
    fun `Returns correct reply for red day`() {
        val today = LocalDate.of(2021, 1, 6)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyEarlyMorning).isEqualTo("BOT_SERVICE_STATUS_REPLY_AFTER_HOUR_OF_DAY")
        assertThat(replyMiddleOfTheDay).isEqualTo("BOT_SERVICE_STATUS_REPLY_WITHIN_AN_HOUR")
        assertThat(replyEarlyEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyLateEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
        assertThat(replyLateNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_TOMORROW")
    }

    @Test
    fun `Returns correct reply for christmas`() {
        val today = LocalDate.of(2020, 12, 24)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
        assertThat(replyEarlyMorning).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
        assertThat(replyMiddleOfTheDay).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
        assertThat(replyEarlyEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
        assertThat(replyLateEvening).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
        assertThat(replyLateNight).isEqualTo("BOT_SERVICE_STATUS_REPLY_CHRISTMAS_DAY")
    }
}
