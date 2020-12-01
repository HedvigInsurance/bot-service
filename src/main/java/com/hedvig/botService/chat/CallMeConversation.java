package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import com.hedvig.common.localization.LocalizationService;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;

public class CallMeConversation extends Conversation {

  public static final String CALLME_CHAT_START = "callme.chat.start";
  public static final String CALLME_CHAT_START_WITHOUT_PHONE = "callme.chat.start.without.phone";
  private static final String CALLME_CHAT_MESSAGE = "callme.chat.message";
  public static final String CALLME_PHONE_OK = "callme.phone.ok";
  public static final String CALLME_PHONE_CHANGE = "callme.phone.change";
  public static final String PHONE_NUMBER = "{PHONE_NUMBER}";

  private final ApplicationEventPublisher eventPublisher;

  public CallMeConversation(ApplicationEventPublisher eventPublisher, LocalizationService localizationService, UserContext userContext) {
    super(eventPublisher, localizationService, userContext);
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
      case CALLME_PHONE_CHANGE:
      case CALLME_CHAT_START_WITHOUT_PHONE: {
        String trimmedText = m.body.text.trim();
        getUserContext().putUserData("{PHONE_NUMBER}", trimmedText);
        m.body.text = "Ni kan nå mig på telefonnummer {PHONE_NUMBER}";
        addToChat(m);

        endConversation(m);
        nxtMsg = CALLME_PHONE_OK;
        break;
      }
      case CALLME_CHAT_START: {
        val messageBody = (MessageBodySingleSelect) m.body;
        val selectedItem = messageBody.getSelectedItem();
        if (Objects.equals(selectedItem.value, CALLME_PHONE_OK)) {
          endConversation(m);
        }
      }
    }

    val handledNxtMsg = handleSingleSelect(m, nxtMsg);

    completeRequest(handledNxtMsg);
  }

  private void endConversation(Message m) {
    eventPublisher.publishEvent(
      new RequestPhoneCallEvent(
        getUserContext().getMemberId(),
        m.body.text,
        getUserContext().getOnBoardingData().getFirstName(),
        getUserContext().getOnBoardingData().getFamilyName()));
    getUserContext().completeConversation(this);
  }

  @Override
  public void init() {
    String phoneNumberKey = getUserContext().getDataEntry(PHONE_NUMBER);

    if (phoneNumberKey == null || phoneNumberKey.trim().isEmpty()) {
      startConversation(CALLME_CHAT_START_WITHOUT_PHONE);
    } else {
      startConversation(CALLME_CHAT_START); // Id of first message
    }
  }

  @Override
  public void init(String startMessage) {
    startConversation(startMessage); // Id of first message
  }
}
