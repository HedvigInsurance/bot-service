package com.hedvig.botService.chat

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.LocalizationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import java.util.*


class MemberSourceConversation(
    eventPublisher: ApplicationEventPublisher,
    localizationService: LocalizationService,
    userContext: UserContext
) : Conversation(eventPublisher, localizationService, userContext) {
    override fun getSelectItemsForAnswer(): List<SelectItem> {
        return listOf()
    }

    override fun canAcceptAnswerToQuestion(): Boolean {
        return false
    }

    override fun handleMessage(m: Message) {

    }

    override fun init() {
        init("membersource.poll")
    }

    override fun init(startMessage: String) {
        startConversation(startMessage)
    }


    override fun receiveEvent(e: EventTypes, value: String) {
        if (e == Conversation.EventTypes.MESSAGE_FETCHED
        ) {
            val relay = getRelay(value)
            if (relay != null) {
                completeRequest(relay)
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
                addToChat(m)

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
                addToChat(m)
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
