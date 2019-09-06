package com.hedvig.botService.chat;

import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;

import com.hedvig.botService.enteties.UserContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

@RunWith(MockitoJUnitRunner.class)
public class CallMeConversationTest {

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private ConversationFactory conversationFactory;

  private CallMeConversation testCallMeConversation;
  private UserContext userContext;

  private static final String PHONE_NUMBER = "{PHONE_NUMBER}";

  @Before
  public void SetUp() {
    testCallMeConversation = new CallMeConversation(eventPublisher);
    userContext = new UserContext(TOLVANSSON_MEMBER_ID);
  }

  @Test
  public void Should_ReturnACallMeStartMessage_WhenPhoneNumberIsInUserContext() {

    userContext.putUserData(PHONE_NUMBER, TOLVANSSON_PHONE_NUMBER);

    testCallMeConversation.init(userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id)
      .startsWith(CallMeConversation.CALLME_CHAT_START);
  }

  @Test
  public void Should_ReturnACallMeStartMessageWithoutPhone_WhenPhoneNumberIsNotInUserContext() {
    testCallMeConversation.init(userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id)
      .startsWith(CallMeConversation.CALLME_CHAT_START_WITHOUT_PHONE);
  }

}
