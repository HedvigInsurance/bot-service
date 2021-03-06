package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.ClaimAudioReceivedEvent;
import com.hedvig.botService.services.events.ClaimCallMeEvent;
import com.hedvig.libs.translations.Translations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hedvig.botService.chat.MainConversation.MESSAGE_COMPLETE_CLAIM;

@Slf4j
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
  static final String MESSAGE_CLAIMS_RECORD_1 = "message.claims.record.one";
  static final String MESSAGE_CLAIMS_RECORD_2 = "message.claims.record.two";
  static final String MESSAGE_CLAIMS_ASK_EXISTING_PHONE = "message.claims.ask.existing.phone";
  static final String MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW = "message.claims.ask.existing.phone.ask.new";
  static final String PHONE_NUMBER = "{PHONE_NUMBER}"; // The phone that we have from the onboarding;
  /*
   * Need to be stateless. I.e no variables apart from logger
   * */
  private final ApplicationEventPublisher eventPublisher;
  private final ClaimsService claimsService;
  private final ProductPricingService productPricingService;
  private final ConversationFactory conversationFactory;
  private final MemberService memberService;

  ClaimsConversation(
    ApplicationEventPublisher eventPublisher,
    ClaimsService claimsService,
    ProductPricingService productPricingService,
    ConversationFactory conversationFactory,
    MemberService memberService,
    Translations translations,
    UserContext userContext) {
    super(eventPublisher, translations, userContext);
    this.eventPublisher = eventPublisher;
    this.claimsService = claimsService;
    this.productPricingService = productPricingService;
    this.conversationFactory = conversationFactory;
    this.memberService = memberService;

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
            add(new SelectOption("Jag vill chatta", MESSAGE_CLAIMS_START));
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
      MESSAGE_CLAIMS_START,
      new MessageBodyParagraph(
        "Du ska få berätta vad som hänt genom att spela in ett röstmeddelande"),
      2000);


    createMessage(MESSAGE_CLAIMS_OK, new MessageBodyParagraph("Tusen tack!"), 2000);

    createMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE, new MessageBodySingleSelect("Om jag skulle behöva kontakta dig över telefon efteråt, ska jag använda detta nummer: {PHONE_NUMBER}?",
      new ArrayList<>() {
        {
          add(new SelectOption("Ja, gör det", MESSAGE_CLAIMS_RECORD_1));
          add(new SelectOption("Nej, använd ett annat nummer", MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW));
        }
      }));

    createMessage(MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW, new MessageBodyNumber("Okej, vilket nummer ska jag kontakta dig på?"));

    createMessage(MESSAGE_CLAIMS_ASK_PHONE, new MessageBodyNumber("Om jag skulle behöva kontakta dig över telefon efteråt, vad är ditt nummer?"));

    createMessage(
      MESSAGE_CLAIMS_RECORD_1,
      new MessageBodyParagraph("Perfekt! Nu ska du få göra röstinspelningen. Försök besvara de här frågorna så utförligt du kan:"),
      2000);
    addRelay(MESSAGE_CLAIMS_RECORD_1, MESSAGE_CLAIMS_RECORD_2);

    final String MICROPHONE_EMOJI = "\uD83C\uDF99";
    createMessage(
      MESSAGE_CLAIMS_RECORD_2,
      new MessageBodyParagraph(
        MICROPHONE_EMOJI + " Vad har hänt?\n" +
          MICROPHONE_EMOJI + " Var och när hände det?\n" +
          MICROPHONE_EMOJI + " Vad/vem tog skada eller behöver ersättas?"),
      2000);
    addRelay(MESSAGE_CLAIMS_RECORD_2, "message.claims.audio");

    createMessage(
      "message.claims.audio",
      new MessageBodyAudio("Starta inspelning", "/claims/fileupload"),
      2000);

    createMessage(
      "message.claims.record.ok",
      new MessageBodyParagraph("Tack! Din anmälan kommer nu hanteras"),
      2000);

    createMessage("error", new MessageBodyText("Oj nu blev något fel..."));
  }

  @Override
  public void init(String startMessage) {
    log.info("Starting claims conversation for user: " + getUserContext().getMemberId());
    Message m = getMessage(startMessage);
    m.header.fromId = MessageHeader.HEDVIG_USER_ID; // new Long(userContext.getMemberId());
    addToChat(m);
    startConversation(startMessage); // Id of first message
  }

  @Override
  public void init() {
    init(MESSAGE_CLAIMS_START);
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer() {
    return null;
  }

  @Override
  public boolean canAcceptAnswerToQuestion() {
    return false;
  }

  @Override
  public void handleMessage(Message m) {

    String nxtMsg = "";

    if (!validateReturnType(m)) {
      return;
    }

    switch (m.getStrippedBaseMessageId()) {
      case "message.claims.audio":
        nxtMsg = handleAudioReceived(m);
        break;

      case MESSAGE_CLAIM_CALLME:
        assignPhoneNumberToUserContext(m, true);
        nxtMsg = "message.claims.callme.end";
        break;

      case MESSAGE_CLAIMS_ASK_PHONE:
      case MESSAGE_CLAIMS_ASK_EXISTING_PHONE_ASK_NEW:
        assignPhoneNumberToUserContext(m, false);
        addToChat(m);
        nxtMsg = MESSAGE_CLAIMS_RECORD_1;
        break;

      case MESSAGE_CLAIMS_NOT_ACTIVE:
        nxtMsg = handleClaimNotActive(m);
        if (nxtMsg == null) {
          return;
        }
        break;
    }

    val handledNxtMsg = handleSingleSelect(m, nxtMsg, Collections.emptyList());

    completeRequest(handledNxtMsg);
  }

  private void assignPhoneNumberToUserContext(Message m, boolean shouldBeCalledRightAway) {
    getUserContext().getOnBoardingData().setPhoneNumber(m.body.text);
    memberService.updatePhoneNumber(getUserContext().getMemberId(), m.body.text.trim());

    if (shouldBeCalledRightAway) {
      sendCallMeEvent(m);
      getUserContext().completeConversation(this);
    }
  }

  private void sendCallMeEvent(Message m) {
    eventPublisher.publishEvent(
      new ClaimCallMeEvent(
        getUserContext().getMemberId(),
        getUserContext().getOnBoardingData().getFirstName(),
        getUserContext().getOnBoardingData().getFamilyName(),
        m.body.text,
        true));
  }

  private String handleClaimNotActive(Message m) {

    MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
    m.body.text = body.getSelectedItem().text;
    addToChat(m);
    if (body.getSelectedItem().value.equals(MESSAGE_CLAIMS_NOT_ACTIVE_CALL_ME)) {
      return MESSAGE_CLAIM_CALLME;
    }

    getUserContext().completeConversation(this);
    getUserContext().startConversation(conversationFactory.createConversation(MainConversation.class, getUserContext()));
    return null;
  }

  private String handleAudioReceived(Message m) {
    String nxtMsg;
    String audioUrl = ((MessageBodyAudio) m.body).url;
    log.info("Audio recieved with m.body.text: " + m.body.text + " and URL: " + audioUrl);
    m.body.text = "Skicka in anmälan";

    claimsService.createClaimFromAudio(getUserContext().getMemberId(), audioUrl);

    this.eventPublisher.publishEvent(new ClaimAudioReceivedEvent(getUserContext().getMemberId()));

    addToChat(m); // Response parsed to nice format
    nxtMsg = "message.claims.record.ok";
    return nxtMsg;
  }

  @Override
  public void receiveEvent(EventTypes e, String value) {

    switch (e) {
      // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        log.info("Message fetched: " + value);
        if (value.equals("message.claims.record.ok")) {
          completeConversation(getUserContext());
          return;
        }

        String relay = getRelay(value);
        if (relay != null) {
          completeRequest(relay);
          break;
        }

        if (value.equals(ClaimsConversation.MESSAGE_CLAIMS_START)) {
          val phone = getUserContext().getOnBoardingData().getPhoneNumber();
          if (phone == null || phone.isEmpty()) {
            completeRequest(MESSAGE_CLAIMS_ASK_PHONE);
          } else {
            completeRequest(MESSAGE_CLAIMS_ASK_EXISTING_PHONE);
          }
        }
        break;
      default:
        break;
    }
  }

  private void completeConversation(UserContext uc) {
    val conversation = conversationFactory.createConversation(MainConversation.class, uc);
    uc.startConversation(conversation, MESSAGE_COMPLETE_CLAIM);
  }
}
