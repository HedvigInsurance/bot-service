package com.hedvig.botService.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.FileUploadedEvent;
import com.hedvig.botService.services.events.OnboardingFileUploadedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

@RunWith(MockitoJUnitRunner.class)
public class FreeChatConversationTest {

  private static final String TEST_KEY = "TestKey";
  private static final String TEST_TYPE = "TestType";
  @Mock
  private StatusBuilder statusBuilder;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private ProductPricingService productPricingService;

  private FreeChatConversation testFreeChatConversation;
  private UserContext userContext;

  private static final String MEMBER_ID = "12345";

  @Before
  public void SetUp() {
    testFreeChatConversation = new FreeChatConversation(statusBuilder, eventPublisher,
      productPricingService);
    userContext = new UserContext(MEMBER_ID);
  }


  @Test
  public void Should_ReturnAFreeChatStart_WhenInitializeFreeChatConversation() {

    userContext.putUserData("{NAME}", "TestName");

    testFreeChatConversation.init(userContext);

    assertThat(userContext.getMemberChat().chatHistory.get(0).id)
      .startsWith(FreeChatConversation.FREE_CHAT_START);

    assertThat(userContext.getMemberChat().chatHistory.get(0).body.text)
      .isEqualTo("Hej TestName! Hur kan jag hjälpa dig idag?");
  }

  @Test
  public void Should_SendOnboardingFileUploadedEventWithKeyAndType_WhenUserUploadsFile() {

    userContext.putUserData("{NAME}", "TestName");

    when(productPricingService.getInsuranceStatus(anyString())).thenReturn(null);

    Message m = testFreeChatConversation
      .getMessage(FreeChatConversation.FREE_CHAT_ONBOARDING_START);
    m.body = new MessageBodyFileUpload("TestContent", TEST_KEY, TEST_TYPE);

    testFreeChatConversation.receiveMessage(userContext, m);

    then(eventPublisher)
      .should()
      .publishEvent(
        new OnboardingFileUploadedEvent(
          userContext.getMemberId(),
          TEST_KEY,
          TEST_TYPE));
  }

  @Test
  public void Should_SendFileUploadedEventWithKeyAndType_WhenUserUploadsFileAndInsuranceIsSigned() {

    userContext.putUserData("{NAME}", "TestName");

    when(productPricingService.getInsuranceStatus(anyString())).thenReturn("SIGNED");

    Message m = testFreeChatConversation
      .getMessage(FreeChatConversation.FREE_CHAT_ONBOARDING_START);
    m.body = new MessageBodyFileUpload("TestContent", TEST_KEY, TEST_TYPE);

    testFreeChatConversation.receiveMessage(userContext, m);

    then(eventPublisher)
      .should()
      .publishEvent(
        new FileUploadedEvent(
          userContext.getMemberId(),
          TEST_KEY,
          TEST_TYPE));
  }
}
