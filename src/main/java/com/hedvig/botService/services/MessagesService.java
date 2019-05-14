package com.hedvig.botService.services;

import com.google.common.collect.Lists;
import com.hedvig.botService.chat.CallMeConversation;
import com.hedvig.botService.chat.ClaimsConversation;
import com.hedvig.botService.chat.ConversationFactory;
import com.hedvig.botService.chat.FreeChatConversation;
import com.hedvig.botService.chat.TrustlyConversation;
import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.ResourceNotFoundException;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.web.v2.dto.FABAction;
import com.hedvig.botService.web.v2.dto.MessagesDTO;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@Transactional
public class MessagesService {
  public static final String triggerUrl = "/v2/app/fabTrigger/%s";

  private final UserContextRepository userContextRepository;
  private final ConversationFactory conversationFactory;
  private final ClaimsService claimsService;

  public MessagesService(
      UserContextRepository userContextRepository,
      ConversationFactory conversationFactory,
      ClaimsService claimsService) {
    this.userContextRepository = userContextRepository;
    this.conversationFactory = conversationFactory;
    this.claimsService = claimsService;
  }

  public MessagesDTO getMessagesAndStatus(String hid, SessionManager.Intent intent) {
    UserContext uc =
        userContextRepository
            .findByMemberId(hid)
            .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

    val messages = uc.getMessages(intent, conversationFactory);

    boolean hasClaim = false; // this.claimsService.getActiveClaims(hid) > 0;

    val options =
        Lists.newArrayList(
            new MessagesDTO.FABOption(
                "Anmäl en skada", createFabTriggerUrl(FABAction.REPORT_CLAIM), !hasClaim),
            new MessagesDTO.FABOption(
                "Prata med Hedvig", createFabTriggerUrl(FABAction.CHAT), true),
            new MessagesDTO.FABOption(
                "Det är kris! Ring mig", createFabTriggerUrl(FABAction.CALL_ME), true));

    val forceTrustly = uc.getDataEntry(UserContext.FORCE_TRUSTLY_CHOICE);
    if ("true".equalsIgnoreCase(forceTrustly)) {
      options.add(
          0,
          new MessagesDTO.FABOption(
              "Koppla autogiro", createFabTriggerUrl(FABAction.TRUSTLY), true));
    }
    return new MessagesDTO(
        new MessagesDTO.State(hasClaim, uc.inOfferState(), uc.hasCompletedOnboarding()),
        messages,
        options);
  }

  public Message getMessage(UserContext uc, Integer globalId) {
    List<Message> messages = uc.getMemberChat().chatHistory;

    messages.removeIf(s -> s.globalId != globalId);

    if (messages.size() == 0) {
      throw new ResourceNotFoundException("Coud not find message.");
    }

    return messages.get(messages.size() - 1);
  }

  public Message markAsRead(String hid, Integer globalId) {
    UserContext uc =
      userContextRepository
        .findByMemberId(hid)
        .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

    Message message = getMessage(uc, globalId);
    MemberChat chat = uc.getMemberChat();

    List<Message> messages = chat.chatHistory;
    Integer index = messages.indexOf(message);

    message.markAsRead();
    messages.set(index, message);

    uc.setMemberChat(chat);
    userContextRepository.saveAndFlush(uc);

    return message;
  }

  public ResponseEntity<?> fabTrigger(String hid, FABAction actionId) {

    UserContext uc =
        userContextRepository
            .findByMemberId(hid)
            .orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

    switch (actionId) {
      case CHAT:
        uc.startConversation(conversationFactory.createConversation(FreeChatConversation.class));
        break;
      case CALL_ME:
        uc.startConversation(conversationFactory.createConversation(CallMeConversation.class));
        break;
      case REPORT_CLAIM:
        uc.startConversation(conversationFactory.createConversation(ClaimsConversation.class));
        break;
      case TRUSTLY:
        uc.startConversation(
            conversationFactory.createConversation(TrustlyConversation.class),
            TrustlyConversation.FORCED_START);
    }

    return ResponseEntity.accepted().build();
  }

  private String createFabTriggerUrl(FABAction action) {
    return String.format(triggerUrl, action.name());
  }
}
