package com.hedvig.botService.chat

import com.hedvig.botService.enteties.MemberChat
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.testHelpers.MessageHelpers.createSingleSelectMessage
import com.hedvig.botService.testHelpers.MessageHelpers.createTextMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.BDDMockito.then
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.context.ApplicationEventPublisher

@RunWith(MockitoJUnitRunner::class)
class ConversationTest {
  private lateinit var sut: Conversation

  @Mock
  private val eventPublisher: ApplicationEventPublisher? = null

  private lateinit var uc: UserContext

  @Spy
  internal var mc: MemberChat? = null
  @Captor
  internal var messageCaptor: ArgumentCaptor<Message>? = null

  @Before
  fun setup() {
    uc = UserContext("111111")
    uc.memberChat = mc

    sut = makeConversation {  }
  }

  @Test
  fun addToChat_withAddToChatCallback_executesCallback(){

    var executed = false;

    sut = makeConversation {
      val wrappedMessage = WrappedMessage(MessageBodyText("Test"), { _ -> executed = true; }, { _, _, _ -> "true" })
      this.createChatMessage("test.message", wrappedMessage)
    }

    sut.addToChat("test.message", uc)

    assertThat(executed).isTrue()

  }

  @Test
  @Throws(Exception::class)
  fun addToChat_renders_selectLink() {
    // Arrange
    uc.putUserData("{TEST}", "localhost")

    val m = createSingleSelectMessage(
      "En förklarande text",
      SelectLink(
        "Länk text",
        "selected.value",
        null,
        "bankid:///{TEST}/text",
        "http://{TEST}/text",
        false))

    // ACT
    sut.addToChat(m, uc)

    // Assert
    then<MemberChat>(mc).should().addToHistory(messageCaptor!!.capture())
    val body = messageCaptor!!.value.body as MessageBodySingleSelect

    val link = body.choices[0] as SelectLink
    assertThat(link.appUrl).isEqualTo("bankid:///localhost/text")
    assertThat(link.webUrl).isEqualTo("http://localhost/text")
  }

  @Test
  @Throws(Exception::class)
  fun addToChat_renders_message() {
    // Arrange
    uc.putUserData("{REPLACE_THIS}", "kort")

    val m = createTextMessage("En förklarande {REPLACE_THIS} text")

    // ACT
    sut.addToChat(m, uc)

    // Assert
    then<MemberChat>(mc).should().addToHistory(messageCaptor!!.capture())
    val body = messageCaptor!!.value.body

    assertThat(body.text).isEqualTo("En förklarande kort text")
  }

  @Test
  fun `receiveMessage_withRegisteredSingleSelectCallback_callsCallback`() {
    var called = false

    val testClass = makeConversation {
      this.createChatMessage(
        "message.id", WrappedMessage(
        MessageBodySingleSelect(
          "hej", listOf(
          SelectItem(false, "Text", "value")))
      ) { _, _, _ ->
        called = true
        ""
      })
    }


    testClass.receiveMessage(uc, makeMessage("message.id", MessageBodySingleSelect("", listOf())))

    assertThat(called).isTrue()

  }

  @Test
  fun receiveMessage_withRegisteredMessageBodyTextCallback_callsCallback() {
    var called = false

    val testClass = makeConversation {
      this.createChatMessage(
        "message.id", WrappedMessage(
        MessageBodyText("hej"),
        receiveMessageCallback = { _, _, _ ->
          called = true
          ""
        }))
    }


    testClass.receiveMessage(uc, makeMessage("message.id", MessageBodyText("")))

    assertThat(called).isTrue()

  }

  fun makeMessage(id: String, body: MessageBody): Message {
    val m = Message()
    m.id = id
    m.body = body
    return m
  }


  fun makeConversation(constructor: Conversation.(Unit) -> Unit): Conversation {
    return object : Conversation(eventPublisher!!) {
      override fun getSelectItemsForAnswer(uc: UserContext): List<SelectItem> {
        return listOf()
      }

      override fun canAcceptAnswerToQuestion(uc: UserContext): Boolean {
        return false
      }

      override fun handleMessage(userContext: UserContext, m: Message) {

      }

      override fun init(userContext: UserContext) {
      }

      override fun init(userContext: UserContext, startMessage: String) {
      }

      init {
        constructor.invoke(this, Unit)
      }
    }
  }

  companion object {

    @JvmField
    val TESTMESSAGE_ID = "testmessage"
  }
}
