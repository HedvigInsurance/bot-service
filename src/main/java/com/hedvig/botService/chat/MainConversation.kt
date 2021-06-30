package com.hedvig.botService.chat

import com.google.common.collect.Lists
import com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_FROM_CLAIM
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.events.QuestionAskedEvent
import com.hedvig.botService.services.events.RequestPhoneCallEvent
import com.hedvig.libs.translations.Translations
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher

class MainConversation @Autowired
constructor(
    private val conversationFactory: ConversationFactory,
    eventPublisher: ApplicationEventPublisher,
    translations: Translations,
    userContext: UserContext
) : Conversation(eventPublisher, translations, userContext) {

    init {

        this.createMessage(
            MESSAGE_HEDVIG_COM,
            MessageBodySingleSelect(
                "Hej {NAME}, vad vill du göra idag?",
                Lists.newArrayList<SelectItem>(
                    SelectOption("Rapportera en skada", MESSAGE_MAIN_REPORT),
                    SelectOption("Ring mig!", MESSAGE_MAIN_CALLME),
                    SelectOption("Jag har en fråga", MESSAGE_MAIN_QUESTION)
                )
            )
        )

        this.createMessage(
            MESSAGE_HEDVIG_COM_POST_LOGIN,
            MessageBodySingleSelect(
                "Välkommen tillbaka {NAME}!",
                Lists.newArrayList<SelectItem>(
                    SelectLink.toDashboard("Ta mig till till hemskärmen", "postlogindash")
                )
            )
        )

        val HANDS_EMOJI = "🙌"
        this.createChatMessage(
            MESSAGE_COMPLETE_CLAIM,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Jag återkommer här i chatten om jag behöver något mer eller för att berätta hur det går $HANDS_EMOJI",
//                    SelectLink.closeChat("Okej!", MESSAGE_CLAIM_DONE), // TODO: add this option when iOS can handle closeChat "link"
                    SelectOption("Jag har en fråga", MESSAGE_MAIN_START_FREE_TEXT_CHAT)
                )
            ) { body, uc, _ ->
                startFreeTextChatConversation(uc)
                body.selectedItem.value
            }
        )

        this.createMessage(
            MESSAGE_QUESTION_RECIEVED,
            MessageBodySingleSelect(
                "Tack {NAME}, jag återkommer så snart jag kan med svar på din fråga",
                Lists.newArrayList(
                    SelectOption("Jag har en till fråga", MESSAGE_MAIN_QUESTION),
                    SelectLink.toDashboard("Hem", MESSAGE_HEDVIG_COM)
                )
            )
        )

        this.createMessage(
            MESSAGE_MAIN_END,
            MessageBodySingleSelect(
                "Tack. Jag ringer upp dig så snart jag kan",
                Lists.newArrayList<SelectItem>(SelectLink.toDashboard("Hem", MESSAGE_MAIN_ONBOARDING_DONE))
            )
        )

        this.createMessage(MESSAGE_MAIN_CALLME, MessageBodyNumber("Vad når jag dig på för nummer?"))

        this.createMessage(
            MESSAGE_MAIN_QUESTION,
            MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
            MessageBodyText("Självklart, vad kan jag hjälpa dig med?")
        )

        this.createMessage(MESSAGE_ERROR, MessageBodyText("Oj nu blev något fel..."))
    }

    override fun receiveEvent(e: Conversation.EventTypes, value: String) {
        when (e) {
            // This is used to let Hedvig say multiple message after another
            Conversation.EventTypes.MESSAGE_FETCHED -> {
                // OnboardingConversationDevi.log.info("Message fetched: $value")

                // New way of handeling relay messages
                val relay = getRelay(value)
                if (relay != null) {
                    completeRequest(relay)
                }
            }
        }
    }

    override fun handleMessage(m: Message) {

        var nxtMsg = ""

        if (!validateReturnType(m)) {
            return
        }

        when (m.strippedBaseMessageId) {
            MESSAGE_COMPLETE_CLAIM, MESSAGE_HEDVIG_COM -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem
                m.body.text = item.text
                if (item.value == MESSAGE_MAIN_REPORT) {
                    nxtMsg = CONVERSATION_DONE
                } else if (item.value == MESSAGE_MAIN_START_TRUSTLY) {
                    addToChat(m)
                    userContext.completeConversation(this) // TODO: End conversation in better way
                    userContext.startConversation(
                        conversationFactory.createConversation(TrustlyConversation::class.java, userContext)
                    )
                    userContext.putUserData(UserContext.FORCE_TRUSTLY_CHOICE, "false")
                    return
                }

                addToChat(m) // Response parsed to nice format
            }
            MESSAGE_MAIN_CALLME -> {
                userContext.putUserData("{PHONE_" + LocalDate().toString() + "}", m.body.text)
                eventPublisher.publishEvent(
                    RequestPhoneCallEvent(
                        userContext.memberId,
                        m.body.text,
                        userContext.onBoardingData.firstName,
                        userContext.onBoardingData.familyName
                    )
                )
                nxtMsg = MESSAGE_MAIN_END
                addToChat(m) // Response parsed to nice format
                userContext.completeConversation(this) // TODO: End conversation in better way
            }
            MESSAGE_MAIN_QUESTION -> nxtMsg = handleQuestion(m)
        }

        val handledNxtMsg = handleSingleSelect(m, nxtMsg, listOf(CONVERSATION_DONE))

        completeRequest(handledNxtMsg)
    }

    private fun startFreeTextChatConversation(uc: UserContext) {
        val conversation = conversationFactory.createConversation(FreeChatConversation::class.java, uc)
        uc.startConversation(conversation, FREE_CHAT_FROM_CLAIM)
    }

    fun handleQuestion(m: Message): String {
        val question = m.body.text
        userContext.putUserData("{QUESTION_" + LocalDate().toString() + "}", question)
        addToChat(m) // Response parsed to nice format
        eventPublisher.publishEvent(QuestionAskedEvent(userContext.memberId, question))
        return MESSAGE_QUESTION_RECIEVED
    }

    /*
   * Generate next chat message or ends conversation
   * */
    public override fun completeRequest(nxtMsg: String) {
        var outNxtMsg = nxtMsg

        when (outNxtMsg) {
            CONVERSATION_DONE -> {
                log.info("conversation complete")
                userContext.completeConversation(this)
                userContext.startConversation(
                    conversationFactory.createConversation(ClaimsConversation::class.java, userContext)
                )

                return
            }
            "$MESSAGE_HEDVIG_COM.4" -> {
                addTrustlyButton(userContext)
                log.error("I dont know where to go next...")
                outNxtMsg = "error"
            }
            "" -> {
                log.error("I dont know where to go next...")
                outNxtMsg = "error"
            }
        }

        super.completeRequest(outNxtMsg)
    }

    fun addTrustlyButton(userContext: UserContext) {
        val message = getMessage(MESSAGE_HEDVIG_COM)
        val body = message!!.body as MessageBodySingleSelect
        val forceTrustly = userContext.getDataEntry(UserContext.FORCE_TRUSTLY_CHOICE)
        if ("true".equals(forceTrustly, ignoreCase = true)) {
            body.choices.add(SelectOption("Koppla autogiro", MESSAGE_MAIN_START_TRUSTLY))
        }
    }

    override fun getSelectItemsForAnswer(): List<SelectItem> {
        return Lists.newArrayList<SelectItem>(
            SelectOption("Svara Hedvig", MESSAGE_MAIN_QUESTION),
            SelectOption("Tack, det var vad jag behövde veta", MESSAGE_HEDVIG_COM)
        )
    }

    override fun canAcceptAnswerToQuestion(): Boolean {
        return true
    }

    override fun init() {
        log.info("Starting main conversation")
        addTrustlyButton(userContext)
        startConversation(MESSAGE_HEDVIG_COM) // Id of first message
    }

    override fun init(startMessage: String) {
        log.info("Starting main conversation with message: $startMessage")
        addTrustlyButton(userContext)
        startConversation(startMessage) // Id of first message
    }

    companion object {

        const val MESSAGE_HEDVIG_COM = "hedvig.com"
        const val MESSAGE_HEDVIG_COM_POST_LOGIN = "hedvig.com.post.login"
        const val MESSAGE_QUESTION_RECIEVED = "message.question.recieved"
        const val MESSAGE_MAIN_END = "message.main.end"
        const val MESSAGE_MAIN_CALLME = "message.main.callme"
        const val MESSAGE_MAIN_QUESTION = "main.question"
        const val MESSAGE_MAIN_START_TRUSTLY = "message.main.start_trustly"
        const val MESSAGE_ERROR = "error"
        const val MESSAGE_MAIN_REPORT = "message.main.report"
        const val MESSAGE_MAIN_ONBOARDING_DONE = "onboarding.done"
        const val CONVERSATION_DONE = "conversation.done"
        const val MESSAGE_COMPLETE_CLAIM = "hedvig.complete.claim"
        const val MESSAGE_CLAIM_DONE = "claim.done"
        const val MESSAGE_MAIN_START_CHAT = "message.main.start.chat"
        const val MESSAGE_MAIN_START_FREE_TEXT_CHAT = "message.main.start.free.text.chat"

        private val log = LoggerFactory.getLogger(MainConversation::class.java)
    }
}
