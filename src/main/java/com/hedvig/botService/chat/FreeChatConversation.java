package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageBodyText;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import java.time.Clock;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;

public class FreeChatConversation extends Conversation {

  public static final String FREE_CHAT_START = "free.chat.start";
  private static final String FREE_CHAT_MESSAGE = "free.chat.message";
  public static final String FREE_CHAT_FROM_BO = "free.chat.from.bo";
  public static final String FREE_CHAT_ONBOARDING_START = "free.chat.onboarding.start";
  public static final String FILE_QUESTION_MESSAGE = "file with mime type: %s is uploaded";

  private final StatusBuilder statusBuilder;
  private final ApplicationEventPublisher eventPublisher;
  private final ProductPricingService productPricingService;

  public FreeChatConversation(
      StatusBuilder statusBuilder,
      ApplicationEventPublisher eventPublisher,
      ProductPricingService productPricingService) {
    this.statusBuilder = statusBuilder;
    this.eventPublisher = eventPublisher;
    this.productPricingService = productPricingService;

    createMessage(
        FREE_CHAT_START,
        new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Hej {NAME}! Hur kan jag hjälpa dig idag?"));

    createMessage(
        FREE_CHAT_ONBOARDING_START,
        new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Hade du någon fundering?"));

    createMessage(
        FREE_CHAT_MESSAGE,
        new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText(""));
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer(UserContext uc) {
    return Lists.newArrayList();
  }

  @Override
  public boolean canAcceptAnswerToQuestion(UserContext uc) {
    return true;
  }

  @Override
  public void receiveMessage(UserContext userContext, Message m) {
    String nxtMsg = "";

    switch (m.id) {
      case FREE_CHAT_START:
      case FREE_CHAT_ONBOARDING_START:
      case FREE_CHAT_FROM_BO:
      case FREE_CHAT_MESSAGE:
        {
          m.header.statusMessage = statusBuilder.getStatusMessage(Clock.systemUTC());

          boolean isFile = m.body instanceof MessageBodyFileUpload;
          if (productPricingService.getInsuranceStatus(userContext.getMemberId()) != null) {
            if(isFile){
              eventPublisher.publishEvent(new QuestionAskedEvent(userContext.getMemberId(), String.format(FILE_QUESTION_MESSAGE, ((MessageBodyFileUpload) m.body).mimeType)));
            }
            else{
              eventPublisher.publishEvent(new QuestionAskedEvent(userContext.getMemberId(), m.body.text));
            }
          } else {
            if (isFile){
              eventPublisher.publishEvent(new OnboardingQuestionAskedEvent(userContext.getMemberId(), String.format(FILE_QUESTION_MESSAGE, ((MessageBodyFileUpload)m.body).mimeType)));
            }else{
              eventPublisher.publishEvent(new OnboardingQuestionAskedEvent(userContext.getMemberId(), m.body.text));
            }
          }

          addToChat(m, userContext);
          nxtMsg = FREE_CHAT_MESSAGE;
          break;
        }
    }

    /*
     * In a Single select, there is only one trigger event. Set default here to be a link to a new message
     */
    if (nxtMsg.equals("") && m.body.getClass().equals(MessageBodySingleSelect.class)) {

      MessageBodySingleSelect body1 = (MessageBodySingleSelect) m.body;
      for (SelectItem o : body1.choices) {
        if (o.selected) {
          m.body.text = o.text;
          addToChat(m, userContext);
          nxtMsg = o.value;
        }
      }
    }

    completeRequest(nxtMsg, userContext);
  }

  @Override
  protected Message createBackOfficeMessage(UserContext uc, String message, String id) {
    Message msg = new Message();
    msg.body = new MessageBodyText(message);
    msg.header.fromId = HEDVIG_USER_ID;
    msg.globalId = null;
    msg.header.messageId = null;
    msg.body.id = null;
    msg.id = FREE_CHAT_FROM_BO;

    return msg;
  }

  @Override
  public void init(UserContext userContext) {
    startConversation(userContext, FREE_CHAT_START); // Id of first message
  }

  @Override
  public void init(UserContext userContext, String startMessage) {
    startConversation(userContext, startMessage); // Id of first message
  }
}
