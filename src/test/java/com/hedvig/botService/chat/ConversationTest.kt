package com.hedvig.botService.chat

import com.hedvig.botService.utils.ConversationUtils
import com.hedvig.botService.enteties.MemberChat
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.LocalizationService
import com.hedvig.botService.testHelpers.MessageHelpers.createSingleSelectMessage
import com.hedvig.botService.testHelpers.MessageHelpers.createTextMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.BDDMockito.*
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.context.ApplicationEventPublisher
import org.mockito.Mockito.`when`
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ConversationTest {
  private lateinit var sut: Conversation

  @Mock
  private val eventPublisher: ApplicationEventPublisher? = null

  @Mock
  private val localizationService: LocalizationService? = null

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

    sut.addToChat("test.message")

    assertThat(executed).isTrue()

  }

  @Test
  @Throws(Exception::class)
  fun addToChat_renders_selectLink() {
    // Arrange
    uc.putUserData("{TEST}", "localhost")

    val linkText = "Länk text"
    val linkValue = "selected.value"

    `when`(localizationService!!.getText(Mockito.any(Locale::class.java), Mockito.anyString())).thenReturn(linkText)

    val m = createSingleSelectMessage(
      "En förklarande text",
      true,
      SelectLink(
        linkText,
        linkValue,
        null,
        "bankid:///{TEST}/text",
        "http://{TEST}/text",
        false))


    // ACT
    sut.addToChat(m)

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
    sut.addToChat(m)

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

    testClass.receiveMessage(makeMessage("message.id", MessageBodySingleSelect("", listOf())))

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


    testClass.receiveMessage(makeMessage("message.id", MessageBodyText("")))

    assertThat(called).isTrue()

  }

  @Test
  fun conversationMessageSpitAndConversationUtils_whitNoSplit() {
    val key = "key1"
    val text = "Test1"

    `when`(localizationService!!.getText(null, key)).thenReturn(text)

    sut.createChatMessage(key, MessageBody(text))

    assertThat(sut.getMessage("key1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1"))).isEqualTo(text)
  }

  @Test
  fun conversationMessageSpitAndConversationUtils_whitOneFirstSplit() {
    val key = "key1"
    val text = "\u000CTest1"

    `when`(localizationService!!.getText(null, key)).thenReturn(text)

    sut.createChatMessage(key, MessageBody(text))

    assertThat(sut.getMessage("key1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.0")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.0"))).isEqualTo("")

    assertThat(sut.getMessage("key1.1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.2")?.body?.text).isEqualTo("Test1")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.2"))).isEqualTo("Test1")
  }

  @Test
  fun conversationMessageSpitAndConversationUtils_whitOneSplit() {
    val key = "key1"
    val text = "Test1\u000CTest2"

    `when`(localizationService!!.getText(null, key)).thenReturn(text)

    sut.createChatMessage(key, MessageBody(text))

    assertThat(sut.getMessage("key1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.0")?.body?.text).isEqualTo("Test1")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.0"))).isEqualTo("Test1")

    assertThat(sut.getMessage("key1.1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.2")?.body?.text).isEqualTo("Test2")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.2"))).isEqualTo("Test2")
  }

  @Test
  fun conversationMessageSpitAndConversationUtils_whitTwoSplit() {
    val key = "key1"
    val text = "Test1\u000CTest2\u000CTest3"

    `when`(localizationService!!.getText(null, key)).thenReturn(text)

    sut.createChatMessage(key, MessageBody(text))

    assertThat(sut.getMessage("key1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.0")?.body?.text).isEqualTo("Test1")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.0"))).isEqualTo("Test1")

    assertThat(sut.getMessage("key1.1")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.1"))).isEqualTo("")

    assertThat(sut.getMessage("key1.2")?.body?.text).isEqualTo("Test2")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.2"))).isEqualTo("Test2")

    assertThat(sut.getMessage("key1.3")?.body?.text).isEqualTo("")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.3"))).isEqualTo("")

    assertThat(sut.getMessage("key1.4")?.body?.text).isEqualTo("Test3")
    assertThat(ConversationUtils.getSplitFromIndex(text, ConversationUtils.getSplitIndexFromText("key1.4"))).isEqualTo("Test3")
  }

  fun makeMessage(id: String, body: MessageBody): Message {
    val m = Message()
    m.id = id
    m.body = body
    return m
  }


  fun makeConversation(constructor: Conversation.(Unit) -> Unit): Conversation {
    return object : Conversation(eventPublisher!!, localizationService!!, uc) {
      override fun getSelectItemsForAnswer(): List<SelectItem> {
        return listOf()
      }

      override fun canAcceptAnswerToQuestion(): Boolean {
        return false
      }

      override fun handleMessage(m: Message) {

      }

      override fun init() {
      }

      override fun init(startMessage: String) {
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
