package com.hedvig.botService.chat

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class HouseOnboardingConversation
    constructor(
        override var eventPublisher: ApplicationEventPublisher,
        private val conversationFactory: ConversationFactory
    ) : Conversation(eventPublisher) {

    var queuePos: Int? = null

    init {

        this.createChatMessage(
            "message.hus.first",
            MessageBodyText(
                "Hej! Jag test message from Hedvig 👋"
            )

        )
        this.addRelay(MESSAGE_HUS_FIRST, MESSAGE_HUS_SECOND)

        this.createChatMessage(
            "message.hus.second",
            MessageBodyText(
                "Hej! Jag test message from Hedvig 2 👋"
            )
        )
        this.addRelay(MESSAGE_HUS_SECOND, MESSAGE_HUS_THIRD)

        this.createChatMessage(
            MESSAGE_HUS_THIRD,
            WrappedMessage(
                MessageBodyText(
                    "What is your last name?",
                    TextContentType.FAMILY_NAME,
                    KeyboardType.DEFAULT,
                    "Family name"
                )
            )
            { body, userContext, message ->
                userContext.onBoardingData.familyName = body.text
                if(userContext.onBoardingData.familyName == "Test") {
                    MESSAGE_HUS_FOURTH
                } else {
                    MESSAGE_HUS_FIRST
                }
            }
        )

        this.createChatMessage(
            MESSAGE_HUS_FOURTH,
            MessageBodyParagraph(
                "Hej! {FAMILY_NAME}"
            )
        )
        this.addRelay(MESSAGE_HUS_FOURTH, CONVERSATION_DONE)
    }

    public override fun completeRequest(nxtMsg: String, userContext: UserContext) {
        var nxtMsg = nxtMsg

        when (nxtMsg) {
              CONVERSATION_DONE -> {
                  userContext.completeConversation(this)
                  val conversation = conversationFactory.createConversation(OnboardingConversationDevi::class.java)
                  userContext.startConversation(conversation, OnboardingConversationDevi.MESSAGE_50K_LIMIT)
              }

            "" -> {
                HouseOnboardingConversation.log.error("I dont know where to go next...")
                nxtMsg = "error"
            }
        }
        super.completeRequest(nxtMsg, userContext)
    }

    override fun init(userContext: UserContext) {
        HouseOnboardingConversation.log.info("Starting house conversation")
        startConversation(userContext, HouseOnboardingConversation.MESSAGE_HUS_FIRST)
    }


    override fun init(userContext: UserContext, startMessage: String) {
        log.info("Starting house onboarding conversation with message: $startMessage")
        startConversation(userContext, startMessage) // Id of first message
    }

    override fun handleMessage(userContext: UserContext, m: Message) {
        var nxtMsg = ""

        if (!validateReturnType(m, userContext)) {
            return
        }

        // Lambda
        if (this.hasSelectItemCallback(m.id) && m.body.javaClass == MessageBodySingleSelect::class.java) {
            // MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
            nxtMsg = this.execSelectItemCallback(m.id, m.body as MessageBodySingleSelect, userContext)
            addToChat(m, userContext)
        }

        val onBoardingData = userContext.onBoardingData

        // ... and then the incoming message id

        nxtMsg = m.id
        addToChat(m, userContext)

        completeRequest(nxtMsg, userContext)
    }

    override fun receiveEvent(e: Conversation.EventTypes, value: String, userContext: UserContext) {
        when (e) {
            // This is used to let Hedvig say multiple message after another
            Conversation.EventTypes.MESSAGE_FETCHED -> {
                log.info("Message fetched: $value")

                // New way of handeling relay messages
                val relay = getRelay(value)

                if (relay != null) {
                    completeRequest(relay, userContext)
                }

            }
        }
    }

    override fun canAcceptAnswerToQuestion(uc: UserContext): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSelectItemsForAnswer(uc: UserContext): List<SelectItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {

        const val MESSAGE_HUS_FIRST = "message.hus.first"
        const val MESSAGE_HUS_SECOND = "message.hus.second"
        const val MESSAGE_HUS_THIRD = "message.hus.third"
        const val MESSAGE_HUS_FOURTH = "message.hus.fourth"
            const val CONVERSATION_DONE = "conversation.done"

        private val log = LoggerFactory.getLogger(HouseOnboardingConversation::class.java)
    }

}
