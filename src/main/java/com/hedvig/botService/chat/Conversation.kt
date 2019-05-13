package com.hedvig.botService.chat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.botService.dataTypes.HedvigDataType
import com.hedvig.botService.dataTypes.TextInput
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.events.MessageSentEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.security.jwt.JwtHelper
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.Long.valueOf
import java.util.*


typealias SelectItemMessageCallback = (MessageBodySingleSelect, UserContext) -> String
typealias GenericMessageCallback = (Message, UserContext) -> String

@Component
abstract class Conversation(var eventPublisher: ApplicationEventPublisher) {

  private val callbacks = TreeMap<String, SelectItemMessageCallback>()
  val genericCallbacks = TreeMap<String, GenericMessageCallback>()

  private val messageList = TreeMap<String, Message>()
  private val relayList = TreeMap<String, String>()

  enum class conversationStatus {
    INITIATED,
    ONGOING,
    COMPLETE
  }

  enum class EventTypes {
    ANIMATION_COMPLETE,
    MODAL_CLOSED,
    MESSAGE_FETCHED,
    MISSING_DATA
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

  public fun findLastChatMessageId(messageId: String): String {
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

  fun addToChat(messageId: String, userContext: UserContext) {
    addToChat(getMessage(messageId), userContext)
  }

  abstract fun getSelectItemsForAnswer(uc: UserContext): List<SelectItem>

  abstract fun canAcceptAnswerToQuestion(uc: UserContext): Boolean

  public open fun addToChat(m: Message?, userContext: UserContext) {
    m!!.render(userContext)
    log.info("Putting message: " + m.id + " content: " + m.body.text)
    userContext.addToHistory(m)
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

  protected fun setMessageCallback(id: String, callback: SelectItemMessageCallback) {
    this.callbacks[id] = callback
  }

  internal fun hasSelectItemCallback(messageId: String): Boolean {
    return this.callbacks.containsKey(messageId)
  }

  internal fun execSelectItemCallback(messageId: String, message: MessageBodySingleSelect, uc: UserContext): String {
    return this.callbacks[messageId]!!.invoke(message, uc)
  }


  protected fun startConversation(userContext: UserContext, startId: String) {
    log.info("Starting conversation with message: $startId")
    addToChat(messageList[startId], userContext)
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
  fun validateReturnType(m: Message, userContext: UserContext): Boolean {

    val mCorr = getMessage(m.id)

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
        if (!ok) mCorr.body.text = mCorr.expectedType.getErrorMessage()
      }
      if (m.body.text == null) {
        m.body.text = ""
      }

      if (!ok) {
        addToChat(m, userContext)
        addToChat(mCorr, userContext)
      }
      return ok
    }
    return true
  }

  // ------------------------------------------------------------------------------- //


  fun receiveMessage(userContext: UserContext, m: Message) {
    var nxtMsg:String? = null

    if(validateReturnType(m, userContext)) {
      //Generic Lambda
      if (this.hasGenericCallback(m.baseMessageId)) {
        nxtMsg = this.execGenericCallback(m, userContext)
      }

      if (nxtMsg != null) {
        this.completeRequest(nxtMsg, userContext)
      } else {
        handleMessage(userContext, m)
      }
    }
  }

  abstract fun handleMessage(userContext: UserContext, m: Message)

  protected open fun completeRequest(nxtMsg: String, userContext: UserContext) {
    if (getMessage(nxtMsg) != null) {
      addToChat(getMessage(nxtMsg), userContext)
    }
  }

  open fun receiveEvent(e: EventTypes, value: String, userContext: UserContext) {}

  abstract fun init(userContext: UserContext)

  abstract fun init(userContext: UserContext, startMessage: String)

  // ----------------------------------------------------------------------------------------------------------------- //

  final inline fun <reified T:MessageBody>createChatMessage(id:String, message:WrappedMessage<T>){
    this.createChatMessage(id, avatar = null, body = message.message)
    this.genericCallbacks[id] = {m,u -> message.callback(m.body as T, u, m)}
  }

  fun createChatMessage(id: String, body: MessageBody) {
    this.createChatMessage(id, body, null)
  }

  /*
 * Splits the message text into separate messages based on \f and adds 'Hedvig is thinking' messages in between
 * */
  fun createChatMessage(id: String, body: MessageBody, avatar: String?) {
    val paragraphs = body.text.split("\u000C".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    var pId = 0
    val delayFactor = 25 // Milliseconds per character TODO: Externalize this!

    val msgs = ArrayList<String>()

    for (i in 0 until paragraphs.size - 1) {
      val s = paragraphs[i]
      val s1 = if (i == 0) id else String.format(CHAT_ID_FORMAT, id, pId++)
      val s2 = String.format(CHAT_ID_FORMAT, id, pId++)
      // log.info("Create message of size "+(s.length())+" with load time:" +
      // (s.length()*delayFactor));
      // createMessage(s1, new MessageBodyParagraph(""), "h_symbol",(s.length()*delayFactor));
      // createMessage(s1, new MessageBodyParagraph(""),(s.length()*delayFactor));
      createMessage(s2, body = MessageBodyParagraph(s))

      // if(i==0){
      //	createMessage(s1, new MessageBodyParagraph(""),"h_symbol",(s.length()*delayFactor));
      // }else{
      createMessage(s1, body = MessageBodyParagraph(""), delay = s.length * delayFactor)
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
    createMessage(sWrite, body = MessageBodyParagraph(""), delay = s.length * delayFactor)
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
  fun addMessageFromBackOffice(uc: UserContext, message: String, messageId: String, userId: String? = null): Boolean {
    if (!this.canAcceptAnswerToQuestion(uc)) {
      return false
    }

    val msg = createBackOfficeMessage(uc, message, messageId)
    msg.author = getUserId(userId)

    uc.memberChat.addToHistory(msg)

    if (eventPublisher != null) {
      eventPublisher.publishEvent(MessageSentEvent(uc.memberId, msg))
    }

    return true
  }


  open fun createBackOfficeMessage(uc: UserContext, message: String, id: String): Message {
    val msg = Message()
    val selectionItems = getSelectItemsForAnswer(uc)
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

  fun execGenericCallback(m: Message, userContext: UserContext): String {
    return this.genericCallbacks[m.baseMessageId]!!.invoke(m, userContext)
  }

  companion object {
    private val CHAT_ID_FORMAT = "%s.%s"

    private val log = LoggerFactory.getLogger(Conversation::class.java)
  }

}
