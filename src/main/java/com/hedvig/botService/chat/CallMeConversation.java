package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageBodyText;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.enteties.message.SelectOption;
import com.hedvig.botService.services.LocalizationService;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

public class CallMeConversation extends Conversation {

  public static final String CALLME_CHAT_START = "callme.chat.start";
  public static final String CALLME_CHAT_START_WITHOUT_PHONE = "callme.chat.start.without.phone";
  private static final String CALLME_CHAT_MESSAGE = "callme.chat.message";
  public static final String CALLME_PHONE_OK = "callme.phone.ok";
  public static final String CALLME_PHONE_CHANGE = "callme.phone.change";
  public static final String PHONE_NUMBER = "{PHONE_NUMBER}";

  private final ApplicationEventPublisher eventPublisher;

  public CallMeConversation(ApplicationEventPublisher eventPublisher, LocalizationService localizationService, @Value("${user.language:sv}") String userLanguage) {
    super(eventPublisher, localizationService, userLanguage);
    this.eventPublisher = eventPublisher;
    createMessage(
      CALLME_CHAT_START,
      new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, false),
      new MessageBodySingleSelect(
        "Hej {NAME}, ska jag ringa dig på {PHONE_NUMBER}?",
        Lists.newArrayList(
          new SelectOption("Ja", CALLME_PHONE_OK),
          new SelectOption("Nej", CALLME_PHONE_CHANGE))));

    createMessage(
      CALLME_CHAT_START_WITHOUT_PHONE,
      new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
      new MessageBodyText("Hej {NAME}, vilket telefonnummer kan jag nå dig på?"));

    createMessage(
      CALLME_PHONE_OK,
      new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
      new MessageBodySingleSelect(
        "Ok då ser jag till att någon ringer dig?",
        Lists.newArrayList(SelectLink.toDashboard("Ok", "callme.phone.dashboard"))));

    createMessage(
      CALLME_PHONE_CHANGE,
      new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
      new MessageBodyText("Vilket telefonnummer kan jag nå dig på?"));
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
  public void handleMessage(UserContext userContext, Message m) {
    String nxtMsg = "";

    switch (m.getStrippedBaseMessageId()) {
      case CALLME_PHONE_CHANGE:
      case CALLME_CHAT_START_WITHOUT_PHONE: {
        String trimmedText = m.body.text.trim();
        userContext.putUserData("{PHONE_NUMBER}", trimmedText);
        m.body.text = "Ni kan nå mig på telefonnummer {PHONE_NUMBER}";
        addToChat(m, userContext);

        endConversation(userContext, m);
        nxtMsg = CALLME_PHONE_OK;
        break;
      }
      case CALLME_CHAT_START: {
        val messageBody = (MessageBodySingleSelect) m.body;
        val selectedItem = messageBody.getSelectedItem();
        if (Objects.equals(selectedItem.value, CALLME_PHONE_OK)) {
          endConversation(userContext, m);
        }
      }
    }

    /*
     * In a Single select, there is only one trigger event. Set default here to be a link to a
     * new message
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

  private void endConversation(UserContext userContext, Message m) {
    eventPublisher.publishEvent(
      new RequestPhoneCallEvent(
        userContext.getMemberId(),
        m.body.text,
        userContext.getOnBoardingData().getFirstName(),
        userContext.getOnBoardingData().getFamilyName()));
    userContext.completeConversation(this);
  }

  @Override
  public void init(UserContext userContext) {
    String phoneNumberKey = userContext.getDataEntry(PHONE_NUMBER);

    if (phoneNumberKey == null || phoneNumberKey.trim().isEmpty()) {
      startConversation(userContext, CALLME_CHAT_START_WITHOUT_PHONE);
    } else {
      startConversation(userContext, CALLME_CHAT_START); // Id of first message
    }
  }

  @Override
  public void init(UserContext userContext, String startMessage) {
    startConversation(userContext, startMessage); // Id of first message
  }
}
