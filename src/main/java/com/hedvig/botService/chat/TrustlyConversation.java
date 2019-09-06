package com.hedvig.botService.chat;

import static com.hedvig.botService.enteties.UserContext.FORCE_TRUSTLY_CHOICE;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.enteties.message.SelectItemTrustly;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.services.triggerService.TriggerService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

public class TrustlyConversation extends Conversation {

  public static final String START = "trustly.start";
  public static final String TRUSTLY_POLL = "trustly.poll";
  private static final String CANCEL = "trustly.cancel";
  private static final String COMPLETE = "trustly.complete";
  public static final String FORCED_START = "forced.start";
  private final TriggerService triggerService;
  private final MemberService memberService;

  public TrustlyConversation(TriggerService triggerService, MemberService memberService, ApplicationEventPublisher eventPublisher) {
    super(eventPublisher);
    this.triggerService = triggerService;
    this.memberService = memberService;

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
  public void handleMessage(final UserContext userContext, final Message m) {

    String nxtMsg = "";
    /*
     * In a Single select, there is only one trigger event. Set default here to be a link to a new message
     */
    if (m.body.getClass().equals(MessageBodySingleSelect.class)) {

      MessageBodySingleSelect body1 = (MessageBodySingleSelect) m.body;
      for (SelectItem o : body1.choices) {
        if (o.selected) {
          m.body.text = o.text;
          addToChat(m, userContext);
          nxtMsg = o.value;
        }
      }
    }

    switch (m.getBaseMessageId()) {
      case START:
        userContext.putUserData(UserContext.TRUSTLY_FORCED_START, "false");
        // endConversation(userContext);
        return;
      case FORCED_START:
        userContext.putUserData(UserContext.TRUSTLY_FORCED_START, "true");
        return;
      case TRUSTLY_POLL:
        handleTrustlyPollResponse((MessageBodySingleSelect) m.body, userContext);
        return;
      case CANCEL:
        return;
      case COMPLETE:
        // endConversation(userContext);
        return;
    }

    completeRequest(nxtMsg, userContext);
  }

  private void handleTrustlyPollResponse(MessageBodySingleSelect body, UserContext userContext) {
    if (body.getSelectedItem().value.equals("end")) {
      userContext.putUserData(FORCE_TRUSTLY_CHOICE, "true");
      addToChat(FORCED_START, userContext);
    }
  }

  private void endConversation(UserContext userContext) {
    userContext.completeConversation(this);
    userContext.putUserData(FORCE_TRUSTLY_CHOICE, "false");
  }

  @Override
  public void receiveEvent(EventTypes e, String value, UserContext userContext) {

    switch (e) {
        // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        // log.info("Message fetched:" + value);

        // New way of handeling relay messages
        String relay = getRelay(value);
        if (relay != null) {
          completeRequest(relay, userContext);
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void init(UserContext userContext) {
    addToChat(START, userContext);
  }

  @Override
  public void init(UserContext userContext, String startMessage) {
    addToChat(startMessage, userContext);
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer(UserContext uc) {
    return Lists.newArrayList();
  }

  @Override
  public boolean canAcceptAnswerToQuestion(UserContext uc) {
    return false;
  }


  @Override
  public void addToChat(Message m, UserContext userContext) {
    if ((m.id.equals(START) || m.id.equals(CANCEL) || m.id.equals(FORCED_START))
        && m.header.fromId == MessageHeader.HEDVIG_USER_ID) {
      final UserData userData = userContext.getOnBoardingData();
      UUID triggerUUID =
          triggerService.createTrustlyDirectDebitMandate(
              userData.getSSN(),
              userData.getFirstName(),
              userData.getFamilyName(),
              userData.getEmail(),
              userContext.getMemberId());

      userContext.putUserData(UserContext.TRUSTLY_TRIGGER_ID, triggerUUID.toString());
    }

    super.addToChat(m, userContext);
  }

  public void windowClosed(UserContext uc) {
    String nxtMsg;

    final DirectDebitMandateTrigger.TriggerStatus orderState =
        triggerService.getTrustlyOrderInformation(uc.getDataEntry(UserContext.TRUSTLY_TRIGGER_ID));
    if (orderState.equals(DirectDebitMandateTrigger.TriggerStatus.FAILED)) {
      nxtMsg = CANCEL;
    } else if (orderState.equals(DirectDebitMandateTrigger.TriggerStatus.SUCCESS)) {
      nxtMsg = COMPLETE;
      addToChat(getMessage(nxtMsg), uc);
      endConversation(uc);
      return;
    } else {
      nxtMsg = TRUSTLY_POLL;
    }

    addToChat(getMessage(nxtMsg), uc);
  }
}
