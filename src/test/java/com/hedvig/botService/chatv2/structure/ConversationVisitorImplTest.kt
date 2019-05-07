package com.hedvig.botService.chatv2.structure

import com.hedvig.botService.chatv2.claims.ClaimChatMessage
import com.hedvig.botService.chatv2.claims.FinalClamsConversationMessage
import com.hedvig.botService.chatv2.claims.StartMessage
import com.hedvig.botService.enteties.UserContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ConversationVisitorImplTest {
    @Test
    fun performsClaimsConversation() {
        val userContext = UserContext()
        val conversationVisitor = ConversationVisitorImpl(userContext, mutableListOf())

        val startMessage = StartMessage()
        startMessage.accept(conversationVisitor)

        assertThat(conversationVisitor.conversationEntries).hasSize(3)
        assertThat(conversationVisitor.conversationEntries[0].messageId).isEqualTo(StartMessage().id)
        assertThat(conversationVisitor.conversationEntries[1].messageId).isEqualTo(ClaimChatMessage().id)
        assertThat(conversationVisitor.conversationEntries[2].messageId).isEqualTo(FinalClamsConversationMessage().id)
    }
}
