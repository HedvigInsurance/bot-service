package com.hedvig.botService.chat;

import com.hedvig.botService.enteties.SignupCodeRepository;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.triggerService.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ConversationFactoryImpl implements ConversationFactory {
  private final Logger log = LoggerFactory.getLogger(ConversationFactoryImpl.class);
  private final MemberService memberService;
  private final ProductPricingService productPricingService;
  private final TriggerService triggerService;
  private final SignupCodeRepository signupCodeRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ClaimsService claimsService;

  private final StatusBuilder statusBuilder;
  private Integer queuePos;

  public ConversationFactoryImpl(
      MemberService memberService,
      ProductPricingService productPricingService,
      TriggerService triggerService,
      SignupCodeRepository signupCodeRepository,
      ApplicationEventPublisher eventPublisher,
      ClaimsService claimsService,
      StatusBuilder statusBuilder,
      @Value("${hedvig.waitlist.length}") Integer queuePos) {
    this.memberService = memberService;
    this.productPricingService = productPricingService;
    this.triggerService = triggerService;

    this.signupCodeRepository = signupCodeRepository;
    this.eventPublisher = eventPublisher;
    this.claimsService = claimsService;
    this.statusBuilder = statusBuilder;
    this.queuePos = queuePos;
  }

  @Override
  public Conversation createConversation(Class<?> conversationClass) {

    if (conversationClass.equals(CharityConversation.class)) {
      return new CharityConversation(this, memberService, productPricingService);
    }

    if (conversationClass.equals(ClaimsConversation.class)) {
      return new ClaimsConversation(eventPublisher, claimsService, productPricingService, this, memberService);
    }

    if (conversationClass.equals(MainConversation.class)) {
      return new MainConversation(this, eventPublisher);
    }

    if (conversationClass.equals(OnboardingConversationDevi.class)) {
      final OnboardingConversationDevi onboardingConversationDevi =
          new OnboardingConversationDevi(
              memberService, productPricingService, signupCodeRepository, eventPublisher, this);
      onboardingConversationDevi.setQueuePos(queuePos);
      return onboardingConversationDevi;
    }

    if (conversationClass.equals(TrustlyConversation.class)) {
      return new TrustlyConversation(triggerService, memberService);
    }

    if (conversationClass.equals(FreeChatConversation.class)) {
      return new FreeChatConversation(statusBuilder, eventPublisher, productPricingService);
    }

    if (conversationClass.equals(CallMeConversation.class)) {
      return new CallMeConversation(eventPublisher);
    }

    if(conversationClass.equals(MemberSourceConversation.class)) {
      return new MemberSourceConversation();
    }

    return null;
  }

  @Override
  public Conversation createConversation(String conversationClassName) {
    try {
      Class<?> concreteClass = Class.forName(conversationClassName);
      return createConversation(concreteClass);
    } catch (ClassNotFoundException ex) {
      log.error("Could not create conversation for classname: {}", conversationClassName, ex);
    }

    return null;
  }
}
