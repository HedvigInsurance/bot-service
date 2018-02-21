package com.hedvig.botService.chat;

import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.*;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.session.triggerService.TriggerService;

import java.util.ArrayList;
import java.util.UUID;

public class TrustlyConversation extends Conversation {

    public static final String START = "trustly.start";
    private final TriggerService triggerService;
    private final ConversationFactory factory;

    public TrustlyConversation(TriggerService triggerService,
                               ConversationFactory factory) {
        super("conversation.trustly");
        this.triggerService = triggerService;
        this.factory = factory;

        createMessage(START,
                new MessageBodySingleSelect("Nu ska vi bara välja ett autogirokonto!",
                        new ArrayList<SelectItem>(){{
                            add(new SelectItemTrustly("Ja välj trustlykonto", "trustly.choose.account"));
                        }}
                ));

        createMessage("trustly.poll",
                new MessageBodySingleSelect("Väntar på svar ifrån trustly.",
                        new ArrayList<SelectItem>(){{
                            add(new SelectOption("Kolla om något hänt", "trustly.poll"));
                        }}));
    }

    @Override
    public void recieveMessage(UserContext userContext, MemberChat memberChat, Message m) {

        switch (m.id) {
            case START:
                endConversation(userContext);
        }

    }

    private void endConversation(UserContext userContext) {
        userContext.completeConversation(this.getClass().toString());

        userContext.startConversation(factory.createConversation(CharityConversation.class));
    }

    @Override
    public void init(UserContext userContext) {
        addToChat(START, userContext);
    }

    @Override
    public void init(UserContext userContext, String startMessage) {
        addToChat(startMessage, userContext);
    }

    @Override
    void addToChat(Message m, UserContext userContext) {
        if(m.id.equals(START)) {
            final UserData userData = userContext.getOnBoardingData();
            UUID triggerUUID = triggerService.createDirectDebitMandate(
                    userData.getSSN(),
                    userData.getFirstName(),
                    userData.getFamilyName(),
                    userData.getEmail(),
                    userContext.getMemberId()
                    );

            userContext.putUserData(UserContext.TRUSTLY_TRIGGER_ID, triggerUUID.toString());
        }

        super.addToChat(m, userContext);
    }
}
