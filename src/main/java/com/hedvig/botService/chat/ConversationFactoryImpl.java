package com.hedvig.botService.chat;

import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.LocalizationService;
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
  private final LocalizationService localizationService;
  private final ProductPricingService productPricingService;
  private final TriggerService triggerService;
  private final ApplicationEventPublisher eventPublisher;
  private final ClaimsService claimsService;

  private final StatusBuilder statusBuilder;
  private Integer queuePos;
  private String appleUserEmail;
  private String appleUserPwd;

  public ConversationFactoryImpl(
    MemberService memberService,
    ProductPricingService productPricingService,
    TriggerService triggerService,
    ApplicationEventPublisher eventPublisher,
    ClaimsService claimsService,
    StatusBuilder statusBuilder,
    LocalizationService localizationService,
    @Value("${hedvig.waitlist.length}") Integer queuePos,
    @Value("${hedvig.appleUser.email}") String appleUserEmail,
    @Value("${hedvig.appleUser.password}") String appleUserPwd) {
    this.memberService = memberService;
    this.productPricingService = productPricingService;
    this.triggerService = triggerService;

    this.eventPublisher = eventPublisher;
    this.claimsService = claimsService;
    this.statusBuilder = statusBuilder;
    this.localizationService = localizationService;
    this.queuePos = queuePos;
    this.appleUserEmail = appleUserEmail;
    this.appleUserPwd = appleUserPwd;
  }

  @Override
  public Conversation createConversation(Class<?> conversationClass) {

    if (conversationClass.equals(CharityConversation.class)) {
      return new CharityConversation(this, memberService, productPricingService, eventPublisher, localizationService);
    }

    if (conversationClass.equals(ClaimsConversation.class)) {
      return new ClaimsConversation(eventPublisher, claimsService, productPricingService, this, memberService, localizationService);
    }

    if (conversationClass.equals(MainConversation.class)) {
      return new MainConversation(this, eventPublisher, localizationService);
    }

    if (conversationClass.equals(OnboardingConversationDevi.class)) {
      final OnboardingConversationDevi onboardingConversationDevi =
          new OnboardingConversationDevi(
              memberService, productPricingService, eventPublisher, this, localizationService, appleUserEmail,appleUserPwd);
      onboardingConversationDevi.setQueuePos(queuePos);
      return onboardingConversationDevi;
    }

    if (conversationClass.equals(TrustlyConversation.class)) {
      return new TrustlyConversation(triggerService, memberService, eventPublisher, localizationService);
    }

    if (conversationClass.equals(FreeChatConversation.class)) {
      return new FreeChatConversation(statusBuilder, eventPublisher, productPricingService, localizationService);
    }

    if (conversationClass.equals(CallMeConversation.class)) {
      return new CallMeConversation(eventPublisher, localizationService);
    }

    if(conversationClass.equals(MemberSourceConversation.class)) {
      return new MemberSourceConversation(eventPublisher, localizationService);
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
