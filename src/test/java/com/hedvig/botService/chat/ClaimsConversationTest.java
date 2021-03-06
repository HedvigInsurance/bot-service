package com.hedvig.botService.chat;

import com.google.common.collect.Iterables;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyAudio;
import com.hedvig.botService.enteties.message.MessageBodyParagraph;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.ClaimAudioReceivedEvent;
import com.hedvig.botService.services.events.ClaimCallMeEvent;
import com.hedvig.libs.translations.Translations;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static com.hedvig.botService.chat.ClaimsConversation.*;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimsConversationTest {

  public static final String AUDIO_RECORDING_URL = "https://someS3.com/someUUID";
  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private ClaimsService claimsService;

  @Mock private ProductPricingService productPricingService;

  @Mock private ConversationFactory conversationFactory;

  @Mock private MemberService memberService;

  @Mock
  private Translations translations;

  private ClaimsConversation testConversation;
  private UserContext userContext;

  @Before
  public void setUp() {

    userContext = new UserContext(TOLVANSSON_MEMBER_ID);
    testConversation =
        new ClaimsConversation(
            eventPublisher, claimsService, productPricingService, conversationFactory, memberService, translations, userContext);
  }

  @Test
  public void AudioReceived_SendsClaimAudioReceivedEvent_AndCreatesClaimInClaimsService2() {
    Message m = testConversation.getMessage("message.claims.audio");
    val body = (MessageBodyAudio) m.body;
    body.url = AUDIO_RECORDING_URL;
    testConversation.receiveMessage(m);

    then(eventPublisher)
        .should()
        .publishEvent(new ClaimAudioReceivedEvent(userContext.getMemberId()));
    then(claimsService).should().createClaimFromAudio(anyString(), eq(AUDIO_RECORDING_URL));
  }

  @Test
  public void init_WhenMemberInsuranceIsActive_StartsClaimFlow() {
    testConversation.init();

    assertThat(userContext.getMemberChat().chatHistory.get(0).id)
        .startsWith(ClaimsConversation.MESSAGE_CLAIMS_START);
  }

  @Test
  public void callMe_WhenMemberInsuranceIsActive_SendsClaimCallMeEventActiveTrue() {
    Message m = testConversation.getMessage(ClaimsConversation.MESSAGE_CLAIM_CALLME);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    testConversation.receiveMessage(m);

    then(eventPublisher)
        .should()
        .publishEvent(
            new ClaimCallMeEvent(
                userContext.getMemberId(),
                userContext.getOnBoardingData().getFirstName(),
                userContext.getOnBoardingData().getFamilyName(),
                m.body.text,
                true));
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsMissing_ThenQuestionAboutPhoneShouldPopUp(){
    userContext.getOnBoardingData().setPhoneNumber(null);

    testConversation.receiveEvent(Conversation.EventTypes.MESSAGE_FETCHED, MESSAGE_CLAIMS_START);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_PHONE);
  }

  @Test
  public void givenPhoneNumberIsPresent_WhenStartsClaimsChat_ThenQuestionAboutChangePhoneShouldPopUp() {
    userContext.getOnBoardingData().setPhoneNumber("0701234567");

    testConversation.receiveEvent(Conversation.EventTypes.MESSAGE_FETCHED, MESSAGE_CLAIMS_START);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_EXISTING_PHONE);
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsMissingAndProvided_ThenClaimsRecord1MessagePopsUp(){
    userContext.getOnBoardingData().setPhoneNumber(null);

    Message m = testConversation.getMessage(MESSAGE_CLAIMS_ASK_PHONE);
    m.body.text = TOLVANSSON_PHONE_NUMBER;
    testConversation.handleMessage(m);

    assertThat(userContext.getMemberChat().chatHistory.get(1).id).matches(MESSAGE_CLAIMS_RECORD_1);
    assertThat(userContext.getDataEntry(PHONE_NUMBER)).isEqualTo(TOLVANSSON_PHONE_NUMBER);
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsPresentButMemberWantsToChange__ThenQuestionAboutNewPhoneShouldPopUp(){
    userContext.getOnBoardingData().setPhoneNumber("0700700707");
    Message m = testConversation.getMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    testConversation.handleMessage(m);

    val lastMessage = userContext.getMemberChat().chatHistory.get(0);
    assertThat(lastMessage.body.text).isEqualTo(TOLVANSSON_PHONE_NUMBER);

    assertThat(userContext.getMemberChat().chatHistory.get(1).id).matches(MESSAGE_CLAIMS_RECORD_1);
    assertThat(userContext.getDataEntry(PHONE_NUMBER)).isEqualTo(TOLVANSSON_PHONE_NUMBER);
  }
}
