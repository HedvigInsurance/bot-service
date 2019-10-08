package com.hedvig.botService.chat

import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ANCILLARY_AREA
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_LAST_NAME
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ADDRESS_LOOK_UP_SUCCESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SSN
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_STREET_ADDRESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ZIP_CODE
import com.hedvig.botService.chat.house.HouseConversationConstants.HOUSE_FIRST
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_OWN
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_RENT
import com.hedvig.botService.chat.house.HouseOnboardingConversation
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.serviceIntegration.lookupService.LookupService
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.Address
import com.hedvig.botService.serviceIntegration.memberService.dto.LookupResponse
import com.hedvig.botService.services.LocalizationService
import com.hedvig.botService.testHelpers.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class HouseOnboardingConversationTest {

    @Mock
    private lateinit var memberService: MemberService
    @Mock
    private lateinit var lookupService: LookupService
    @Mock
    private lateinit var localizationService: LocalizationService
    @Mock
    private lateinit var publisher: ApplicationEventPublisher
    @Mock
    private lateinit var conversationFactory: ConversationFactory
    @Mock
    internal var mockConversation: Conversation? = null

    private lateinit var userContext: UserContext
    private lateinit var testConversation: HouseOnboardingConversation

    @Before
    fun setup() {
        userContext = UserContext(TestData.TOLVANSSON_MEMBER_ID)

        testConversation = HouseOnboardingConversation(
            memberService, lookupService, publisher, conversationFactory, localizationService, userContext
        )
    }

    @Test
    fun houseSelectRent_userDataHouseTypeRent_thenGoToAskSSN() {
        val message = testConversation.getMessage(HOUSE_FIRST.id + ".2")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_RENT.value)

        testConversation.receiveMessage(message)

        val onBoardingData = userContext.onBoardingData
        assertThat(onBoardingData.houseType).isEqualTo("RENT")

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.id).isEqualTo(ASK_SSN.id)
    }

    @Test
    fun houseSelectRent_userDataHouseTypeOwn_thenGoToAskSSN() {
        val message = testConversation.getMessage(HOUSE_FIRST.id + ".2")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_OWN.value)

        testConversation.receiveMessage(message)

        val onBoardingData = userContext.onBoardingData
        assertThat(onBoardingData.houseType).isEqualTo("HOUSE")

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.id).isEqualTo(ASK_SSN.id)
    }

    @Test
    fun houseProvideValidSSN_userDataStoreSSN_thenGoToLookUp() {
        given(memberService.lookupAddressSWE("191212121212", TestData.TOLVANSSON_MEMBER_ID)).willReturn(
            LookupResponse(
                "Tolvan",
                "Tolvansson",
                Address("SomeStreet 13", "Stockholm", "12345", "1004", 0)
            )
        )

        val message = testConversation.getMessage(ASK_SSN.id + ".2")
        (message!!.body as MessageBodyNumber).text = "191212121212"

        testConversation.receiveMessage(message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("19121212-****")

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_ADDRESS_LOOK_UP_SUCCESS.id)

        userContext.onBoardingData.let {
            assertThat(it.ssn).isEqualTo("191212121212")
            assertThat(it.firstName).isEqualTo("Tolvan")
            assertThat(it.familyName).isEqualTo("Tolvansson")
            assertThat(it.birthDate).isEqualTo(LocalDate.parse("1912-12-12"))
            assertThat(it.addressStreet).isEqualTo("SomeStreet 13")
            assertThat(it.addressZipCode).isEqualTo("12345")
            assertThat(it.addressCity).isEqualTo("Stockholm")
        }
    }

    @Test
    fun houseProvideValidSSN_userDataStoreSSN_failsLookUp_thenGoToLastName() {
        given(memberService.lookupAddressSWE("191212121212", TestData.TOLVANSSON_MEMBER_ID)).willReturn(null)

        val message = testConversation.getMessage(ASK_SSN.id + ".2")
        (message!!.body as MessageBodyNumber).text = "191212121212"

        testConversation.receiveMessage(message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("19121212-****")

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_LAST_NAME.id)

        userContext.onBoardingData.let {
            assertThat(it.ssn).isEqualTo("191212121212")
            assertThat(it.birthDate).isEqualTo(LocalDate.parse("1912-12-12"))
        }
    }

    @Test
    fun houseProvideLastName_userDataStoreLastName_thenGoToStreetAddress() {
        val message = testConversation.getMessage(ASK_LAST_NAME.id + ".2")
        (message!!.body as MessageBodyText).text = TestData.TOLVANSSON_LASTNAME

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_STREET_ADDRESS.id)

        userContext.onBoardingData.let {
            assertThat(it.familyName).isEqualTo(TestData.TOLVANSSON_LASTNAME)
        }
    }

    @Test
    fun houseProvideStreet_userDataStreet_thenGoToZipCode() {
        val message = testConversation.getMessage(ASK_STREET_ADDRESS.id + ".0")
        (message!!.body as MessageBodyText).text = TestData.TOLVANSSON_STREET

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_ZIP_CODE.id)

        userContext.onBoardingData.let {
            assertThat(it.addressStreet).isEqualTo(TestData.TOLVANSSON_STREET)
        }
    }

    @Test
    fun houseProvideZipCode_userDataZipCode_thenGoToSquareMeters() {
        userContext.onBoardingData.addressStreet = TestData.TOLVANSSON_STREET

        val message = testConversation.getMessage(ASK_ZIP_CODE.id + ".0")
        (message!!.body as MessageBodyNumber).text = TestData.TOLVANSSON_ZIP

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_SQUARE_METERS.id)

        userContext.onBoardingData.let {
            assertThat(it.addressZipCode).isEqualTo(TestData.TOLVANSSON_ZIP)
        }
    }

    @Test
    fun houseProvideSquareMeters_productTypeHouse_userDataSquareMeters_thenGoToAncillary() {
        userContext.onBoardingData.houseType = OnboardingConversationDevi.ProductTypes.HOUSE.toString()

        val message = testConversation.getMessage(ASK_SQUARE_METERS.id + ".2")
        (message!!.body as MessageBodyNumber).text = 100.toString()

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_ANCILLARY_AREA.id)

        userContext.onBoardingData.let {
            assertThat(it.livingSpace).isEqualTo(100f)
        }
    }

    @Test
    fun houseProvideSquareMeters_productTypeRent_userDataSquareMeters_thenEndHouseOnboarding() {
        var called = false
        given(conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)).will {
            called = true
            mockConversation
        }

        userContext.onBoardingData.houseType = OnboardingConversationDevi.ProductTypes.RENT.toString()

        val message = testConversation.getMessage(ASK_SQUARE_METERS.id + ".2")
        (message!!.body as MessageBodyNumber).text = 100.toString()

        testConversation.receiveMessage(message)

        assertThat(called).isTrue()

        userContext.onBoardingData.let {
            assertThat(it.livingSpace).isEqualTo(100f)
        }
    }

     private fun ArrayList<SelectItem>.selectWithValue(value: String) {
         this.forEach {
             if (it.value == value) it.selected = true
         }
     }
}
