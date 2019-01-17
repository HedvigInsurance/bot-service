package com.hedvig.botService.chat;

import static com.hedvig.botService.chat.ClaimsConversation.MESSAGE_CLAIMS_ASK_EXISTING_PHONE;
import static com.hedvig.botService.chat.ClaimsConversation.MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW;
import static com.hedvig.botService.chat.ClaimsConversation.MESSAGE_CLAIMS_ASK_PHONE;
import static com.hedvig.botService.chat.ClaimsConversation.MESSAGE_CLAIMS_ASK_PHONE_END;
import static com.hedvig.botService.chat.ClaimsConversation.PHONE_NUMBER;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

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
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

@RunWith(MockitoJUnitRunner.class)
public class ClaimsConversationTest {

  public static final String AUDIO_RECORDING_URL = "https://someS3.com/someUUID";
  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private ClaimsService claimsService;

  @Mock private ProductPricingService productPricingService;

  @Mock private ConversationFactory conversationFactory;

  @Mock private MemberService memberService;

  private ClaimsConversation testConversation;
  private UserContext userContext;

  @Before
  public void setUp() {

    testConversation =
        new ClaimsConversation(
            eventPublisher, claimsService, productPricingService, conversationFactory, memberService);
    userContext = new UserContext(TOLVANSSON_MEMBER_ID);
  }

  @Test
  public void AudioReceived_SendsClaimAudioReceivedEvent_AndCreatesClaimInClaimsService2() {
    Message m = testConversation.getMessage("message.claims.audio");
    val body = (MessageBodyAudio) m.body;
    body.url = AUDIO_RECORDING_URL;
    testConversation.receiveMessage(userContext, m);

    then(eventPublisher)
        .should()
        .publishEvent(new ClaimAudioReceivedEvent(userContext.getMemberId()));
    then(claimsService).should().createClaimFromAudio(anyString(), eq(AUDIO_RECORDING_URL));
  }

  @Test
  public void init_WhenMemberInsuranceIsInactive_StartsNotActiveFlow() {
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(false);

    testConversation.init(userContext);

    Message msg;
    boolean messageIsParagraph;
    int i = 0;
    do {
      msg = Iterables.getLast(userContext.getMemberChat().chatHistory);
      messageIsParagraph = MessageBodyParagraph.class.isInstance(msg.body);
      if (messageIsParagraph) {
        testConversation.receiveEvent(Conversation.EventTypes.MESSAGE_FETCHED, msg.id, userContext);
      }
    } while (messageIsParagraph && i++ < 20);

    assertThat(msg.body).isNotInstanceOf(MessageBodyParagraph.class);
    assertThat(msg.id).startsWith(ClaimsConversation.MESSAGE_CLAIMS_NOT_ACTIVE);
  }

  @Test
  public void init_WhenMemberInsuranceIsActive_StartsClaimFlow() {
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    testConversation.init(userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id)
        .startsWith(ClaimsConversation.MESSAGE_CLAIMS_START);
  }

  @Test
  public void callMe_WhenMemberInsuranceIsActive_SendsClaimCallMeEventActiveTrue() {
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    Message m = testConversation.getMessage(ClaimsConversation.MESSAGE_CLAIM_CALLME);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    testConversation.receiveMessage(userContext, m);

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
  public void callMe_WhenMemberInsuranceIsInactive_SendsClaimCallMeEventActiveFalse() {
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(false);

    Message m = testConversation.getMessage(ClaimsConversation.MESSAGE_CLAIM_CALLME);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    testConversation.receiveMessage(userContext, m);

    then(eventPublisher)
        .should()
        .publishEvent(
            new ClaimCallMeEvent(
                userContext.getMemberId(),
                userContext.getOnBoardingData().getFirstName(),
                userContext.getOnBoardingData().getFamilyName(),
                m.body.text,
                false));
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsMissing_ThenQuestionAboutPhoneShouldPopUp(){
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    userContext.getOnBoardingData().setPhoneNumber(null);

    testConversation.receiveEvent(Conversation.EventTypes.MESSAGE_FETCHED, ClaimsConversation.MESSAGE_CLAIMS_OK, userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_PHONE);
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsMissingAndProvided_ThenToppenMessagePopsUp(){
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    userContext.getOnBoardingData().setPhoneNumber(null);

    Message m = testConversation.getMessage(MESSAGE_CLAIMS_ASK_PHONE);
    m.body.text = TOLVANSSON_PHONE_NUMBER;
    testConversation.handleMessage(userContext,m);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_PHONE_END);
    assertThat(userContext.getDataEntry(PHONE_NUMBER)).isEqualTo(TOLVANSSON_PHONE_NUMBER);
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsPresent_ThenQuestionAboutPhoneShouldPopUp(){
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    userContext.getOnBoardingData().setPhoneNumber(TOLVANSSON_PHONE_NUMBER);

    testConversation.receiveEvent(Conversation.EventTypes.MESSAGE_FETCHED, ClaimsConversation.MESSAGE_CLAIMS_OK, userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_EXISTING_PHONE);
  }

  @Test
  public void givenThatMemberCanReportClaim_WhenPhoneNumberIsPresentButMemberWantsToChange__ThenQuestionAboutNewPhoneShouldPopUp(){
    when(productPricingService.isMemberInsuranceActive(TOLVANSSON_MEMBER_ID)).thenReturn(true);

    userContext.getOnBoardingData().setPhoneNumber("0700700707");
    Message m = testConversation.getMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    testConversation.handleMessage(userContext, m);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id).matches(MESSAGE_CLAIMS_ASK_PHONE_END);
    assertThat(userContext.getDataEntry(PHONE_NUMBER)).isEqualTo(TOLVANSSON_PHONE_NUMBER);
  }

}
