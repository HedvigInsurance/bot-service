package com.hedvig.botService.services


import com.hedvig.localization.client.LocalizationClient
import com.hedvig.localization.client.dto.Key
import com.hedvig.localization.client.dto.Language
import com.hedvig.localization.client.dto.LocalizationData
import com.hedvig.localization.client.dto.LocalizationResponse
import com.hedvig.localization.client.dto.Translation
import com.hedvig.localization.service.LocalizationService
import com.hedvig.localization.service.LocalizationServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class LocalizationServiceTest{

    @Mock
    lateinit var localizationClient: LocalizationClient

    private val mockLocalizationResponse = LocalizationResponse(
        data = LocalizationData(
            listOf(
                Language(
                    listOf(
                        Translation(
                            Key(
                                "key1"
                            ),
                            "sv1"
                        ),
                        Translation(
                            Key(
                                "key2"
                            ),
                            "sv2"
                        )
                    ),
                    "sv_SE"
                ),
                Language(
                    listOf(
                        Translation(
                            Key(
                                "key1"
                            ),
                            "en1"
                        ),
                        Translation(
                            Key(
                                "key2"
                            ),
                            "en2"
                        )
                    ),
                    "en_SE"
                )
            )
        )
    )

    lateinit var localizationService: LocalizationService

    @Before
    fun setUp() {
        `when`(localizationClient.fetchLocalization(any())).thenReturn(
            mockLocalizationResponse
        )

        localizationService = LocalizationServiceImpl(localizationClient, "test")
    }

    @Test
    fun givenLocaleAndKey() {
        val textEn =  localizationService.getText(Locale("en", "se"), "key1")
        val textEn2 =  localizationService.getText(Locale("en", "SE"), "key2")
        val textSv =  localizationService.getText(Locale("sv", "SE"), "key1")
        val textSv2 =  localizationService.getText(Locale("sv", "SE"), "key2")

        assertThat(textEn).isEqualTo("en1")
        assertThat(textEn2).isEqualTo("en2")
        assertThat(textSv).isEqualTo("sv1")
        assertThat(textSv2).isEqualTo("sv2")
    }
}

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}    private fun <T> uninitialized(): T = null as T
