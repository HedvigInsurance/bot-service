package com.hedvig.botService.enteties

import com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID

import com.hedvig.botService.chat.Conversation
import com.hedvig.botService.chat.ConversationFactory
import com.hedvig.botService.chat.FreeChatConversation
import com.hedvig.botService.chat.OnboardingConversationDevi
import com.hedvig.botService.services.SessionManager.Intent
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class UserContextTest {

    @Mock
    internal var conversationFactory: ConversationFactory? = null
    @Mock
    internal var mockConversation: Conversation? = null

    @Test
    fun getMessages_withIntentOnboarding_callsInitWithCorrectStartMessage() {

        val uc = UserContext(TOLVANSSON_MEMBER_ID)

        given(conversationFactory!!.createConversation(OnboardingConversationDevi::class.java, uc))
            .willReturn(mockConversation)

        uc.getMessages(Intent.ONBOARDING, conversationFactory)

        then<Conversation>(mockConversation)
            .should(times(1))
            .init(OnboardingConversationDevi.MESSAGE_ONBOARDINGSTART_ASK_NAME)
    }

    @Test
    fun getMessages_initFreeChat_callsInitWithCorrectStartMessage() {

        val uc = UserContext(TOLVANSSON_MEMBER_ID)
        uc.locale = Locale("en", "NO")

        given(conversationFactory!!.createConversation(FreeChatConversation::class.java, uc))
            .willReturn(mockConversation)

        uc.getMessages(Intent.ONBOARDING, conversationFactory)

        then<Conversation>(mockConversation)
            .should(times(1))
            .init(FreeChatConversation.FREE_CHAT_ONBOARDING_START)
    }
}
