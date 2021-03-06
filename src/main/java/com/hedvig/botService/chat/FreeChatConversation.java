package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.FileUploadedEvent;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import com.hedvig.botService.utils.DateUtil;
import com.hedvig.libs.translations.Translations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FreeChatConversation extends Conversation {

  public static final String FREE_CHAT_START = "free.chat.start";
  public static final String FREE_CHAT_MESSAGE = "free.chat.message";
  public static final String FREE_CHAT_FROM_BO = "free.chat.from.bo";
  public static final String FREE_CHAT_FROM_CLAIM = "free.chat.from.claim";
  public static final String FREE_CHAT_ONBOARDING_START = "free.chat.onboarding.start";
  public static final String FILE_QUESTION_MESSAGE = "file with mime type: %s is uploaded";

  private final StatusBuilder statusBuilder;
  private final ApplicationEventPublisher eventPublisher;
  private final ProductPricingService productPricingService;

  public FreeChatConversation(
    StatusBuilder statusBuilder,
    ApplicationEventPublisher eventPublisher,
    ProductPricingService productPricingService,
    Translations translations,
    UserContext userContext) {
    super(eventPublisher, translations, userContext);
    this.statusBuilder = statusBuilder;
    this.eventPublisher = eventPublisher;
    this.productPricingService = productPricingService;

    createMessage(
      FREE_CHAT_START,
      MessageHeader.createRichTextHeader(),
      new MessageBodyText("Hej {NAME}! Hur kan jag hjälpa dig idag?"));

    createMessage(
      FREE_CHAT_ONBOARDING_START,
      MessageHeader.createRichTextHeader(),
      new MessageBodyText("Hade du någon fundering?"));

    createMessage(
      FREE_CHAT_MESSAGE,
      MessageHeader.createRichTextHeader(),
      new MessageBodyText(""));

    createMessage(
      FREE_CHAT_FROM_CLAIM,
      MessageHeader.createRichTextHeader(),
      new MessageBodyText("Självklart, vad kan jag hjälpa dig med?"));
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer() {
    return Lists.newArrayList();
  }

  @Override
  public boolean canAcceptAnswerToQuestion() {
    return true;
  }

  @Override
  public void handleMessage(Message m) {
    String nxtMsg = "";
    switch (m.getStrippedBaseMessageId()) {
      case FREE_CHAT_START:
      case FREE_CHAT_ONBOARDING_START:
      case FREE_CHAT_FROM_BO:
      case FREE_CHAT_FROM_CLAIM:
      case FREE_CHAT_MESSAGE: {
        m.header.statusMessage = statusBuilder.getStatusReplyMessage(DateUtil.INSTANCE.nowInStockholm(), getUserContext().getLocale());

        boolean isFile = m.body instanceof MessageBodyFileUpload;

        if (isFile) {
          val body = (MessageBodyFileUpload) m.body;
          eventPublisher.publishEvent(new FileUploadedEvent(getUserContext().getMemberId(), body.key, body.mimeType));
        } else {
          eventPublisher.publishEvent(new QuestionAskedEvent(getUserContext().getMemberId(), m.body.text));
        }

        addToChat(m);
        nxtMsg = FREE_CHAT_MESSAGE;
        break;
      }
    }

    val handledNxtMsg = handleSingleSelect(m, nxtMsg, Collections.emptyList());

    completeRequest(handledNxtMsg);
  }

  @NotNull
  @Override
  public Message createBackOfficeMessage(String message, String id) {
    Message msg = new Message();
    msg.body = new MessageBodyText(message);
    msg.header = MessageHeader.createRichTextHeader();
    msg.header.messageId = null;
    msg.globalId = null;
    msg.body.id = null;
    msg.id = FREE_CHAT_FROM_BO;
    addMessage(msg);
    return msg;
  }

  @Override
  public void init() {
    startConversation(FREE_CHAT_START); // Id of first message
  }

  @Override
  public void init(String startMessage) {
    startConversation(startMessage); // Id of first message
  }
}
