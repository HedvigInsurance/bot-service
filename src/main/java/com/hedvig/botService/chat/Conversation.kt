package com.hedvig.botService.chat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.botService.utils.MessageUtil
import com.hedvig.botService.dataTypes.HedvigDataType
import com.hedvig.botService.dataTypes.TextInput
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.events.MessageSentEvent
import com.hedvig.localization.service.LocalizationService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.jwt.JwtHelper
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.Long.valueOf
import java.util.*


typealias SelectItemMessageCallback = (MessageBodySingleSelect, UserContext) -> String
typealias GenericMessageCallback = (Message, UserContext) -> String
typealias AddMessageCallback = (UserContext) -> Unit

@Component
abstract class Conversation(
  open var eventPublisher: ApplicationEventPublisher,
  val localizationService: LocalizationService,
  userContext: UserContext
) {

  private val callbacks = TreeMap<String, SelectItemMessageCallback>()
  val genericCallbacks = TreeMap<String, GenericMessageCallback>()
  val addMessageCallbacks = TreeMap<String, AddMessageCallback>()

  private val messageList = TreeMap<String, Message>()
  private val relayList = TreeMap<String, String>()

  open val userContext: UserContext = userContext

  enum class conversationStatus {
    INITIATED,
    ONGOING,
    COMPLETE
  }

  enum class EventTypes {
    MESSAGE_FETCHED
  }

  fun getMessage(key: String): Message? {
    val m = messageList[key]
    if (m == null) log.error("Message not found with id: $key")
    return m
  }

  protected fun addRelayToChatMessage(s1: String, s2: String) {
    val i = findLastChatMessageId(s1)

    relayList[i] = s2
  }

  protected fun addRelay(s1: String, s2: String) {

    relayList[s1] = s2
  }

  fun findLastChatMessageId(messageId: String): String {
    var i = 0
    while (messageList.containsKey(String.format(CHAT_ID_FORMAT, messageId, i))) {
      i++

      if (i == 100) {
        val format = String.format("Found 100 ChatMessages messages for %s, this seems strange", messageId)
        throw RuntimeException(format)
      }
    }

    return if (i > 0) {
      String.format(CHAT_ID_FORMAT, messageId, i - 1)
    } else messageId

  }

  protected fun getRelay(s1: String): String? {
    return relayList[s1]
  }

  fun addToChat(messageId: String) {
    addToChat(getMessage(messageId))
  }

  abstract fun getSelectItemsForAnswer(): List<SelectItem>

  abstract fun canAcceptAnswerToQuestion(): Boolean

  public open fun addToChat(m: Message?) {
    m!!.render(userContext, localizationService)
    log.info("Putting message: " + m.id + " content: " + m.body.text)
    userContext.addToHistory(m)
    addMessageCallbacks[m.id]?.invoke(userContext)
    if (eventPublisher != null) {
      eventPublisher.publishEvent(MessageSentEvent(userContext.memberId, m))
    }
  }

  fun createMessage(id:String, header: MessageHeader, body: MessageBody){
    this.createMessage(id, header,body, null)
  }

  fun createMessage(id:String, body: MessageBody){
    this.createMessage(id, body = body, avatarName = null)
  }

  fun createMessage(id:String, body:MessageBody, delay: Int){
    this.createMessage(id, body = body, avatarName = null, delay = delay)
  }

  fun createMessage(
    id: String,
    header: MessageHeader = MessageHeader(MessageHeader.HEDVIG_USER_ID, -1),
    body: MessageBody,
    avatarName: String? = null,
    delay: Int? = null) {
    val m = Message()
    m.id = id

    m.header = header
    m.body = body
    if (delay != null) {
      m.header.pollingInterval = valueOf(delay.toLong())
    }
    if(avatarName != null){
      m.header.avatarName = avatarName
    }
    messageList[m.id] = m
  }

  internal fun hasSelectItemCallback(messageId: String): Boolean {
    return this.callbacks.containsKey(messageId)
  }

  internal fun execSelectItemCallback(messageId: String, message: MessageBodySingleSelect): String {
    return this.callbacks[messageId]!!.invoke(message, userContext)
  }


  protected fun startConversation(startId: String) {
    log.info("Starting conversation with message: $startId")
    addToChat(messageList[startId])
  }

  fun setExpectedReturnType(messageId: String, type: HedvigDataType) {
    if (getMessage(messageId) != null) {
      log.debug(
        "Setting the expected return typ for message: "
          + messageId
          + " to "
          + type.javaClass.name)
      getMessage(findLastChatMessageId(messageId))!!.expectedType = type
    } else {
      log.error("ERROR: ------------> Message not found: $messageId")
    }
  }

  // If the message has a preferred return type it is validated otherwise not
  fun validateReturnType(m: Message): Boolean {

    val mCorr = getMessage(MessageUtil.removeNotValidFromId(m.id))

    if (mCorr != null) {
      var ok = true
      // All text input are validated to prevent null pointer exceptions
      if (mCorr.body.javaClass == MessageBodyText::class.java) {
        val t = TextInput()
        ok = t.validate(m.body.text)
        if (!ok) mCorr.body.text = t.getErrorMessage()
      }
      // Input with explicit validation
      if (mCorr.expectedType != null) {
        ok = mCorr.expectedType.validate(m.body.text)
        if (!ok) {
          mCorr.id += NOT_VALID_POST_FIX
          val localizedErrorMessage =
            localizationService.getText(userContext.locale, mCorr.expectedType.errorMessageId)
              ?: mCorr.expectedType.getErrorMessage()

          mCorr.body.text = localizedErrorMessage.replace("{INPUT}", m.body.text)

          m.id = mCorr.expectedType.errorMessageId + ".input" + NOT_VALID_POST_FIX
        }
      }
      if (m.body.text == null) {
        m.body.text = ""
      }

      if (!ok) {
        addToChat(m)
        addToChat(mCorr)
      }
      return ok
    }
    return true
  }

  // ------------------------------------------------------------------------------- //


  fun receiveMessage(m: Message) {
    var nxtMsg:String? = null

    if(validateReturnType(m)) {
      //Generic Lambda
      if (this.hasGenericCallback(m.strippedBaseMessageId)) {
        nxtMsg = this.execGenericCallback(m)
      }

      if (nxtMsg != null) {
        this.completeRequest(nxtMsg)
      } else {
        handleMessage(m)
      }
    }
  }

  abstract fun handleMessage(m: Message)

  protected open fun completeRequest(nxtMsg: String) {
    if (getMessage(nxtMsg) != null) {
      addToChat(getMessage(nxtMsg))
    }
  }


  open fun receiveEvent(e: EventTypes, value: String) {}

  abstract fun init()

  abstract fun init(startMessage: String)

  // ----------------------------------------------------------------------------------------------------------------- //

  final inline fun <reified T:MessageBody>createChatMessage(id:String, message:WrappedMessage<T>){
    this.createChatMessage(id, avatar = null, body = message.message)
    this.genericCallbacks[id] = {m,u -> message.receiveMessageCallback(m.body as T, u, m)}
    if(message.addMessageCallback != null) {
      this.addMessageCallbacks[id] = message.addMessageCallback
    }

  }

  fun createChatMessage(id: String, body: MessageBody) {
    this.createChatMessage(id, body, null)
  }

  /*
 * Splits the message text into separate messages based on \f and adds 'Hedvig is thinking' messages in between
 * */
  fun createChatMessage(id: String, body: MessageBody, avatar: String?) {
    val text = localizationService.getText(userContext.locale, id) ?: body.text
    val paragraphs = text.split("\u000C".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    var pId = 0
    val msgs = ArrayList<String>()

    for (i in 0 until paragraphs.size - 1) {
      val s = paragraphs[i]
      val s1 = if (i == 0) id else String.format(CHAT_ID_FORMAT, id, pId++)
      val s2 = String.format(CHAT_ID_FORMAT, id, pId++)

      createMessage(s2, body = MessageBodyParagraph(s))

      // if(i==0){
      //	createMessage(s1, new MessageBodyParagraph(""),"h_symbol",(s.length()*delayFactor));
      // }else{
      createMessage(s1, body = MessageBodyParagraph(""), delay = minOf(s.length * MESSAGE_DELAY_FACTOR_MS, MESSAGE_MAX_DELAY_MS))
      // }
      msgs.add(s1)
      msgs.add(s2)
    }

    // The 'actual' message
    val sWrite = if (pId == 0) id else String.format(CHAT_ID_FORMAT, id, pId++)
    val sFinal = String.format(CHAT_ID_FORMAT, id, pId++)
    val s = paragraphs[paragraphs.size - 1] // Last paragraph is put on actual message
    body.text = s
    // createMessage(sWrite, new MessageBodyParagraph(""), "h_symbol",(s.length()*delayFactor));
    createMessage(sWrite, body = MessageBodyParagraph(""), delay =  minOf(s.length * MESSAGE_DELAY_FACTOR_MS, MESSAGE_MAX_DELAY_MS))
    if (avatar != null) {
      createMessage(sFinal, body = body, avatarName = avatar)
    } else {
      createMessage(sFinal, body = body)
    }
    msgs.add(sWrite)
    msgs.add(sFinal)

    // Connect all messages in relay chain
    for (i in 0 until msgs.size - 1) addRelay(msgs[i], msgs[i + 1])
  }

  @JvmOverloads
  fun addMessageFromBackOffice(message: String, messageId: String, userId: String? = null): Boolean {
    if (!this.canAcceptAnswerToQuestion()) {
      return false
    }

    val msg = createBackOfficeMessage(message, messageId)
    msg.author = getUserId(userId)

    userContext.memberChat.addToHistory(msg)


    if (eventPublisher != null) {
      eventPublisher.publishEvent(MessageSentEvent(userContext.memberId, msg))
    }

    return true
  }


  open fun createBackOfficeMessage(message: String, id: String): Message {
    val msg = Message()
    val selectionItems = getSelectItemsForAnswer()
    msg.body = MessageBodySingleSelect(message, selectionItems)
    msg.globalId = null
    msg.header = MessageHeader.createRichTextHeader()
    msg.header.messageId = null
    msg.body.id = null
    msg.id = id

    return msg
  }

  private fun getUserId(token: String?): String? {
    return try {
      val map = ObjectMapper().readValue<Map<String, String>>(JwtHelper.decode(token!!).claims, object : TypeReference<Map<String, String>>() {

      })
      map["email"]
    } catch (e: IOException) {
      log.error(e.message)
      ""
    } catch (e: RuntimeException) {
      log.error(e.message)
      ""
    }

  }

  fun hasGenericCallback(id: String): Boolean {
    return genericCallbacks.containsKey(id)
  }

  fun execGenericCallback(m: Message): String {
    return this.genericCallbacks[m.strippedBaseMessageId]!!.invoke(m, userContext)
  }

  companion object {
    private val CHAT_ID_FORMAT = "%s.%s"

    const val NOT_VALID_POST_FIX = ".not.valid"

    private val MESSAGE_DELAY_FACTOR_MS = 15
    private val MESSAGE_MAX_DELAY_MS = 1000

    private val log = LoggerFactory.getLogger(Conversation::class.java)
  }
}
