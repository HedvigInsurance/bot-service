package com.hedvig.botService.chat


import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hedvig.botService.chat.Conversation.EventTypes
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_50K_LIMIT_YES_YES
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_BANKIDJA
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_LAGENHET_ADDRESSNOTFOUND
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_MEMBER_UNDER_EIGHTEEN
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_NAGOTMER
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_ONBOARDINGSTART_REPLY_NAME
import com.hedvig.botService.chat.OnboardingConversationDevi.Companion.MESSAGE_VARBORDUFELADRESS
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.enteties.userContextHelpers.UserData
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.Address
import com.hedvig.botService.serviceIntegration.memberService.dto.LookupResponse
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService
import com.hedvig.botService.services.LocalizationService
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent
import com.hedvig.botService.services.events.RequestObjectInsuranceEvent
import com.hedvig.botService.services.events.UnderwritingLimitExcededEvent
import com.hedvig.botService.testHelpers.TestData
import com.hedvig.botService.testHelpers.TestData.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class OnboardingConversationDeviTest {

    @Mock
    private lateinit var memberService: MemberService
    @Mock
    private lateinit var localizationService: LocalizationService

    @Mock
    private lateinit var productPricingService: ProductPricingService

    @Mock
    private lateinit var publisher: ApplicationEventPublisher

    @Mock
    private lateinit var conversationFactory: ConversationFactory

    @Mock
    private lateinit var phoneNumberUtil: PhoneNumberUtil

    private lateinit var userContext: UserContext
    private lateinit var testConversation: OnboardingConversationDevi


    @Before
    fun setup() {
        userContext = UserContext(TOLVANSSON_MEMBER_ID)
        userContext.putUserData(UserData.HOUSE, TOLVANSSON_PRODUCT_TYPE)

        testConversation = OnboardingConversationDevi(
            memberService, productPricingService, publisher, conversationFactory,localizationService, "test", "test", phoneNumberUtil
        )
    }

    @Test
    fun clearMembersAddress_WhenMemberEntersAddressManually() {
        addSsnToContext(userContext, TOLVANSSON_SSN)
        addFirstnameToContext(userContext, TOLVANSSON_FIRSTNAME)
        addFamilynameToContext(userContext, TOLVANSSON_LASTNAME)
        TestData.addBirthDateToContext(userContext, TestData.TOLVANSSON_BIRTH_DATE)

        addFloorToContext(userContext, TestData.TOLVANSSON_FLOOR)
        addStreetToContext(userContext, TestData.TOLVANSSON_STREET)
        addCityToContext(userContext, TestData.TOLVANSSON_CITY)
        addZipCodeToContext(userContext, TestData.TOLVANSSON_ZIP)

        val m = testConversation.getMessage("message.bankidja.0")
        val body = m!!.body as MessageBodySingleSelect
        body.choices[1].selected = true

        testConversation.receiveMessage(userContext, m)

        val onBoardingData = userContext.onBoardingData
        assertThat(onBoardingData.addressCity).isNull()
        assertThat(onBoardingData.addressStreet).isNull()
        assertThat(onBoardingData.addressZipCode).isNull()
        assertThat(onBoardingData.floor).isZero()
    }

    @Test
    fun sendNotificationEventOn_HousingUnderWritingLimit() {

        addFirstnameToContext(userContext, TOLVANSSON_FIRSTNAME)
        addFamilynameToContext(userContext, TOLVANSSON_LASTNAME)

        val m = testConversation.getMessage("message.uwlimit.housingsize")
        m!!.body.text = TOLVANSSON_PHONE_NUMBER

        testConversation.receiveMessage(userContext, m)

        then<ApplicationEventPublisher>(publisher)
            .should()
            .publishEvent(
                UnderwritingLimitExcededEvent(
                    TOLVANSSON_MEMBER_ID,
                    TOLVANSSON_PHONE_NUMBER,
                    TOLVANSSON_FIRSTNAME,
                    TOLVANSSON_LASTNAME,
                    UnderwritingLimitExcededEvent.UnderwritingType.HouseingSize
                )
            )
    }

    @Test
    fun sendNotificationEventOn_HouseholdUnderWritingLimit() {

        addFirstnameToContext(userContext, TOLVANSSON_FIRSTNAME)
        addFamilynameToContext(userContext, TOLVANSSON_LASTNAME)

        val m = testConversation.getMessage("message.uwlimit.householdsize")
        m!!.body.text = TOLVANSSON_PHONE_NUMBER

        testConversation.receiveMessage(userContext, m)

        then<ApplicationEventPublisher>(publisher)
            .should()
            .publishEvent(
                UnderwritingLimitExcededEvent(
                    TOLVANSSON_MEMBER_ID,
                    TOLVANSSON_PHONE_NUMBER,
                    TOLVANSSON_FIRSTNAME,
                    TOLVANSSON_LASTNAME,
                    UnderwritingLimitExcededEvent.UnderwritingType.HouseholdSize
                )
            )
    }

    @Test
    fun sendNotificationEventOn_FriFraga() {
        addFirstnameToContext(userContext, TOLVANSSON_LASTNAME)
        addFamilynameToContext(userContext, TOLVANSSON_FIRSTNAME)

        val m = testConversation.getMessage("message.frifraga")
        m!!.body.text = "I wonder if I can get a home insurance, even thouh my name is Tolvan?"

        testConversation.receiveMessage(userContext, m)

        then<ApplicationEventPublisher>(publisher)
            .should()
            .publishEvent(OnboardingQuestionAskedEvent(TOLVANSSON_MEMBER_ID, m.body.text))
    }

    @Test
    fun doNotSendNotificationEvent_WhenMessage_50K_LIMIT_YES_withAnswer_MESSAGE_50K_LIMIT_YES_YES() {
        val m = testConversation.getMessage(
            OnboardingConversationDevi.MESSAGE_50K_LIMIT_YES + ".2"
        )
        val choice = (m!!.body as MessageBodySingleSelect)
            .choices
            .stream()
            .filter { x -> x.value.equals(MESSAGE_50K_LIMIT_YES_YES, ignoreCase = true) }
            .findFirst()

        choice.get().selected = true

        testConversation.receiveMessage(userContext, m)
        then<ApplicationEventPublisher>(publisher)
            .should(times(0))
            .publishEvent(
                RequestObjectInsuranceEvent(TOLVANSSON_MEMBER_ID, TOLVANSSON_PRODUCT_TYPE)
            )
    }

    @Test
    fun sendNotificationEvent_WhenMemberSignedIsCalled_withUserContextValue50K_LIMITeqTRUE() {
        val referenceId = "53bb6e92-5cc7-11e8-8c3b-235d0786c76b"
        userContext.putUserData("{50K_LIMIT}", "true")
        testConversation.memberSigned(referenceId, userContext)
        then<ApplicationEventPublisher>(publisher)
            .should(times(1))
            .publishEvent(
                RequestObjectInsuranceEvent(TOLVANSSON_MEMBER_ID, TOLVANSSON_PRODUCT_TYPE)
            )
    }

    @Test
    fun doNothing_WhenMemberSignedIsCalled_withUserContextValue50K_LIMITeqNULL() {
        val referenceId = "53bb6e92-5cc7-11e8-8c3b-235d0786c76b"
        testConversation.memberSigned(referenceId, userContext)
        then<ApplicationEventPublisher>(publisher)
            .should(times(0))
            .publishEvent(
                RequestObjectInsuranceEvent(TOLVANSSON_MEMBER_ID, TOLVANSSON_PRODUCT_TYPE)
            )
    }

    @Test
    fun canAlwaysAcceptAnswersToQuestion() {

        val uc = UserContext(TOLVANSSON_MEMBER_ID)
        val canAcceptAnswer = testConversation.canAcceptAnswerToQuestion(uc)

        assertThat(canAcceptAnswer).isEqualTo(true)
    }

    @Test
    fun addCorrectStartMessage_WhenInitWithMessageId() {

        testConversation.init(
            userContext,
            OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME
        )

        assertThat(userContext.memberChat.chatHistory)
            .first()
            .hasFieldOrPropertyWithValue(
                "id", OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME
            )
    }

    @Test
    fun addMessageOnboardingStartShort_WhenCallingInit_WithoutMessageId() {

        testConversation.init(userContext)

        assertThat(userContext.memberChat.chatHistory)
            .first()
            .hasFieldOrPropertyWithValue(
                "id", OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME
            )
    }

    @Test
    fun relayToMessageForslagsstart_FromMessageOboardingstartShort() {

        testConversation.receiveEvent(
            EventTypes.MESSAGE_FETCHED,
            testConversation.findLastChatMessageId(
                OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_SHORT
            ),
            userContext
        )

        assertThat(userContext.memberChat.chatHistory)
            .first()
            .hasFieldOrPropertyWithValue(
                "id",
                OnboardingConversationDevi.MESSAGE_FORSLAGSTART
            )
    }

    @Test
    fun messageOnboardingStart_ContainsOptionForExistingMembers() {
        val message = testConversation.getMessage(
            testConversation.findLastChatMessageId(
                OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART
            )
        )

        val body = message!!.body as MessageBodySingleSelect

        assertThat<SelectItem>(body.choices)
            .extracting("text", "value")
            .contains(tuple("Jag är redan medlem", "message.bankid.start"))
    }

    @Test
    fun messageAskName_whenMemberEntersTheirName_capitalizedNameCorrectly() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)

        val body = message.body as MessageBodyText
        body.text = "TOLVAN"

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("TOLVAN")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_ONBOARDINGSTART_REPLY_NAME)
        assertThat(lastMessage.body.text).contains("Tolvan")

        assertThat(userContext.onBoardingData.firstName).isEqualTo("Tolvan")
    }

    @Test
    fun messageAskName_whenMemberGreetsHedvigWithTheirName_removesGreeting() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)

        val body = message.body as MessageBodyText
        body.text = "Hej jag heter Tolvan"

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("Hej jag heter Tolvan")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_ONBOARDINGSTART_REPLY_NAME)
        assertThat(lastMessage.body.text).isEqualTo("Trevligt att träffas Tolvan!")

        assertThat(userContext.onBoardingData.firstName).isEqualTo("Tolvan")
    }

    @Test
    fun messageAskName_whenMemberHasNameThatIsAlmostGreeting_doneNotremovePartOfName() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)

        val body = message.body as MessageBodyText
        body.text = "Hejs jag heter Tolvan"

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("Hejs jag heter Tolvan")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_ONBOARDINGSTART_REPLY_NAME)
        assertThat(lastMessage.body.text).isEqualTo("Trevligt att träffas Hejs Jag Heter Tolvan!")

        assertThat(userContext.onBoardingData.firstName).isEqualTo("Hejs Jag Heter Tolvan")
    }

    @Test
    fun messageAskName_whenMemberGreetsHedvigWithPuncation_removesGreeting() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)

        val body = message.body as MessageBodyText
        body.text = "Hej, jag heter Tolvan"

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("Hej, jag heter Tolvan")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_ONBOARDINGSTART_REPLY_NAME)
        assertThat(lastMessage.body.text).isEqualTo("Trevligt att träffas Tolvan!")

        assertThat(userContext.onBoardingData.firstName).isEqualTo("Tolvan")
    }

    @Test
    fun messageAskName_whenMemberHasPuncationAfterName_removesPuncation() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)

        val body = message.body as MessageBodyText
        body.text = "Tolvan!"

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("Tolvan!")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_ONBOARDINGSTART_REPLY_NAME)
        assertThat(lastMessage.body.text).isEqualTo("Trevligt att träffas Tolvan!")

        assertThat(userContext.onBoardingData.firstName).isEqualTo("Tolvan")
    }


    @Test
    fun messageAskEmail_setsKeyoardTypeTo_email() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_EMAIL)

        assertThat((message.body as MessageBodyText).keyboardType).isEqualTo(KeyboardType.EMAIL_ADDRESS)

    }

    @Test
    fun messageAskEmail_always_forwardsEmailToMemberService() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_EMAIL)
        message.body.text = "tolvan@hej.com"

        testConversation.receiveMessage(userContext, message)

        then(memberService).should(times(1)).updateEmail(TOLVANSSON_MEMBER_ID, "tolvan@hej.com")

    }


    @Test
    fun lookupAddressDetails_whenMemberEntersTheirSSN() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val body = message.body as MessageBodyNumber
        body.text = "191212121212"

        given(memberService.lookupAddressSWE("191212121212", "1337")).willReturn(
            LookupResponse(
                "Tolvan",
                "Tolvansson",
                Address("SomeStreet 13", "Stockholm", "12345", "1004", 0)
            )
        )

        testConversation.receiveMessage(userContext, message)
        assertThat(userContext.memberChat.chatHistory.findLast { it.id == message.id }?.body?.text).contains("19121212-****")

        val paragraphMessage = userContext.memberChat.chatHistory.last()
        testConversation.receiveEvent(EventTypes.MESSAGE_FETCHED, paragraphMessage.id, userContext = userContext)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_BANKIDJA)
        assertThat(lastMessage.body.text).contains("SomeStreet 13")

        userContext.onBoardingData.let {
            assertThat(it.ssn).isEqualTo("191212121212")
            assertThat(it.firstName).isEqualTo("Tolvan")
            assertThat(it.familyName).isEqualTo("Tolvansson")
            assertThat(it.birthDate).isEqualTo(LocalDate.parse("1912-12-12"))
            assertThat(it.addressStreet).isEqualTo("SomeStreet 13")
            assertThat(it.addressZipCode).isEqualTo("12345")
            assertThat(it.addressCity).isEqualTo("Stockholm")
            assertThat(it.floor).isEqualTo(0)
        }

    }

    @Test
    fun lookupAddressDetails_whenAddressLookupReturnsNullAddress() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val body = message.body as MessageBodyNumber
        body.text = "191212121212"

        given(memberService.lookupAddressSWE("191212121212", "1337")).willReturn(
            LookupResponse(
                "Tolvan",
                "Tolvansson",
                null
            )
        )

        testConversation.receiveMessage(userContext, message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo("message.lagenhet.addressnotfound")

        userContext.onBoardingData.let {
            assertThat(it.ssn).isEqualTo("191212121212")
            assertThat(it.firstName).isEqualTo("Tolvan")
            assertThat(it.familyName).isEqualTo("Tolvansson")
            assertThat(it.birthDate).isEqualTo(LocalDate.parse("1912-12-12"))
            assertThat(it.addressStreet).isNull()
            assertThat(it.addressZipCode).isNull()
            assertThat(it.addressCity).isNull()
            assertThat(it.floor).isEqualTo(0)
        }
    }

    @Test
    fun lookupAddressDetails_whenAddressLookupReturnsNoMatch() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val body = message.body as MessageBodyNumber
        body.text = "191212121212"

        given(memberService.lookupAddressSWE("191212121212", "1337")).willReturn(null)

        testConversation.receiveMessage(userContext, message)

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo("message.lagenhet.addressnotfound")

        userContext.onBoardingData.let {
            assertThat(it.ssn).isEqualTo("191212121212")
            assertThat(it.familyName).isNull()
            assertThat(it.birthDate).isEqualTo(LocalDate.parse("1912-12-12"))
            assertThat(it.addressStreet).isNull()
            assertThat(it.addressZipCode).isNull()
            assertThat(it.addressCity).isNull()
            assertThat(it.floor).isEqualTo(0)
        }
    }

    @Test
    fun receiveMessageLagenhetAddressnotfound_whenMemberHasEnteredLastnameInFirstname_thenRemoveLastName() {

        userContext.onBoardingData.firstName = "Tolvan tolvansson"

        val message = getMessage("message.lagenhet.addressnotfound")

        val body = message.body as MessageBodyText
        body.text = "Tolvansson"

        testConversation.receiveMessage(userContext, message)

        userContext.onBoardingData.let {
            assertThat(it.firstName).isEqualTo("Tolvan")
            assertThat(it.familyName).isEqualTo("Tolvansson")
        }
    }

    @Test
    fun receiveMessageLagenhetAddressnotfound_whenMemberHasOneNameInFirstnameThatMatchesLastname_thenDoNotRemoveLastName() {

        userContext.onBoardingData.firstName = "Tolvan"

        val message = getMessage("message.lagenhet.addressnotfound")

        val body = message.body as MessageBodyText
        body.text = "Tolvan"

        testConversation.receiveMessage(userContext, message)

        userContext.onBoardingData.let {
            assertThat(it.firstName).isEqualTo("Tolvan")
            assertThat(it.familyName).isEqualTo("Tolvan")
        }
    }

    @Test
    fun receiveMessageLagenhetAddressnotfound_whenMemberHasTwoNamesInFirstnameThatMatchesLastname_thenDoNotRemoveLastName() {

        userContext.onBoardingData.firstName = "A Tolvan"

        val message = getMessage("message.lagenhet.addressnotfound")

        val body = message.body as MessageBodyText
        body.text = "A Tolvan"

        testConversation.receiveMessage(userContext, message)

        userContext.onBoardingData.let {
            assertThat(it.firstName).isEqualTo("A Tolvan")
            assertThat(it.familyName).isEqualTo("A Tolvan")
        }
    }

    @Test
    fun receiveMessageLagenhetAddressnotfound_then() {
        val message = getMessage("message.lagenhet.addressnotfound")
        message.body.text = "tolvansson"

        testConversation.receiveMessage(userContext, message)

        userContext.onBoardingData.let {
            assertThat(it.familyName).isEqualTo("Tolvansson")
        }

        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo("message.varborduadress")

    }

    @Test
    fun receiveMessageBANKIDJA_whenAddressIsWrong_removeAddressFromUserContext() {
        userContext.onBoardingData.let {
            it.addressCity = "Stockholm"
            it.addressStreet = "Somestreet 13"
            it.addressZipCode = "12345"
        }

        val message = getMessage(OnboardingConversationDevi.MESSAGE_BANKIDJA)
        (message.body as MessageBodySingleSelect).choices.findLast { it.value == MESSAGE_VARBORDUFELADRESS }!!.selected =
                true

        testConversation.receiveMessage(userContext, message)

        userContext.onBoardingData.let {
            assertThat(it.addressCity).isNull()
            assertThat(it.addressStreet).isNull()
            assertThat(it.addressZipCode).isNull()
        }
    }

    @Test
    fun receiveMessageBANKIDJA_whenAddressIsWrong_nextMessageIs_MESSAGE_VARBORDUFELADRESS() {

        val message = getMessage(OnboardingConversationDevi.MESSAGE_BANKIDJA)
        (message.body as MessageBodySingleSelect).choices.findLast { it.value == MESSAGE_VARBORDUFELADRESS }!!.selected =
                true

        testConversation.receiveMessage(userContext, message)
        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_VARBORDUFELADRESS)
    }

    @Test
    fun dontAskForTheEmailAddress_whenHouseFlow_nextMessageIs_MESSAGE_NAGOTMER(){

        val remarkableEmail = "remarkable.testing@hedvig.com"

        userContext.onBoardingData.email = remarkableEmail
        val message = getMessage(OnboardingConversationDevi.MESSAGE_HUS)
        (message.body as MessageBodySingleSelect).choices.findLast { it.value == MESSAGE_NAGOTMER }!!.selected = true

        testConversation.receiveMessage(userContext, message)
        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_NAGOTMER)
        assertThat(userContext.onBoardingData.newsLetterEmail).isEqualTo(remarkableEmail)

    }

    fun getMessage(id: String): Message {
        return testConversation.getMessage(testConversation.findLastChatMessageId(id))!!
    }

    @Test
    fun recieveMessageIsUnderEighteenIfPersonnummerShowsMemberIsUnder18() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val dateToday = LocalDate.now()

        val date18YearsAgo = dateToday.minusYears(18)
        val dateOneDayYoungerThan18AsString = date18YearsAgo.plusDays(1).toString().replace("-", "")
        val testPersonnummerAsString = dateOneDayYoungerThan18AsString + "2125"

        message.body.text = testPersonnummerAsString

        testConversation.receiveMessage(userContext, message)
        val lastMessage = userContext.memberChat.chatHistory.last()
        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_MEMBER_UNDER_EIGHTEEN)
    }


    @Test
    fun recieveNormalFlowIfMemberIsOlderThan18() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val dateToday = LocalDate.now()

        val date18YearsAgo = dateToday.minusYears(18)
        val dateOneDayOlderThan18AsString = date18YearsAgo.minusDays(1).toString().replace("-", "")
        val testPersonnummerAsString = dateOneDayOlderThan18AsString + "2125"

        message.body.text = testPersonnummerAsString

        testConversation.receiveMessage(userContext, message)
        val lastMessage = userContext.memberChat.chatHistory.last()

        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_LAGENHET_ADDRESSNOTFOUND)
    }

    @Test
    fun recieveNormalFlowIfMemberIs18() {
        val message = getMessage(OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER)

        val dateToday = LocalDate.now()

        val date18YearsAgo = dateToday.minusYears(18).toString().replace("-", "")
        val testPersonnummerAsString = date18YearsAgo + "2125"

        message.body.text = testPersonnummerAsString

        testConversation.receiveMessage(userContext, message)
        val lastMessage = userContext.memberChat.chatHistory.last()

        assertThat(lastMessage.baseMessageId).isEqualTo(MESSAGE_LAGENHET_ADDRESSNOTFOUND)
    }
}
