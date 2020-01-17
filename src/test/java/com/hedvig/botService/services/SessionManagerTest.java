package com.hedvig.botService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hedvig.botService.chat.*;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.hedvig.botService.enteties.TrackingDataRespository;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdStatusType;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.serviceIntegration.underwriter.Underwriter;
import com.hedvig.botService.web.dto.AddMessageRequestDTO;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static com.hedvig.botService.enteties.message.MessageHeader.HEDVIG_USER_ID;
import static com.hedvig.botService.services.TriggerServiceTest.TOLVANSSON_MEMBERID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

  public static final String MESSAGE = "Heh hej";
  public static final SelectLink SELECT_LINK = SelectLink.toOffer("Offer", "offer");
  public static final String TOLVANSSON_FCM_TOKEN = "test-token";
  @Mock UserContextRepository userContextRepository;

  @Mock MemberService memberService;

  @Mock ProductPricingService productPricingService;

  @Mock TrackingDataRespository campaignCodeRepository;

  @Mock ConversationFactory conversationFactory;

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  Conversation mockConversation;

  @Mock MessagesService messagesService;

  @Mock ClaimsService claimsService;

  @Mock LocalizationService localizationService;

  @Mock Underwriter underwriter;

  @Mock
  TextKeysLocaleResolver localeResolver;

  @Mock PhoneNumberUtil phoneNumberUtil;

  @Mock
  ApplicationEventPublisher applicationEventPublisher;

  SessionManager sessionManager;

  @Before
  public void setUp() {
    val objectMapper = new ObjectMapper();
    sessionManager =
        new SessionManager(
            userContextRepository,
            memberService,
          conversationFactory,
            campaignCodeRepository,
            objectMapper,
            localeResolver);
  }

  // FIXME
  @Test
  public void
      givenConversationThatCanAcceptMessage_WhenAddMessageFromHedvig_ThenAddsMessageToHistory() {

    val tolvanssonUserContext = makeTolvanssonUserContext();
    startMockConversation(tolvanssonUserContext);

    when(userContextRepository.findByMemberId(TOLVANSSON_MEMBERID))
        .thenReturn(Optional.of(tolvanssonUserContext));
    when(conversationFactory.createConversation(anyString(), eq(tolvanssonUserContext))).thenReturn(mockConversation);

    when(mockConversation.canAcceptAnswerToQuestion()).thenReturn(true);
    when(mockConversation.getSelectItemsForAnswer())
        .thenReturn(Lists.newArrayList(SELECT_LINK));
    when(mockConversation.getUserContext())
        .thenReturn(tolvanssonUserContext);

    AddMessageRequestDTO requestDTO = new AddMessageRequestDTO(TOLVANSSON_MEMBERID, MESSAGE, false);

    val messageCouldBeAdded = sessionManager.addMessageFromHedvig(requestDTO);

    assertThat(messageCouldBeAdded).isTrue();

    Message message = Iterables.getLast(tolvanssonUserContext.getMemberChat().chatHistory);
    assertThat(message.body.text).isEqualTo(MESSAGE);
    assertThat(message.id).isEqualTo("message.bo.message");
    assertThat(message.header.fromId).isEqualTo(HEDVIG_USER_ID);
    assertThat(((MessageBodySingleSelect) message.body).choices).containsExactly(SELECT_LINK);
  }

  // FIXME
  @Test
  public void givenConversationThatCanNotAcceptMessage_WhenAddMessageFromHedvig_ThenReturnFalse() {

    val tolvanssonUserContext = makeTolvanssonUserContext();
    startMockConversation(tolvanssonUserContext);

    when(userContextRepository.findByMemberId(TOLVANSSON_MEMBERID))
        .thenReturn(Optional.of(tolvanssonUserContext));
    when(mockConversation.canAcceptAnswerToQuestion()).thenReturn(false);
    when(conversationFactory.createConversation(anyString(), eq(tolvanssonUserContext))).thenReturn(mockConversation);

    AddMessageRequestDTO requestDTO = new AddMessageRequestDTO(TOLVANSSON_MEMBERID, MESSAGE, false);

    val messageCouldBeAdded = sessionManager.addMessageFromHedvig(requestDTO);

    assertThat(messageCouldBeAdded).isFalse();
  }

  @Test
  public void givenForceSendMessageANDConversationThatCanNotAcceptMessage_WhenAddMessageFromHedvig_ThenStartNewConversationReturnTrue() {

    val mockFreeTextConversation  = mock(FreeChatConversation.class);

    //Conversationfactory should return mocked conversation
    when(conversationFactory.createConversation(anyString(),any())).thenReturn(mockConversation, mockFreeTextConversation);

    val tolvanssonUserContext = makeTolvanssonUserContext();
    startMockConversation(tolvanssonUserContext);

    when(userContextRepository.findByMemberId(TOLVANSSON_MEMBERID))
      .thenReturn(Optional.of(tolvanssonUserContext));

    when(conversationFactory.createConversation(any(Class.class), any()))
      .thenReturn(mockFreeTextConversation);
    when(mockFreeTextConversation.addMessageFromBackOffice(anyString(),anyString(),any())).thenReturn(true);


    AddMessageRequestDTO requestDTO = new AddMessageRequestDTO(TOLVANSSON_MEMBERID, MESSAGE, true);
    val messageCouldBeAdded = sessionManager.addMessageFromHedvig(requestDTO);

    assertThat(messageCouldBeAdded).isTrue();
  }

  @Test
  public void givenNoExistingConversation_whenGetAllMessages_thenOnboardingConversationIsStarted() {

    val tolvanssonUserContext = makeTolvanssonUserContext();

    when(userContextRepository.findByMemberId(TOLVANSSON_MEMBERID))
        .thenReturn(Optional.of(tolvanssonUserContext));
    val onboardingConversation = makeOnboardingConversation(tolvanssonUserContext);
    when(conversationFactory.createConversation(any(Class.class), any()))
        .thenReturn(onboardingConversation);
    when(localeResolver.resolveLocale(any())).thenReturn(TextKeysLocaleResolver.Companion.getDEFAULT_LOCALE());

    val messages = sessionManager.getAllMessages(TOLVANSSON_MEMBERID,  null, null);

    assertThat(Iterables.getLast(messages))
        .hasFieldOrPropertyWithValue(
            "id", OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME);
    assertThat(tolvanssonUserContext.getActiveConversation().get()).isNotNull();
  }

  @Test
  public void
      givenNoExistingConversation_whenGetAllMessagesWithIntentLOGIN_thenOnboardingConversationIsStarted() {

    val tolvanssonUserContext = makeTolvanssonUserContext();

    when(userContextRepository.findByMemberId(TOLVANSSON_MEMBERID))
        .thenReturn(Optional.of(tolvanssonUserContext));
    val onboardingConversation = makeOnboardingConversation(tolvanssonUserContext);
    when(conversationFactory.createConversation(any(Class.class), anyObject()))
      .thenReturn(onboardingConversation);
    when(memberService.auth(TOLVANSSON_MEMBERID)).thenReturn(Optional.of(makeBankIdResponse()));
    when(localeResolver.resolveLocale(any())).thenReturn(TextKeysLocaleResolver.Companion.getDEFAULT_LOCALE());

    val messages = sessionManager.getAllMessages(TOLVANSSON_MEMBERID, null, SessionManager.Intent.LOGIN);

    assertThat(Iterables.getLast(messages))
        .hasFieldOrPropertyWithValue("id", "message.start.login");
  }

  private OnboardingConversationDevi makeOnboardingConversation(UserContext userContext) {
    return new OnboardingConversationDevi(memberService, productPricingService, underwriter, applicationEventPublisher, conversationFactory, localizationService, "test", "test", phoneNumberUtil, userContext) ;
  }

  private BankIdAuthResponse makeBankIdResponse() {
    return new BankIdAuthResponse(
        BankIdStatusType.STARTED, UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private UserContext makeTolvanssonUserContext() {
    val tolvanssonUserContext = new UserContext(TOLVANSSON_MEMBERID);

    return tolvanssonUserContext;
  }

  private void startMockConversation(UserContext tolvanssonUserContext) {
    tolvanssonUserContext.startConversation(mockConversation);
  }
}
