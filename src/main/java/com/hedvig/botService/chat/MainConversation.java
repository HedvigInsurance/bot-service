package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyNumber;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageBodyText;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.enteties.message.SelectOption;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import lombok.val;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_FROM_CLAIM;

@Component
public class MainConversation extends Conversation {

  public static final String MESSAGE_HEDVIG_COM = "hedvig.com";
  public static final String MESSAGE_HEDVIG_COM_POST_LOGIN = "hedvig.com.post.login";
  public static final String MESSAGE_QUESTION_RECIEVED = "message.question.recieved";
  public static final String MESSAGE_MAIN_END = "message.main.end";
  public static final String MESSAGE_MAIN_CALLME = "message.main.callme";
  public static final String MESSAGE_MAIN_QUESTION = "main.question";
  public static final String MESSAGE_MAIN_START_TRUSTLY = "message.main.start_trustly";
  public static final String MESSAGE_ERROR = "error";
  public static final String MESSAGE_MAIN_REPORT = "message.main.report";
  public static final String MESSAGE_MAIN_ONBOARDING_DONE = "onboarding.done";
  public static final String CONVERSATION_DONE = "conversation.done";
  public static final String MESSAGE_COMPLETE_CLAIM = "hedvig.complete.claim";
  public static final String MESSAGE_CLAIM_DONE = "claim.done";
  public static final String MESSAGE_MAIN_START_CHAT = "message.main.start.chat";
  public static final String MESSAGE_MAIN_START_FREE_TEXT_CHAT = "message.main.start.free.text.chat";

