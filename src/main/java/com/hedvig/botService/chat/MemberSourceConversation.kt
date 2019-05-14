package com.hedvig.botService.chat

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import org.springframework.context.ApplicationEventPublisher


class MemberSourceConversation(eventPublisher: ApplicationEventPublisher) : Conversation(eventPublisher) {
    override fun getSelectItemsForAnswer(uc: UserContext): List<SelectItem> {
        return listOf()
    }

    override fun canAcceptAnswerToQuestion(uc: UserContext): Boolean {
        return false
    }

    override fun handleMessage(userContext: UserContext, m: Message) {

    }

    override fun init(userContext: UserContext) {
        init(userContext, "membersource.poll")
    }

    override fun init(userContext: UserContext, startMessage: String) {
        startConversation(userContext, startMessage)
    }


    override fun receiveEvent(e: EventTypes, value: String, userContext: UserContext) {
        if (e == Conversation.EventTypes.MESSAGE_FETCHED
        ) {
            val relay = getRelay(value)
            if (relay != null) {
                completeRequest(relay, userContext)
            }
        }
    }

    init {
        this.createChatMessage(
            "membersource.poll",
            WrappedMessage(
                MessageBodySingleSelect(
                    "En sista fråga, hur hörde du om Hedvig? 🙂",
                    listOf(
                        SelectOption("Från en vän/familj/kollega", "friend"),
                        SelectOption("Google", "google"),
                        SelectOption("Facebook/Instagram", "facebook_instagram"),
                        SelectOption("Influencer/Podcast", "influencerPodcast"),
                        SelectOption("Nyheterna/TV", "news_tv"),
                        SelectOption("Reklam utomhus", "ad_outside"),
                        SelectOption("Annat", "other")
                    )
                )
            ) { b, u, m ->
                u.putUserData("MEMBER_SOURCE", b.selectedItem.value)
                b.text = b.selectedItem.text
                addToChat(m, u)

                val nxtMsg = when (b.selectedItem.value) {
                    "other" -> "membersource.text"
                    else -> {

                        "membersource.thanks"
                    }
                }
                nxtMsg
            }
        )

        this.createChatMessage("membersource.text",
            WrappedMessage(MessageBodyText("Var hörde du om mig? ")) { b, u, m ->
                addToChat(m, u)
                u.putUserData("MEMBER_SOURCE_TEXT", b.text.trim())
                "membersource.thanks"
            }
        )

        this.createChatMessage(
            "membersource.thanks",
            MessageBodySingleSelect("Toppen, tack!", listOf(SelectLink.toDashboard("Börja utforska appen", "expore")))
        )
    }

}
