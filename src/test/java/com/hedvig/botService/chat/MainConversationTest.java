package com.hedvig.botService.chat;

import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_FIRSTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_LASTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PHONE_NUMBER;
import static org.mockito.BDDMockito.then;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class MainConversationTest {

  public static final String QUESTION = "A short but sweet question";
  @Mock ApplicationEventPublisher eventPublisher;

  @Mock ConversationFactory conversationFactory;

  @Mock ProductPricingService productPricingService;

  @Mock Environment springEnvironment;

  MainConversation testConversation;
  UserContext uc;

  @Before
  public void setup() {
    testConversation = new MainConversation(conversationFactory, eventPublisher);

    uc = new UserContext(TOLVANSSON_MEMBER_ID);
  }

  @Test
  public void recieveMessage() throws Exception {
    Message m = testConversation.getMessage(MainConversation.MESSAGE_MAIN_CALLME);
    m.body.text = TOLVANSSON_PHONE_NUMBER;

    uc.getOnBoardingData().setFirstName(TOLVANSSON_FIRSTNAME);
    uc.getOnBoardingData().setFamilyName(TOLVANSSON_LASTNAME);

    testConversation.receiveMessage(uc, m);

    then(eventPublisher)
        .should()
        .publishEvent(
            new RequestPhoneCallEvent(
                TOLVANSSON_MEMBER_ID,
                TOLVANSSON_PHONE_NUMBER,
                TOLVANSSON_FIRSTNAME,
                TOLVANSSON_LASTNAME));
  }

  @Test
  public void GIVEN_MessageQuestion_SHOULD_sendQuestionAskedEvent() {

    Message m = testConversation.getMessage(MainConversation.MESSAGE_MAIN_QUESTION);
    m.body.text = QUESTION;

    testConversation.receiveMessage(uc, m);

    then(eventPublisher)
        .should()
        .publishEvent(new QuestionAskedEvent(TOLVANSSON_MEMBER_ID, QUESTION));
  }
}
