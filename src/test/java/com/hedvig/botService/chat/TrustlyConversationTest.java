package com.hedvig.botService.chat;

import static com.hedvig.botService.chat.TrustlyConversation.START;
import static com.hedvig.botService.enteties.UserContext.TRUSTLY_FORCED_START;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_EMAIL;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_FIRSTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_LASTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_SSN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import com.hedvig.botService.enteties.DirectDebitMandateTrigger.TriggerStatus;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.services.triggerService.TriggerService;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.test.mock.mockito.MockBean;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyConversationTest {

  public static final UUID TRIGGER_UUID = UUID.randomUUID();
  @Mock MemberService memberService;
  @Mock TriggerService triggerService;
  @Mock ConversationFactory factory;
  private UserContext userContext;
  private TrustlyConversation testConversation;

  @MockBean
  private ApplicationEventPublisher applicationEventPublisher;

  @Before
  public void setup() {
    userContext = new UserContext(TOLVANSSON_MEMBER_ID);
    testConversation = new TrustlyConversation(triggerService, memberService, applicationEventPublisher);
  }

  public void addTolvansonToUserContext() {
    final UserData onBoardingData = userContext.getOnBoardingData();
    onBoardingData.setSSN(TOLVANSSON_SSN);
    onBoardingData.setFirstName(TOLVANSSON_FIRSTNAME);
    onBoardingData.setFamilyName(TOLVANSSON_LASTNAME);
    onBoardingData.setEmail(TOLVANSSON_EMAIL);
  }

  @Test
  public void addingStartMessageToChat_initializes_directDebitTrigger() {

    UUID triggerUUID = UUID.randomUUID();

    addTolvansonToUserContext();

    given(
            triggerService.createTrustlyDirectDebitMandate(
                TOLVANSSON_SSN,
                TOLVANSSON_FIRSTNAME,
                TOLVANSSON_LASTNAME,
                TOLVANSSON_EMAIL,
                TOLVANSSON_MEMBER_ID))
        .willReturn(triggerUUID);

    // ACT
    testConversation.addToChat(START, userContext);

    assertThat(userContext.getDataEntry("{TRUSTLY_TRIGGER_ID}")).isEqualTo(triggerUUID.toString());
  }

  @Test
  public void responding_to_START_addNoNewMessageToChat() {

    final Message message = testConversation.getMessage(START + ".4");
    ((MessageBodySingleSelect) message.body).choices.get(0).selected = true;

    addTolvansonToUserContext();

    given(
            triggerService.createTrustlyDirectDebitMandate(
                TOLVANSSON_SSN,
                TOLVANSSON_FIRSTNAME,
                TOLVANSSON_LASTNAME,
                TOLVANSSON_EMAIL,
                TOLVANSSON_MEMBER_ID))
        .willReturn(TRIGGER_UUID);

    testConversation.receiveMessage(userContext, message);

    assertThat(userContext.getMemberChat().chatHistory.size()).isEqualTo(1);
  }
}
