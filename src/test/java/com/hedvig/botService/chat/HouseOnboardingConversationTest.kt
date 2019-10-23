package com.hedvig.botService.chat

import com.hedvig.botService.chat.house.HouseConversationConstants
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ANCILLARY_AREA
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_LAST_NAME
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ADDRESS_LOOK_UP_SUCCESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_BATHROOMS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_REAL_ESTATE_LOOKUP_CORRECT
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SSN
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_STREET_ADDRESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ZIP_CODE
import com.hedvig.botService.chat.house.HouseConversationConstants.HOUSE_FIRST
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_ADDRESS_LOOK_UP_SUCCESS_NO
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_ADDRESS_LOOK_UP_SUCCESS_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_OWN
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_RENT
import com.hedvig.botService.chat.house.HouseOnboardingConversation
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.serviceIntegration.lookupService.LookupService
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateDrain
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateDto
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateResponse
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateWater
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

        val message = testConversation.getMessage(ASK_SSN.id + ".0")
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
    fun houseProvideValidSSN_userDataStoreSSN_underEighteen_thenGoToAskSsnUnderEighteen() {
        val message = testConversation.getMessage(ASK_SSN.id + ".0")
        (message!!.body as MessageBodyNumber).text = "201912121212"

        testConversation.receiveMessage(message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("20191212-****")

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(HouseConversationConstants.ASK_SSN_UNDER_EIGHTEEN.id)
    }

    @Test
    fun houseProvideValidSSN_userDataStoreSSN_failsLookUp_thenGoToLastName() {
        given(memberService.lookupAddressSWE("191212121212", TestData.TOLVANSSON_MEMBER_ID)).willReturn(null)

        val message = testConversation.getMessage(ASK_SSN.id + ".0")
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
    fun houseProvideLookupSuccess_userDataSqm_thenGoToRealEstateLookup() {
        given(
            lookupService.realEstateLookup(
                TestData.TOLVANSSON_MEMBER_ID,
                RealEstateDto("SomeStreet 13", "12345")
            )
        ).willReturn(
            RealEstateResponse(
                21,
                21,
                21,
                RealEstateDrain("", ""),
                200,
                1,
                "",
                RealEstateWater("", ""),
                1930
            )
        )
        userContext.onBoardingData.apply {
            houseType = OnboardingConversationDevi.ProductTypes.HOUSE.toString()
            addressStreet = "SomeStreet 13"
            addressZipCode = "12345"
        }

        val message = testConversation.getMessage(ASK_ADDRESS_LOOK_UP_SUCCESS.id + ".0")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_ADDRESS_LOOK_UP_SUCCESS_YES.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_REAL_ESTATE_LOOKUP_CORRECT.id)
    }

    fun houseProvideLookupSuccess_userDataSqm_thenGoToSqm() {
        userContext.onBoardingData.apply {
            houseType = OnboardingConversationDevi.ProductTypes.RENT.toString()
        }

        val message = testConversation.getMessage(ASK_ADDRESS_LOOK_UP_SUCCESS.id + ".0")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_ADDRESS_LOOK_UP_SUCCESS_YES.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_SQUARE_METERS.id)
    }

    //TODO: Lot's of test that should be added when we have the time
    @Test
    fun houseProvideLookupSuccess_userDataStoreAddress_thenGoToRealEstateLookup() {
        userContext.onBoardingData.apply {
            houseType = OnboardingConversationDevi.ProductTypes.HOUSE.toString()
            addressStreet = "SomeStreet 13"
            addressZipCode = "12345"
        }

        given(
            lookupService.realEstateLookup(
                TestData.TOLVANSSON_MEMBER_ID,
                RealEstateDto("SomeStreet 13", "12345")
            )
        ).willReturn(
            RealEstateResponse(
                21,
                21,
                21,
                RealEstateDrain("", ""),
                200,
                1,
                "",
                RealEstateWater("", ""),
                1930
            )
        )

        val message = testConversation.getMessage(ASK_ADDRESS_LOOK_UP_SUCCESS.id + ".0")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_ADDRESS_LOOK_UP_SUCCESS_YES.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_REAL_ESTATE_LOOKUP_CORRECT.id)
    }

    @Test
    fun houseProvideLookupSuccess_userDataStoreAddress_thenGoToSqm() {
        userContext.onBoardingData.apply {
            houseType = OnboardingConversationDevi.ProductTypes.HOUSE.toString()
            addressStreet = "SomeStreet 13"
            addressZipCode = "12345"
        }

        given(
            lookupService.realEstateLookup(
                TestData.TOLVANSSON_MEMBER_ID,
                RealEstateDto("SomeStreet 13", "12345")
            )
        ).willReturn(null)

        val message = testConversation.getMessage(ASK_ADDRESS_LOOK_UP_SUCCESS.id + ".0")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_ADDRESS_LOOK_UP_SUCCESS_YES.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_SQUARE_METERS.id)
    }

    @Test
    fun houseProvideLookupSuccess_userDataStoreAddress_thenGoToLastName() {
        userContext.onBoardingData.apply {
            houseType = OnboardingConversationDevi.ProductTypes.HOUSE.toString()
            addressStreet = "SomeStreet 13"
            addressZipCode = "12345"
        }

        given(
            lookupService.realEstateLookup(
                TestData.TOLVANSSON_MEMBER_ID,
                RealEstateDto("SomeStreet 13", "12345")
            )
        ).willReturn(null)

        val message = testConversation.getMessage(ASK_ADDRESS_LOOK_UP_SUCCESS.id + ".0")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_ADDRESS_LOOK_UP_SUCCESS_NO.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_STREET_ADDRESS.id)
    }

    @Test
    fun houseProvideRealEstateLookupSuccess_userDataSqm_thenGoToNumberOfBathrooms() {
        userContext.onBoardingData.apply {
            livingSpace = 200f
            houseAncillaryArea = 21
            yearOfConstruction = 1930
        }

        val message = testConversation.getMessage(ASK_REAL_ESTATE_LOOKUP_CORRECT.id + ".2")
        (message!!.body as MessageBodySingleSelect).choices.selectWithValue(SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES.value)

        testConversation.receiveMessage(message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP.id)
    }

