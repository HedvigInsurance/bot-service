package com.hedvig.botService.chat

import com.hedvig.common.localization.LocalizationService
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
    private lateinit var localizationService: LocalizationService

    private lateinit var statusBuilderToTest: StatusBuilderImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { localizationService.getTranslation(any(), any()) } returns null
        statusBuilderToTest = StatusBuilderImpl(localizationService)
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
        assertThat(replyEarlyNight).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyEarlyMorning).isEqualTo("Hedvig svarar efter kl. 8")
        assertThat(replyMiddleOfTheDay).isEqualTo("Hedvig svarar inom 10 min")
        assertThat(replyEarlyEvening).isEqualTo("Hedvig svarar inom 20 min")
        assertThat(replyLateEvening).isEqualTo("Hedvig svarar inom 30 min")
        assertThat(replyLateNight).isEqualTo("Hedvig svarar imorgon")
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
        assertThat(replyEarlyNight).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyEarlyMorning).isEqualTo("Hedvig svarar efter kl. 10")
        assertThat(replyMiddleOfTheDay).isEqualTo("Hedvig svarar inom en timme")
        assertThat(replyEarlyEvening).isEqualTo("Hedvig svarar inom en timme")
        assertThat(replyLateEvening).isEqualTo("Hedvig svarar inom en timme")
        assertThat(replyLateNight).isEqualTo("Hedvig svarar imorgon")
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
        assertThat(replyEarlyNight).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyEarlyMorning).isEqualTo("Hedvig svarar efter kl. 10")
        assertThat(replyMiddleOfTheDay).isEqualTo("Hedvig svarar inom en timme")
        assertThat(replyEarlyEvening).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyLateEvening).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyLateNight).isEqualTo("Hedvig svarar imorgon")
    }

    @Test
    fun `Returns correct reply for red day`() {
        val today = LocalDate.of(2021, 1, 25)
        val replyEarlyNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(2, 0, 0), DEFAULT_LOCALE)
        val replyEarlyMorning = statusBuilderToTest.getStatusReplyMessage(today.atTime(7, 0, 0), DEFAULT_LOCALE)
        val replyMiddleOfTheDay = statusBuilderToTest.getStatusReplyMessage(today.atTime(13, 0, 0), DEFAULT_LOCALE)
        val replyEarlyEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(18, 0, 0), DEFAULT_LOCALE)
        val replyLateEvening = statusBuilderToTest.getStatusReplyMessage(today.atTime(21, 0, 0), DEFAULT_LOCALE)
        val replyLateNight = statusBuilderToTest.getStatusReplyMessage(today.atTime(23, 0, 0), DEFAULT_LOCALE)
        assertThat(replyEarlyNight).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyEarlyMorning).isEqualTo("Hedvig svarar efter kl. 10")
        assertThat(replyMiddleOfTheDay).isEqualTo("Hedvig svarar inom en timme")
        assertThat(replyEarlyEvening).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyLateEvening).isEqualTo("Hedvig svarar imorgon")
        assertThat(replyLateNight).isEqualTo("Hedvig svarar imorgon")
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
        assertThat(replyEarlyNight).isEqualTo("Hedvig återkommer på juldagen")
        assertThat(replyEarlyMorning).isEqualTo("Hedvig återkommer på juldagen")
        assertThat(replyMiddleOfTheDay).isEqualTo("Hedvig återkommer på juldagen")
        assertThat(replyEarlyEvening).isEqualTo("Hedvig återkommer på juldagen")
        assertThat(replyLateEvening).isEqualTo("Hedvig återkommer på juldagen")
        assertThat(replyLateNight).isEqualTo("Hedvig återkommer på juldagen")
    }
}
