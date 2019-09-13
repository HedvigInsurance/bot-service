package com.hedvig.botService.services;

import static com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_ONBOARDING_START;

import com.hedvig.botService.chat.ConversationFactory;
import com.hedvig.botService.chat.FreeChatConversation;
import com.hedvig.botService.chat.OnboardingConversationDevi;
import com.hedvig.botService.config.SwitchableInsurers;
import com.hedvig.botService.enteties.BankIdSessionImpl;
import com.hedvig.botService.enteties.ResourceNotFoundException;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdProgressStatus;
import com.hedvig.botService.serviceIntegration.memberService.exceptions.BankIdError;
import com.hedvig.botService.serviceIntegration.memberService.exceptions.ErrorType;
import com.hedvig.botService.web.dto.BankidStartResponse;

import java.time.Instant;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
@Transactional
public class OnboardingService {

  private final Logger log = LoggerFactory.getLogger(OnboardingService.class);
  private final MemberService memberService;
  private final UserContextRepository userContextRepository;
  private final ConversationFactory conversationFactory;

  public OnboardingService(
    MemberService memberService,
    UserContextRepository userContextRepository,
    ConversationFactory conversationFactory) {
    this.memberService = memberService;
    this.userContextRepository = userContextRepository;
    this.conversationFactory = conversationFactory;
  }

  public BankidStartResponse sign(String hid) {
    UserContext uc =
      userContextRepository
        .findByMemberId(hid)
        .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

    UserData ud = uc.getOnBoardingData();

    String signText = createUserSignText(ud);
    val signData = memberService.signEx(ud.getSSN(), signText, hid);

    uc.startBankIdSign(signData);

    userContextRepository.saveAndFlush(uc);

    return new BankidStartResponse(signData.getAutoStartToken(), signData.getReferenceToken());
  }

  public BankIdCollectResponse collect(@RequestHeader("hedvig.token") String hid, String orderRef) {
    UserContext uc =
      userContextRepository
        .findByMemberId(hid)
        .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

    BankIdSessionImpl bankIdCollectStatus = uc.getBankIdCollectStatus(orderRef);

    if (bankIdCollectStatus == null) {
      throw new BankIdError(ErrorType.INVALID_PARAMETERS, "Could not find order reference");
    }

    if (bankIdCollectStatus.allowedToCall() == false) {
      return new BankIdCollectResponse(
        BankIdProgressStatus.OUTSTANDING_TRANSACTION,
        bankIdCollectStatus.getReferenceToken(),
        null);
    }

    val collectResponse = this.memberService.collect(orderRef, hid);

    log.info(
      "BankIdStatus after collect:{}, memberId:{}, lastCollectionStatus: {}",
      collectResponse.getBankIdStatus().name(),
      hid,
      bankIdCollectStatus.getLastStatus());

    bankIdCollectStatus.setLastCallTime(Instant.now());
    bankIdCollectStatus.setLastStatus(collectResponse.getBankIdStatus().name());

    if (collectResponse.getBankIdStatus() == BankIdProgressStatus.COMPLETE) {
      OnboardingConversationDevi conversation =
        (OnboardingConversationDevi)
          conversationFactory.createConversation(OnboardingConversationDevi.class, uc.getLocale().getLanguage());
      conversation.memberSigned(collectResponse.getReferenceToken(), uc);
    }

    return collectResponse;
  }

  private String createUserSignText(UserData ud) {
    String signText;

    if (ud.getCurrentInsurer() == null) {
      signText = "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag skaffar en försäkring hos Hedvig.";
    } else if (SwitchableInsurers.SWITCHABLE_INSURERS.contains(ud.getCurrentInsurer())) {
      signText =
        "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag vill byta till Hedvig när min gamla försäkring går ut. Jag ger också  Hedvig fullmakt att byta försäkringen åt mig.";
    } else {
      signText =
        "Jag har tagit del av förköpsinformation samt villkor och bekräftar att jag vill byta till Hedvig när min nuvarande hemförsäkring går ut.";
    }
    return signText;
  }

  public void offerClosed(String hid) {

    val uc =
      userContextRepository
        .findByMemberId(hid)
        .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));
    uc.setInOfferState(true);
    val activeConversation =
      uc.getActiveConversation()
        .orElseThrow(() -> new RuntimeException("No active conversation."));

    if (activeConversation.getClassName().equals(FreeChatConversation.class.getName()) == false) {
      val conversation = conversationFactory.createConversation(FreeChatConversation.class, uc.getLocale().getLanguage());
      uc.startConversation(conversation, FREE_CHAT_ONBOARDING_START);
    }
  }
}
