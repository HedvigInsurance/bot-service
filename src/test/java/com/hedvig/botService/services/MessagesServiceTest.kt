package com.hedvig.botService.services

import com.hedvig.botService.chat.Conversation
import com.hedvig.botService.chat.ConversationFactory
import com.hedvig.botService.enteties.MessageRepository
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.UserContextRepository
import com.hedvig.botService.web.v2.dto.FABAction
import com.hedvig.localization.service.TextKeysLocaleResolverImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.Optional

class MessagesServiceTest {

    @MockK
    lateinit var userContextRepository: UserContextRepository

    @MockK
    lateinit var convsersationFactory: ConversationFactory

    @MockK(relaxed = true)
    lateinit var mockConversation: Conversation

    @MockK
    lateinit var messageRepository: MessageRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `messagesAndStatus_sets_locale`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.getMessagesAndStatus("1337", "sv-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3", SessionManager.Intent.ONBOARDING)

        assertThat(userContext.locale).isEqualTo(Locale("sv", "se"))
    }

    @Test
    fun `messagesAndStatus without previous locale and no header`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.getMessagesAndStatus("1337", null, SessionManager.Intent.ONBOARDING)

        assertThat(userContext.locale).isEqualTo(TextKeysLocaleResolverImpl.DEFAULT_LOCALE)
    }

    @Test
    fun `messagesAndStatus without old locale and no header`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()


        userContext.putUserData(UserContext.LANGUAGE_KEY, "se")

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.getMessagesAndStatus("1337", null, SessionManager.Intent.ONBOARDING)

        assertThat(userContext.locale).isEqualTo(TextKeysLocaleResolverImpl.DEFAULT_LOCALE)
    }


    @Test
    fun `fabTrigger without old locale and no header`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()


        userContext.putUserData(UserContext.LANGUAGE_KEY, "se")

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.fabTrigger("1337", null, FABAction.CALL_ME)

        assertThat(userContext.locale).isEqualTo(TextKeysLocaleResolverImpl.DEFAULT_LOCALE)
    }

    @Test
    fun `fabTrigger with accept language`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.fabTrigger("1337", "en-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3", FABAction.CALL_ME)

        assertThat(userContext.locale).isEqualTo(Locale("en", "se"))
    }

    @Test
    fun `fabTrigger with norsk bokmål as accept language`() {

        val userContext = UserContext("1337")
        every { userContextRepository.findByMemberId("1337") } returns Optional.of(userContext)
        every {
            convsersationFactory.createConversation(any<Class<Any>>(), any())
        } returns mockConversation

        val textKeysLocaleResolver = TextKeysLocaleResolverImpl()

        val cut =
            MessagesService(userContextRepository, convsersationFactory, messageRepository, textKeysLocaleResolver)

        cut.fabTrigger("1337", "nb-NO", FABAction.CALL_ME)

        assertThat(userContext.locale).isEqualTo(Locale("nb", "no"))
    }
}
