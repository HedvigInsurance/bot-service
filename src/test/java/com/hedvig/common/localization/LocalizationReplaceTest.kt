package com.hedvig.common.localization

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class LocalizationReplaceTest {

    @MockK
    lateinit var clientWrapper: LokaliseClientWrapper

    lateinit var classUnderTest: LocalizationServiceImpl

    @BeforeEach
    fun before() {
        classUnderTest = LocalizationServiceImpl(clientWrapper)
    }

    @Test
    fun `replace one`() {
        every { clientWrapper.getTranslation(any(), any()) } returns "String with [%1\$s:KEY]"

        val translation = classUnderTest.getTranslation("key", Locale.CANADA)

        assertThat(translation).isEqualTo("String with {KEY}")
    }


    @Test
    fun `replace two`() {
        every { clientWrapper.getTranslation(any(), any()) } returns "String with [%1\$s:KEY] and [%1\$s:KEY_2]"

        val translation = classUnderTest.getTranslation("key", Locale.CANADA)

        assertThat(translation).isEqualTo("String with {KEY} and {KEY_2}")
    }

    @Test
    fun `replace three`() {
        every { clientWrapper.getTranslation(any(), any()) } returns "String with [%1\$s:KEY], [%2\$s:KEY_2] and [%3\$s:KEY_3]"

        val translation = classUnderTest.getTranslation("key", Locale.CANADA)

        assertThat(translation).isEqualTo("String with {KEY}, {KEY_2} and {KEY_3}")
    }

    @Test
    fun `replace big numbers`() {
        every { clientWrapper.getTranslation(any(), any()) } returns "[%10\$s:KEY], [%200\$s:KEY_2], [%3210\$s:KEY_3], [%456789\$s:KEY_4.5_6_7_8_9], [%500000\$s:KEY_5]"

        val translation = classUnderTest.getTranslation("key", Locale.CANADA)

        assertThat(translation).isEqualTo("{KEY}, {KEY_2}, {KEY_3}, {KEY_4.5_6_7_8_9}, {KEY_5}")
    }

    @Test
    fun `replace none`() {
        every { clientWrapper.getTranslation(any(), any()) } returns "String without"

        val translation = classUnderTest.getTranslation("key", Locale.CANADA)

        assertThat(translation).isEqualTo("String without")
    }
}