//    @Test
//    fun houseProvideRealEstateLookupSuccess_userDataSqm_thenGoToCallSqm_underWriterGuideLines() {
//     userContext.onBoardingData.let {
//            assertThat(it.livingSpace).isEqualTo(200f)
//            assertThat(it.houseAncillaryArea).isEqualTo(21)
//            assertThat(it.yearOfConstruction).isEqualTo(1930)
//        }
//    }
//
//    @Test
//    fun houseProvideRealEstateLookupSuccess_userDataSqm_thenGoToCallTotalSqm_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideRealEstateLookupSuccess_userDataSqm_thenGoToCallYearOfConstruction_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideRealEstateLookupSuccess_thenGoToSqm() {
//
//    }


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


//    @Test
//    fun houseProvideSquareMeters_userDataSquareMeters_thenGoToSqmCall_underWriterGuideLines() {
//
//    }
//
//
//    @Test
//    fun houseFailedLookupProvideSquareMeters_productTypeHouse_userDataSquareMeters_thenGoToAncillary() {
//
//    }
//
//    @Test
//    fun houseFailedLookupProvideSquareMeters_productTypeRent_userDataSquareMeters_thenEndHouseOnboarding() {
//
//    }
//
//    @Test
//    fun houseProvideAncillaryArea_userDataAncillaryArea_thenGoToYearOfConstruction() {
//
//    }
//
//    @Test
//    fun houseProvideAncillaryArea_userDataAncillaryArea_thenGoToTotalSqmCall_underWriter() {
//
//    }
//
//    @Test
//    fun houseProvideYearOfConstruction_userDataYearOfConstruction_thenGoToNumberOfBathRooms() {
//
//    }
//
//    @Test
//    fun houseProvideYearOfConstruction_userDataYearOfConstruction_thenGoToYearOfConstructionCall_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfBathrooms_userDataNumberOfBathrooms_thenGoToAskHouseHold() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfBathrooms_userDataNumberOfBathrooms_thenGoToCallNumberOfBathrooms_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfBathroomsLookupSuccess_userDataNumberOfBathrooms_thenGoToAskHouseHold() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfBathroomsLookupSuccess_userDataNumberOfBathrooms_thenGoToCallNumberOfBathrooms_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideHouseHoldMembers_userDataHouseHoldMembers_thenGoToSubLetting() {
//
//    }
//
//    @Test
//    fun houseProvideHouseHoldMembers_userDataHouseHoldMembers_thenGoToCallHouseHoldMembers_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideSubLetting_userDataYes_thenGoToFloorsFromYes() {
//
//    }
//
//    @Test
//    fun houseProvideSubLetting_userDataYes_thenGoToFloorsFromNo() {
//
//    }
//
//    @Test
//    fun houseProvideFloors_fromNo_userDataNo_thenGoToExtraBuildings() {
//
//    }
//
//    @Test
//    fun houseProvideFloors_fromNo_userDataYes_thenGoToCallFloors_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideFloors_fromYes_userDataNo_thenGoToExtraBuildings() {
//
//    }
//
//    @Test
//    fun houseProvideFloors_fromYes_userDataYes_thenGoToCallFloors_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideHasExtraBuildings_userDataNo_thenGoToConversationDone() {
//
//    }
//
//    @Test
//    fun houseProvideHasExtraBuildings_userDataYes_thenGoToNumberOfExtraBuilding() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfExtraBuilding_userDataZero_thenGoToConversationDone() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfExtraBuilding_userDataOne_thenGoToTypeOne() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfExtraBuilding_userDataTwo_thenGoToTypeMoreThanOne() {
//
//    }
//
//    @Test
//    fun houseProvideNumberOfExtraBuilding_userDataFive_thenGoToCallExtraBuildings_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideExtraBuildType_userDataGarage_thenGoToExtraBuildingSqm() {
//
//    }
//
//    @Test
//    fun houseProvideExtraBuildingSqm_userDataTen_thenGoToExtraWaterConnected() {
//
//    }
//
//    @Test
//    fun houseProvideExtraBuildingSqm_userDataEighty_thenGoToExtraWaterConnected_underWriterGuideLines() {
//
//    }
//
//    @Test
//    fun houseProvideExtraBuildingWaterConnected_userDataYes_thenGoToExtraBuildingTypeInLoop() {
//
//    }
//
//    @Test
//    fun houseProvideExtraBuildingWaterConnected_userDataYes_thenGoToConversationDone() {
//
//    }

    private fun ArrayList<SelectItem>.selectWithValue(value: String) {
        this.forEach {
            if (it.value == value) it.selected = true
        }
    }
}
