package com.hedvig.botService.chat

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService
import com.hedvig.botService.services.LocalizationService
import com.hedvig.botService.services.triggerService.TriggerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.*

@Component
class ConversationFactoryImpl(
    private val memberService: MemberService,
    private val productPricingService: ProductPricingService,
    private val triggerService: TriggerService,
    private val eventPublisher: ApplicationEventPublisher,
    private val claimsService: ClaimsService,
    private val statusBuilder: StatusBuilder,
    private val localizationService: LocalizationService,
    @Value("\${hedvig.waitlist.length}") private val queuePos: Int?,
    @Value("\${hedvig.appleUser.email}") private val appleUserEmail: String,
    @Value("\${hedvig.appleUser.password}") private val appleUserPwd: String,
    private val phoneNumberUtil: PhoneNumberUtil
) : ConversationFactory {
    private val log = LoggerFactory.getLogger(ConversationFactoryImpl::class.java)

    override fun createConversation(conversationClass: Class<*>, userLocale: Locale?): Conversation {

        if (conversationClass == CharityConversation::class.java) {
            return CharityConversation(this, memberService, productPricingService, eventPublisher, localizationService)
              .also { it.userLocale = userLocale }
        }

        if (conversationClass == ClaimsConversation::class.java) {
            return ClaimsConversation(
                eventPublisher,
                claimsService,
                productPricingService,
                this,
                memberService,
                localizationService
            ).also { it.userLocale = userLocale }
        }

        if (conversationClass == MainConversation::class.java) {
            return MainConversation(this, eventPublisher, localizationService)
              .also { it.userLocale = userLocale }
        }

        if (conversationClass == OnboardingConversationDevi::class.java) {
            val onboardingConversationDevi = OnboardingConversationDevi(
                memberService,
                productPricingService,
                eventPublisher,
                this,
                localizationService,
                appleUserEmail,
                appleUserPwd,
                phoneNumberUtil
            ).also { it.userLocale = userLocale }
            onboardingConversationDevi.queuePos = queuePos
            return onboardingConversationDevi
        }

        if (conversationClass == TrustlyConversation::class.java) {
            return TrustlyConversation(triggerService, memberService, eventPublisher, localizationService)
              .also { it.userLocale = userLocale }
        }

        if (conversationClass == FreeChatConversation::class.java) {
            return FreeChatConversation(statusBuilder, eventPublisher, productPricingService, localizationService)
              .also { it.userLocale = userLocale }
        }

        if (conversationClass == CallMeConversation::class.java) {
            return CallMeConversation(eventPublisher, localizationService)
              .also { it.userLocale = userLocale }
        }

        return if (conversationClass == MemberSourceConversation::class.java) {
            MemberSourceConversation(eventPublisher, localizationService)
              .also { it.userLocale = userLocale }
        } else throw RuntimeException("Failed to create conversation")

    }

    override fun createConversation(conversationClassName: String, userLocale: Locale?): Conversation {
        try {
            val concreteClass = Class.forName(conversationClassName)
            return createConversation(concreteClass, userLocale)
        } catch (ex: ClassNotFoundException) {
            log.error("Could not create conversation for classname: {}", conversationClassName, ex)
        }

        throw RuntimeException("Failed to create conversation")
    }
}