  private static Logger log = LoggerFactory.getLogger(MainConversation.class);
  private final ConversationFactory conversationFactory;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public MainConversation(
      ConversationFactory conversationFactory, ApplicationEventPublisher eventPublisher) {
    super(eventPublisher);
    this.conversationFactory = conversationFactory;
    this.eventPublisher = eventPublisher;

    createMessage(
        MESSAGE_HEDVIG_COM,
        new MessageBodySingleSelect(
            "Hej {NAME}, vad vill du göra idag?",
            Lists.newArrayList(
                new SelectOption("Rapportera en skada", MESSAGE_MAIN_REPORT),
                new SelectOption("Ring mig!", MESSAGE_MAIN_CALLME),
                new SelectOption("Jag har en fråga", MESSAGE_MAIN_QUESTION))));

    createMessage(
      MESSAGE_HEDVIG_COM_POST_LOGIN,
      new MessageBodySingleSelect(
        "Välkommen tillbaka {NAME}!",
        Lists.newArrayList(
          SelectLink.toDashboard("Ta mig till till hemskärmen", "postlogindash"))));

    final String HANDS_EMOJI = "🙌";
    createMessage(
      MESSAGE_COMPLETE_CLAIM,
        new MessageBodySingleSelect(
            "Jag återkommer här i chatten om jag behöver något mer eller för att berätta hur det går " + HANDS_EMOJI,
            Lists.newArrayList(
                new SelectOption("Okej!", MESSAGE_CLAIM_DONE),
                new SelectOption("Jag har en fråga", MESSAGE_MAIN_START_FREE_TEXT_CHAT))));

    createMessage(
        MESSAGE_QUESTION_RECIEVED,
        new MessageBodySingleSelect(
            "Tack {NAME}, jag återkommer så snart jag kan med svar på din fråga",
            Lists.newArrayList(
                new SelectOption("Jag har en till fråga", MESSAGE_MAIN_QUESTION),
                SelectLink.toDashboard("Hem", MESSAGE_HEDVIG_COM))));

    createMessage(
        MESSAGE_MAIN_END,
        new MessageBodySingleSelect(
            "Tack. Jag ringer upp dig så snart jag kan",
            Lists.newArrayList(SelectLink.toDashboard("Hem", MESSAGE_MAIN_ONBOARDING_DONE))));

    createMessage(MESSAGE_MAIN_CALLME, new MessageBodyNumber("Vad når jag dig på för nummer?"));

    createMessage(
        MESSAGE_MAIN_QUESTION,
        new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Självklart, vad kan jag hjälpa dig med?"));

    createMessage(MESSAGE_ERROR, new MessageBodyText("Oj nu blev något fel..."));
  }

  @Override
  public void handleMessage(UserContext userContext, Message m) {

    String nxtMsg = "";

    if (!validateReturnType(m, userContext)) {
      return;
    }

    switch (m.id) {
      case MESSAGE_COMPLETE_CLAIM:
      case MESSAGE_HEDVIG_COM:
        {
          SelectItem item = ((MessageBodySingleSelect) m.body).getSelectedItem();
          m.body.text = item.text;
          if (Objects.equals(item.value, MESSAGE_MAIN_REPORT)) {
            nxtMsg = CONVERSATION_DONE;
          } else if (Objects.equals(item.value, MESSAGE_MAIN_START_TRUSTLY)) {
            addToChat(m, userContext);
            userContext.completeConversation(this); // TODO: End conversation in better way
            userContext.startConversation(
                conversationFactory.createConversation(TrustlyConversation.class));
            userContext.putUserData(UserContext.FORCE_TRUSTLY_CHOICE, "false");
            return;
          }

          addToChat(m, userContext); // Response parsed to nice format
          break;
        }
      case MESSAGE_MAIN_CALLME:
        userContext.putUserData("{PHONE_" + new LocalDate().toString() + "}", m.body.text);
        eventPublisher.publishEvent(
            new RequestPhoneCallEvent(
                userContext.getMemberId(),
                m.body.text,
                userContext.getOnBoardingData().getFirstName(),
                userContext.getOnBoardingData().getFamilyName()));
        nxtMsg = MESSAGE_MAIN_END;
        addToChat(m, userContext); // Response parsed to nice format
        userContext.completeConversation(this); // TODO: End conversation in better way
        break;
      case MESSAGE_MAIN_QUESTION:
        nxtMsg = handleQuestion(userContext, m);
        break;
      case MESSAGE_MAIN_START_FREE_TEXT_CHAT:
        startFreeTextChatConversation(userContext);
        break;
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

  private void startFreeTextChatConversation(UserContext uc) {
    val conversation = conversationFactory.createConversation(FreeChatConversation.class);
    uc.startConversation(conversation, FREE_CHAT_FROM_CLAIM);
  }

  public String handleQuestion(UserContext userContext, Message m) {
    String nxtMsg;
    final String question = m.body.text;
    userContext.putUserData("{QUESTION_" + new LocalDate().toString() + "}", question);
    addToChat(m, userContext); // Response parsed to nice format
    eventPublisher.publishEvent(new QuestionAskedEvent(userContext.getMemberId(), question));
    nxtMsg = MESSAGE_QUESTION_RECIEVED;
    return nxtMsg;
  }

  /*
   * Generate next chat message or ends conversation
   * */
  @Override
  public void completeRequest(String nxtMsg, UserContext userContext) {

    switch (nxtMsg) {
      case CONVERSATION_DONE:
        log.info("conversation complete");
        userContext.completeConversation(this);
        userContext.startConversation(
            conversationFactory.createConversation(ClaimsConversation.class));

        return;
      case MESSAGE_HEDVIG_COM + ".4":
        addTrustlyButton(userContext);
      case "":
        log.error("I dont know where to go next...");
        nxtMsg = "error";
        break;
    }

    super.completeRequest(nxtMsg, userContext);
  }

  public void addTrustlyButton(UserContext userContext) {
    val message = getMessage(MESSAGE_HEDVIG_COM);
    val body = (MessageBodySingleSelect) message.body;
    String forceTrustly = userContext.getDataEntry(UserContext.FORCE_TRUSTLY_CHOICE);
    if ("true".equalsIgnoreCase(forceTrustly)) {
      body.choices.add(new SelectOption("Koppla autogiro", MESSAGE_MAIN_START_TRUSTLY));
    }
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer(UserContext uc) {
    return Lists.newArrayList(
        new SelectOption("Svara Hedvig", MESSAGE_MAIN_QUESTION),
        new SelectOption("Tack, det var vad jag behövde veta", MESSAGE_HEDVIG_COM));
  }

  @Override
  public boolean canAcceptAnswerToQuestion(UserContext uc) {
    return true;
  }

  @Override
  public void init(UserContext userContext) {
    log.info("Starting main conversation");
    addTrustlyButton(userContext);
    startConversation(userContext, MESSAGE_HEDVIG_COM); // Id of first message
  }

  @Override
  public void init(UserContext userContext, String startMessage) {
    log.info("Starting main conversation with message: " + startMessage);
    addTrustlyButton(userContext);
    startConversation(userContext, startMessage); // Id of first message
  }
}
