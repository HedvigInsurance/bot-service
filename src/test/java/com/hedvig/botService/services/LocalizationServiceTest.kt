package com.hedvig.botService.services

import com.hedvig.botService.enteties.localization.*
import com.hedvig.botService.serviceIntegration.localization.LocalizationClient
import com.hedvig.botService.services.LocalizationService.Companion.GRAPHCMS_TEXT_KEYS_QUERY
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
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
        `when`(localizationClient.fetchLocalization(GRAPHCMS_TEXT_KEYS_QUERY)).thenReturn(mockLocalizationResponse)
        localizationService = LocalizationService(localizationClient)
    }

    @Test
    fun givenLocaleAndKey() {
        val textEn =  localizationService.getText(Locale("en"), "key1")
        val textEn2 =  localizationService.getText(Locale("en"), "key2")
        val textSv =  localizationService.getText(Locale("sv"), "key1")
        val textSv2 =  localizationService.getText(Locale("sv"), "key2")

        assertThat(textEn).isEqualTo("en1")
        assertThat(textEn2).isEqualTo("en2")
        assertThat(textSv).isEqualTo("sv1")
        assertThat(textSv2).isEqualTo("sv2")
    }
}
