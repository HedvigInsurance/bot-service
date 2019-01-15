package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyAudio;
import com.hedvig.botService.enteties.message.MessageBodyNumber;
import com.hedvig.botService.enteties.message.MessageBodyParagraph;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageBodyText;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.enteties.message.SelectOption;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.ClaimAudioReceivedEvent;
import com.hedvig.botService.services.events.ClaimCallMeEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ClaimsConversation extends Conversation {

  static final String MESSAGE_CLAIMS_START = "message.claims.start";
  static final String MESSAGE_CLAIMS_NOT_ACTIVE = "message.claims.not_active";
  private static final String MESSAGE_CLAIMS_NOT_ACTIVE_CALL_ME =
    "message.claims.not_active.call_me";
  private static final String MESSAGE_CLAIMS_NOT_ACTIVE_OK = "message.claims.not_active.ok";
  static final String MESSAGE_CLAIM_CALLME = "message.claim.callme";
  static final String MESSAGE_CLAIMS_OK = "message.claims.ok";
  static final String MESSAGE_CLAIMS_ASK_PHONE = "message.claims.ask.phone";
  static final String MESSAGE_CLAIMS_ASK_PHONE_END = "message.claims.ask.phone.end";
  static final String MESSAGE_CLAIMS_RECORD = "message.claims.record";
  static final String MESSAGE_CLAIMS_ASK_EXISTING_PHONE = "message.claims.ask.existing.phone";
  static final String MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW = "message.claims.ask.existing.phone.ask.new";
  static final String PHONE_NUMBER = "{PHONE_NUMBER}"; // The phone that we have from the onboarding;
  static final String PHONE_CLAIM = "{PHONE_CLAIM}"; // emergency phone for the claim, will be set only if you don't have PHONE_NUMBER or the user wants otherwise
  /*
   * Need to be stateless. I.e no variables apart from logger
   * */
  private final ApplicationEventPublisher eventPublisher;
  private final ClaimsService claimsService;
  private final ProductPricingService productPricingService;
  private final ConversationFactory conversationFactory;

  @Autowired
  ClaimsConversation(
    ApplicationEventPublisher eventPublisher,
    ClaimsService claimsService,
    ProductPricingService productPricingService,
    ConversationFactory conversationFactory) {
    super();
    this.eventPublisher = eventPublisher;
    this.claimsService = claimsService;
    this.productPricingService = productPricingService;
    this.conversationFactory = conversationFactory;

    createMessage(
      MESSAGE_CLAIMS_START, new MessageBodyParagraph("Okej, det här löser vi på nolltid!"), 2000);
    addRelay(MESSAGE_CLAIMS_START, "message.claims.chat");

    createChatMessage(
      MESSAGE_CLAIMS_NOT_ACTIVE,
      new MessageBodySingleSelect(
        "Din försäkring har inte ännu flyttats till Hedvig, du har fortfarande bindningstid kvar hos ditt gamla försäkringsbolag. Så tills vidare skulle jag rekommendera dig att prata med dem.\fBehöver du stöd eller hjälp kan jag så klart be en av mina kollegor att ringa dig?",
        Lists.newArrayList(
          new SelectOption("Jag förstår", MESSAGE_CLAIMS_NOT_ACTIVE_OK),
          new SelectOption("Ring mig", MESSAGE_CLAIMS_NOT_ACTIVE_CALL_ME))));

    createMessage(
      "message.claim.menu",
      new MessageBodySingleSelect(
        "Är du i en krissituation just nu? Om det är akut så ser jag till att en kollega ringer upp dig",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Ring mig!", MESSAGE_CLAIM_CALLME));
            add(new SelectOption("Jag vill chatta", "message.claims.chat"));
          }
        }));

    createMessage(MESSAGE_CLAIM_CALLME, new MessageBodyNumber("Vilket telefonnummer nås du på?"));
    createMessage(
      "message.claims.callme.end",
      new MessageBodySingleSelect(
        "Tack! En kollega ringer dig så snart som möjligt",
        new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Hem", "onboarding.done", "Dashboard", null, null, false));
          }
        }));

    createMessage(
      "message.claims.chat",
      new MessageBodyParagraph(
        "Du ska strax få berätta vad som hänt genom att spela in ett röstmeddelande"),
      2000);
    addRelay("message.claims.chat", "message.claims.chat2");

    createMessage(
      "message.claims.chat2",
      new MessageBodyParagraph("Först behöver du bara bekräfta detta"),
      2000);
    addRelay("message.claims.chat2", "message.claim.promise");

    createMessage(
      "message.claim.promise",
      new MessageBodySingleSelect(
        "Hedvigs hederslöfte\n\n"
          + "Jag förstår att Hedvig bygger på tillit. Jag lovar att berätta om händelsen precis som den var, och bara ta ut den ersättning jag har rätt till.\n\n"
          + "Tar jag ut mer än så inser jag att det drabbar min valda välgörenhetsorganisation",
        // new MessageBodySingleSelect("HEDVIGS HEDERSLÖFTE\nJag vet att Hedvig bygger på tillit
        // medlemmar emellan.\nJag lovar att berätta om händelsen precis som den var, och bara
        // ta ut den ersättning jag har rätt till ur vår gemensamma medlemspott.",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag lovar!", MESSAGE_CLAIMS_OK));
          }
        }));

    createMessage(MESSAGE_CLAIMS_OK, new MessageBodyParagraph("Tusen tack!"), 2000);

    createMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE, new MessageBodySingleSelect("Om jag skulle behöva kontakta dig. Ska vi använda detta nummer {PHONE_NUMBER} ?",
      new ArrayList<SelectItem>() {
        {
          add(new SelectOption("Ja", MESSAGE_CLAIMS_ASK_PHONE_END));
          add(new SelectOption("Nej", MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW));
        }
      }));

    createMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW,new MessageBodyNumber("Okej, vilket nummer ska jag kontakta dig på?"));

    createMessage(MESSAGE_CLAIMS_ASK_PHONE, new MessageBodyNumber("Om jag skulle behöva nå dig på telefon, vad är ditt nummer?"));

    createMessage(MESSAGE_CLAIMS_ASK_PHONE_END, new MessageBodyParagraph("Toppen!"), 2000);

    addRelay(MESSAGE_CLAIMS_ASK_PHONE_END, MESSAGE_CLAIMS_RECORD);

    createMessage(
      MESSAGE_CLAIMS_RECORD,
      new MessageBodyParagraph("Berätta vad som har hänt genom att spela in ett röstmeddelande"),
      2000);
    addRelay(MESSAGE_CLAIMS_RECORD, "message.claims.record2");

    createMessage(
      "message.claims.record2",
      new MessageBodyParagraph(
        "Ju mer detaljer du ger, desto snabbare hjälp kan jag ge. Så om du svarar på dessa frågor är vi en god bit på väg: "),
      2000);
    addRelay("message.claims.record2", "message.claims.record3");

    createMessage("message.claims.record3", new MessageBodyParagraph("Vad har hänt?"), 2000);
    addRelay("message.claims.record3", "message.claims.record4");

    createMessage(
      "message.claims.record4", new MessageBodyParagraph("Var och när hände det?"), 2000);
    addRelay("message.claims.record4", "message.claims.record5");

    createMessage(
      "message.claims.record5", new MessageBodyParagraph("Vad eller vem drabbades?"), 2000);
    addRelay("message.claims.record5", "message.claims.audio");

    createMessage(
      "message.claims.audio",
      new MessageBodyAudio("Starta inspelning", "/claims/fileupload"),
      2000);

    createMessage(
      "message.claims.record.ok",
      new MessageBodyParagraph("Tack! Det är allt jag behöver just nu"),
      2000);
    addRelay("message.claims.record.ok", "message.claims.record.ok2");

    createMessage(
      "message.claims.record.ok2",
      new MessageBodyParagraph(
        "Jag återkommer till dig om jag behöver något mer, eller för att meddela att jag kan betala ut ersättning direkt"),
      2000);
    addRelay("message.claims.record.ok2", "message.claims.record.ok3");

    createMessage(
      "message.claims.record.ok3",
      new MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
      new MessageBodyText(
        "Tack för att du delat med dig om det som hänt. Ta hand om dig så länge, så hörs vi snart!"));

    createMessage("error", new MessageBodyText("Oj nu blev något fel..."));
  }

  public void init(UserContext userContext, String startMessage) {
    log.info("Starting claims conversation for user: " + userContext.getMemberId());
    Message m = getMessage(startMessage);
    m.header.fromId = MessageHeader.HEDVIG_USER_ID; // new Long(userContext.getMemberId());
    addToChat(m, userContext);
    startConversation(userContext, startMessage); // Id of first message
  }

  @Override
  public void init(UserContext userContext) {
    if (productPricingService.isMemberInsuranceActive(userContext.getMemberId()) == false) {
      init(userContext, MESSAGE_CLAIMS_NOT_ACTIVE);
      return;
    }

    init(userContext, MESSAGE_CLAIMS_START);
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer(UserContext uc) {
    return null;
  }

  @Override
  public boolean canAcceptAnswerToQuestion(UserContext uc) {
    return false;
  }

  @Override
  public void handleMessage(UserContext userContext, Message m) {

    String nxtMsg = "";

    if (!validateReturnType(m, userContext)) {
      return;
    }

    switch (m.getBaseMessageId()) {
      case "message.claims.audio":
        nxtMsg = handleAudioReceived(userContext, m);
        break;

      case MESSAGE_CLAIM_CALLME:
        assignPhoneNumberToUserContext(userContext, m, true);
        nxtMsg = "message.claims.callme.end";
        break;

      case MESSAGE_CLAIMS_ASK_PHONE:
      case MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW:
        assignPhoneNumberToUserContext(userContext, m, false);
        nxtMsg = MESSAGE_CLAIMS_ASK_PHONE_END;
        break;
      case MESSAGE_CLAIMS_OK:
        val phone = userContext.getOnBoardingData().getPhoneNumber();
        if (phone == null || phone.isEmpty()) {
          nxtMsg = MESSAGE_CLAIMS_ASK_PHONE;
        } else {
          nxtMsg = MESSAGE_CLAIMS_ASK_EXISTING_PHONE;
        }
        break;

      case MESSAGE_CLAIMS_NOT_ACTIVE:
        nxtMsg = handleClaimNotActive(userContext, m);
        if (nxtMsg == null) {
          return;
        }
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

  private void assignPhoneNumberToUserContext(UserContext userContext, Message m, boolean shouldBeCalledRightAway) {
    val onBoardingPhone = userContext.getOnBoardingData().getPhoneNumber();
    if (onBoardingPhone == null || onBoardingPhone.isEmpty()) {
      userContext.getOnBoardingData().setPhoneNumber(m.body.text);
    }

    userContext.putUserData("{PHONE_CLAIM}", m.body.text);
    addToChat(m, userContext); // Response parsed to nice format
    userContext.completeConversation(this);

    if (shouldBeCalledRightAway) sendCallMeEvent(userContext, m);
  }

  private void sendCallMeEvent(UserContext userContext, Message m) {
    val isInsuranceActive =
      productPricingService.isMemberInsuranceActive(userContext.getMemberId());
    eventPublisher.publishEvent(
      new ClaimCallMeEvent(
        userContext.getMemberId(),
        userContext.getOnBoardingData().getFirstName(),
        userContext.getOnBoardingData().getFamilyName(),
        m.body.text,
        isInsuranceActive));
  }

  private String handleClaimNotActive(UserContext userContext, Message m) {

    MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
    m.body.text = body.getSelectedItem().text;
    addToChat(m, userContext);
    if (body.getSelectedItem().value.equals(MESSAGE_CLAIMS_NOT_ACTIVE_CALL_ME)) {
      return MESSAGE_CLAIM_CALLME;
    }

    userContext.completeConversation(this);
    userContext.startConversation(conversationFactory.createConversation(MainConversation.class));
    return null;
  }

  private String handleAudioReceived(UserContext userContext, Message m) {
    String nxtMsg;
    String audioUrl = ((MessageBodyAudio) m.body).url;
    log.info("Audio recieved with m.body.text: " + m.body.text + " and URL: " + audioUrl);
    m.body.text = "Inspelning klar";

    claimsService.createClaimFromAudio(userContext.getMemberId(), audioUrl);

    this.eventPublisher.publishEvent(new ClaimAudioReceivedEvent(userContext.getMemberId()));

    addToChat(m, userContext); // Response parsed to nice format
    nxtMsg = "message.claims.record.ok";
    return nxtMsg;
  }

  @Override
  public void receiveEvent(EventTypes e, String value, UserContext userContext) {

    switch (e) {
      // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        log.info("Message fetched: " + value);
        if (value.equals("message.claims.record.ok2")) {
          completeConversation(userContext);
          return;
        }

        String relay = getRelay(value);
        if (relay != null) {
          completeRequest(relay, userContext);
        }
        break;
      default:
        break;
    }
  }

  private void completeConversation(UserContext uc) {
    addToChat("message.claims.record.ok3", uc);
    val conversation = conversationFactory.createConversation(MainConversation.class);
    uc.startConversation(conversation, MainConversation.MESSAGE_HEDVIG_COM_CLAIMS);
  }
}
