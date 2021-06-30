package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.services.triggerService.TriggerService;
import com.hedvig.libs.translations.Translations;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hedvig.botService.enteties.UserContext.FORCE_TRUSTLY_CHOICE;

public class TrustlyConversation extends Conversation {

  public static final String START = "trustly.start";
  public static final String TRUSTLY_POLL = "trustly.poll";
  private static final String CANCEL = "trustly.cancel";
  private static final String COMPLETE = "trustly.complete";
  public static final String FORCED_START = "forced.start";
  private final TriggerService triggerService;

  public TrustlyConversation(TriggerService triggerService, ApplicationEventPublisher eventPublisher, Translations translations, UserContext userContext) {
    super(eventPublisher, translations, userContext);
    this.triggerService = triggerService;

    createChatMessage(
      START,
      new MessageBodySingleSelect(
        "Fantastiskt! Nu är allt klart, jag ska bara sätta upp din betalning \fDet ska vara smidigt såklart, så jag använder digitalt autogiro genom Trustly\fInga pengar dras såklart förrän försäkringen börjar gälla!",
        Lists.newArrayList(new SelectItemTrustly("Välj bankkonto", "trustly.noop"))));

    createChatMessage(
      FORCED_START,
      new MessageBodySingleSelect(
        "Då är det dags att sätta upp din betalning \fDet ska vara smidigt såklart, så jag använder digitalt autogiro genom Trustly\fInga pengar dras såklart förrän försäkringen börjar gälla!",
        Lists.newArrayList(new SelectItemTrustly("Välj bankkonto", "trustly.noop"))));

    createChatMessage(
      TRUSTLY_POLL,
      new MessageBodySingleSelect(
        "Om du hellre vill så kan vi vänta med att sätta upp betalningen!\fDå hör jag av mig till dig lite innan din försäkring aktiveras",
        Lists.newArrayList(
          new SelectItemTrustly("Vi gör klart det nu", "trustly.noop"),
          SelectLink.toDashboard("Vi gör det senare, ta mig till appen!", "end"))));

    createMessage(
      CANCEL,
      new MessageBodySingleSelect(
        "Oj, nu verkar det som att något gick lite fel med betalningsregistreringen. Vi testar igen!",
        Lists.newArrayList(new SelectItemTrustly("Välj bankkonto", "trustly.noop"))));

    createMessage(
      COMPLETE,
      new MessageBodySingleSelect(
        "Tack! Dags att börja utforska appen!",
        Lists.newArrayList(
          new SelectLink("Sätt igång", "end", "Dashboard", null, null, false))));
  }

  @Override
  public void handleMessage(final Message m) {

    String nxtMsg = "";

    val handledNxtMsg = handleSingleSelect(m, nxtMsg, Collections.emptyList());

    switch (m.getStrippedBaseMessageId()) {
      case START:
        getUserContext().putUserData(UserContext.TRUSTLY_FORCED_START, "false");
        // endConversation(userContext);
        return;
      case FORCED_START:
        getUserContext().putUserData(UserContext.TRUSTLY_FORCED_START, "true");
        return;
      case TRUSTLY_POLL:
        handleTrustlyPollResponse((MessageBodySingleSelect) m.body);
        return;
      case CANCEL:
        return;
      case COMPLETE:
        // endConversation(userContext);
        return;
    }

    completeRequest(handledNxtMsg);
  }

  private void handleTrustlyPollResponse(MessageBodySingleSelect body) {
    if (body.getSelectedItem().value.equals("end")) {
      getUserContext().putUserData(FORCE_TRUSTLY_CHOICE, "true");
      addToChat(FORCED_START);
    }
  }

  private void endConversation(UserContext userContext) {
    userContext.completeConversation(this);
    userContext.putUserData(FORCE_TRUSTLY_CHOICE, "false");
  }

  @Override
  public void receiveEvent(EventTypes e, String value) {

    switch (e) {
      // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        // log.info("Message fetched:" + value);

        // New way of handeling relay messages
        String relay = getRelay(value);
        if (relay != null) {
          completeRequest(relay);
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void init() {
    addToChat(START);
  }

  @Override
  public void init(String startMessage) {
    addToChat(startMessage);
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer() {
    return Lists.newArrayList();
  }

  @Override
  public boolean canAcceptAnswerToQuestion() {
    return false;
  }


  @Override
  public void addToChat(Message m) {
    if ((m.id.equals(START) || m.id.equals(CANCEL) || m.id.equals(FORCED_START))
      && m.header.fromId == MessageHeader.HEDVIG_USER_ID) {
      final UserData userData = getUserContext().getOnBoardingData();
      UUID triggerUUID =
        triggerService.createTrustlyDirectDebitMandate(
          userData.getSSN(),
          userData.getFirstName(),
          userData.getFamilyName(),
          userData.getEmail(),
          getUserContext().getMemberId());

      getUserContext().putUserData(UserContext.TRUSTLY_TRIGGER_ID, triggerUUID.toString());
    }

    super.addToChat(m);
  }

  public void windowClosed() {
    String nxtMsg;

    final DirectDebitMandateTrigger.TriggerStatus orderState =
      triggerService.getTrustlyOrderInformation(getUserContext().getDataEntry(UserContext.TRUSTLY_TRIGGER_ID));
    if (orderState.equals(DirectDebitMandateTrigger.TriggerStatus.FAILED)) {
      nxtMsg = CANCEL;
    } else if (orderState.equals(DirectDebitMandateTrigger.TriggerStatus.SUCCESS)) {
      nxtMsg = COMPLETE;
      addToChat(getMessage(nxtMsg));
      endConversation(getUserContext());
      return;
    } else {
      nxtMsg = TRUSTLY_POLL;
    }

    addToChat(getMessage(nxtMsg));
  }
}
