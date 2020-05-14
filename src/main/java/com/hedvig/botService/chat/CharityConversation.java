package com.hedvig.botService.chat;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.common.localization.LocalizationService;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CharityConversation extends Conversation {

  private static final String SOSBARNBYAR_VALUE = "charity.sosbarnbyar";
  private static final String SOS_BARNBYAR_NAME = "SOS Barnbyar";
  private static final UUID SOS_BARNBYAR_ID =
    UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2");

  private static final String BARNCANCERFONDEN_VALUE = "charity.barncancerfonden";
  private static final String BARNCANCERFONDEN_NAME = "Barncancerfonden";
  private static final UUID BARNCANCERFONDEN_ID =
    UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f");

  private static final String MESSAGE_CHARITY_UNKOWN_CHOICE = "message.charity.unkownchoice";
  private static final String MESSAGE_KONTRAKT_CHARITY = "message.kontrakt.charity";
  private static final String MESSAGE_KONTRAKT_CHARITY_TELLMEMORE =
    "message.kontrakt.charity.tellmemore";
  private static final String MESSAGE_KONTRAKT_CHARITY_TACK = "message.kontrakt.charity.tack";
  private static final String MESSAGE_KONTRAKT_CHARITY_TACK_END =
    "message.kontrakt.charity.tack.end";

  private final Logger log = LoggerFactory.getLogger(CharityConversation.class);
  private final ConversationFactory conversationFactory;
  private final MemberService memberService;
  private final ProductPricingService productPricingService;

  CharityConversation(
    ConversationFactory factory,
    MemberService memberService,
    ProductPricingService productPricingService,
    ApplicationEventPublisher eventPublisher,
    LocalizationService localizationService,
    UserContext userContext) {
    super(eventPublisher, localizationService, userContext);
    this.conversationFactory = factory;
    this.memberService = memberService;
    this.productPricingService = productPricingService;

    createChatMessage(
      MESSAGE_KONTRAKT_CHARITY,
      new MessageBodySingleSelect(
        "Tack! En grej till! \f"
          + "Som Hedvig-medlem får du välja en välgörenhetsorganisation att stödja om det blir pengar över när alla skador har betalats",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption(SOS_BARNBYAR_NAME, SOSBARNBYAR_VALUE));
            add(new SelectOption(BARNCANCERFONDEN_NAME, BARNCANCERFONDEN_VALUE));
            add(new SelectOption("Berätta mer", MESSAGE_KONTRAKT_CHARITY_TELLMEMORE));
          }
        }));

    createChatMessage(
      MESSAGE_CHARITY_UNKOWN_CHOICE,
      new MessageBodySingleSelect(
        "Jag känner inte igen det alternativ du valt, du kan välja en av dessa välgörenhetsorganisationer",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption(SOS_BARNBYAR_NAME, SOSBARNBYAR_VALUE));
            add(new SelectOption(BARNCANCERFONDEN_NAME, BARNCANCERFONDEN_VALUE));
            add(new SelectOption("Berätta mer", MESSAGE_KONTRAKT_CHARITY_TELLMEMORE));
          }
        }));

    createChatMessage(
      MESSAGE_KONTRAKT_CHARITY_TELLMEMORE,
      new MessageBodySingleSelect(
        "Så här, Hedvig fungerar inte som ett vanligt försäkringsbolag\f"
          + "Vi tar ut en fast avgift för att kunna ge dig bra service\f"
          + "Resten av det du betalar öronmärks för att ersätta skador\f"
          + "När alla skador har betalats skänks överskottet till organisationer som gör världen bättre\f"
          + "Du väljer själv vad ditt hjärta klappar för!",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption(SOS_BARNBYAR_NAME, SOSBARNBYAR_VALUE));
            add(new SelectOption(BARNCANCERFONDEN_NAME, BARNCANCERFONDEN_VALUE));
          }
        }));

    createMessage(
      MESSAGE_KONTRAKT_CHARITY_TACK,
      new MessageBodySingleSelect(
        "Toppen, tack!",
        new ArrayList<SelectItem>() {
          {
            // add(new SelectLink("Börja utforska appen", "onboarding.done", "Dashboard", null,
            // null,  false));
          }
        }));

    createMessage(
      MESSAGE_KONTRAKT_CHARITY_TACK_END,
      new MessageBodySingleSelect(
        "Toppen, tack!",
        new ArrayList<SelectItem>() {
          {
            add(SelectLink.toDashboard("Börja utforska appen", "onboarding.done"));
          }
        }));
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

    String nxtMsg = MESSAGE_KONTRAKT_CHARITY;
    switch (m.getStrippedBaseMessageId()) {
      case MESSAGE_CHARITY_UNKOWN_CHOICE:
      case MESSAGE_KONTRAKT_CHARITY_TELLMEMORE:
      case MESSAGE_KONTRAKT_CHARITY:
        MessageBodySingleSelect mss = (MessageBodySingleSelect) m.body;
        final SelectItem selectedItem = mss.getSelectedItem();

        if (selectedItem.value.startsWith("charity")) {
          m.body.text = "Jag vill att mitt överskott ska gå till " + selectedItem.text;
          addToChat(m);

          getUserContext().putUserData("{CHARITY}", selectedItem.value);
          getUserContext().setOnboardingComplete();

          val charityId = getCharityId(selectedItem.value);
          if (charityId.isPresent()) {

            memberService.selectCashback(getUserContext().getMemberId(), charityId.get());

            getUserContext().completeConversation(this);

            nxtMsg = MESSAGE_KONTRAKT_CHARITY_TACK;
            addToChat(nxtMsg);
            getUserContext().startConversation(
              conversationFactory.createConversation(MemberSourceConversation.class, getUserContext()));
            return;
          }

          nxtMsg = MESSAGE_CHARITY_UNKOWN_CHOICE;
        } else {
          m.body.text = selectedItem.text;
          nxtMsg = selectedItem.value;
          addToChat(m);
        }
        break;
    }

    completeRequest(nxtMsg);
  }

  private Optional<UUID> getCharityId(final String charity) {
    switch (charity) {
      case BARNCANCERFONDEN_VALUE:
        return Optional.of(BARNCANCERFONDEN_ID);
      case SOSBARNBYAR_VALUE:
        return Optional.of(SOS_BARNBYAR_ID);
      default:
        return Optional.empty();
    }
  }

  @Override
  public void receiveEvent(EventTypes e, String value) {

    switch (e) {
      // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        log.info("Message fetched: " + value);

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
    startConversation(MESSAGE_KONTRAKT_CHARITY);
  }

  @Override
  public void init(String startMessage) {
    startConversation(startMessage);
  }
}
