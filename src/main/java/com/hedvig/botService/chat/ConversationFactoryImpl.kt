package com.hedvig.botService.chat

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hedvig.botService.chat.house.HouseOnboardingConversation
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService
import com.hedvig.botService.serviceIntegration.lookupService.LookupService
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService
import com.hedvig.botService.serviceIntegration.underwriter.Underwriter
import com.hedvig.botService.services.triggerService.TriggerService
import com.hedvig.libs.translations.Translations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ConversationFactoryImpl(
    private val memberService: MemberService,
    private val lookupService: LookupService,
    private val underwriter: Underwriter,
    private val productPricingService: ProductPricingService,
    private val triggerService: TriggerService,
    private val eventPublisher: ApplicationEventPublisher,
    private val claimsService: ClaimsService,
    private val statusBuilder: StatusBuilder,
    private val translations: Translations,
    @Value("\${hedvig.waitlist.length}") private val queuePos: Int?,
    @Value("\${hedvig.appleUser.email}") private val appleUserEmail: String,
    @Value("\${hedvig.appleUser.password}") private val appleUserPwd: String,
    private val phoneNumberUtil: PhoneNumberUtil
) : ConversationFactory {
    private val log = LoggerFactory.getLogger(ConversationFactoryImpl::class.java)

    override fun createConversation(conversationClass: Class<*>, userContext: UserContext): Conversation {

        if (conversationClass == CharityConversation::class.java) {
            return CharityConversation(this, memberService, productPricingService, eventPublisher, translations, userContext)
        }

        if (conversationClass == ClaimsConversation::class.java) {
            return ClaimsConversation(
                eventPublisher,
                claimsService,
                productPricingService,
                this,
                memberService,
                translations,
                userContext
            )
        }

        if (conversationClass == MainConversation::class.java) {
            return MainConversation(this, eventPublisher, translations, userContext)
        }

        if (conversationClass == OnboardingConversationDevi::class.java) {
            val onboardingConversationDevi = OnboardingConversationDevi(
                memberService,
                underwriter,
                eventPublisher,
                this,
                translations,
                appleUserEmail,
                appleUserPwd,
                phoneNumberUtil,
                userContext
            )
            onboardingConversationDevi.queuePos = queuePos
            return onboardingConversationDevi
        }

        if (conversationClass == HouseOnboardingConversation::class.java) {
            val houseOnboardingConversation = HouseOnboardingConversation(
                memberService, lookupService, eventPublisher, this, translations, userContext
            )
            houseOnboardingConversation.queuePos = queuePos
            return houseOnboardingConversation
        }

        if (conversationClass == TrustlyConversation::class.java) {
            return TrustlyConversation(triggerService, eventPublisher, translations, userContext)
        }

        if (conversationClass == FreeChatConversation::class.java) {
            return FreeChatConversation(statusBuilder, eventPublisher, productPricingService, translations, userContext)
        }

        if (conversationClass == CallMeConversation::class.java) {
            return CallMeConversation(eventPublisher, translations, userContext)
        }

        return if (conversationClass == MemberSourceConversation::class.java) {
            MemberSourceConversation(eventPublisher, translations, userContext)
        } else throw RuntimeException("Failed to create conversation")

    }

    override fun createConversation(conversationClassName: String, userContext: UserContext): Conversation {
        try {
            val concreteClass = Class.forName(conversationClassName)
            return createConversation(concreteClass, userContext)
        } catch (ex: ClassNotFoundException) {
            log.error("Could not create conversation for classname: {}", conversationClassName, ex)
        }

        throw RuntimeException("Failed to create conversation")
    }
}
