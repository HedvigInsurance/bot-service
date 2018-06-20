package com.hedvig.botService.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.botService.chat.*;
import com.hedvig.botService.chat.Conversation.EventTypes;
import com.hedvig.botService.enteties.*;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.web.dto.AddMessageRequestDTO;
import com.hedvig.botService.web.dto.BackOfficeAnswerDTO;
import com.hedvig.botService.web.dto.SignupStatus;
import com.hedvig.botService.web.dto.TrackingDTO;
import com.hedvig.botService.web.dto.UpdateTypes;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.hedvig.botService.chat.OnboardingConversationDevi.LOGIN;
import static com.hedvig.botService.chat.OnboardingConversationDevi.MESSAGE_START_LOGIN;

/*
 * The session manager is the main controller class for the chat service. It contains all user sessions with chat histories, context etc
 * It is a singleton accessed through the request controller
 * */

@Component
@Transactional
public class SessionManager {

    public enum Intent {
        LOGIN,
        ONBOARDING
    }

    private static Logger log = LoggerFactory.getLogger(SessionManager.class);
    private final UserContextRepository userrepo;
    private final MemberService memberService;
    private final ProductPricingService productPricingclient;

    private final SignupCodeRepository signupRepo;
    private final ConversationFactory conversationFactory;
    private final TrackingDataRespository trackerRepo;
    private final ObjectMapper objectMapper;

    public enum conversationTypes {MainConversation, OnboardingConversationDevi, UpdateInformationConversation, ClaimsConversation}
    
    @Value("${hedvig.waitlist.length}")
    public Integer queuePos;
	
    @Autowired
    public SessionManager(UserContextRepository userrepo,
                          MemberService memberService,
                          ProductPricingService client,
                          SignupCodeRepository signupRepo,
                          ConversationFactory conversationFactory,
                          TrackingDataRespository trackerRepo, ObjectMapper objectMapper) {
        this.userrepo = userrepo;
        this.memberService = memberService;
        this.productPricingclient = client;
        this.signupRepo = signupRepo;
        this.conversationFactory = conversationFactory;
        this.trackerRepo = trackerRepo;
        this.objectMapper = objectMapper;
    }

    public List<Message> getMessages(int i, String hid) {
        log.info("Getting " + i + " messages for user:" + hid);
        List<Message>  messages = getAllMessages(hid, null);

        return messages.subList(Math.max(messages.size() - i, 0), messages.size());
    }

    public SignupStatus getSignupQueuePosition(String externalToken){

        ArrayList<SignupCode> scList = (ArrayList<SignupCode>) signupRepo.findAllByOrderByDateAsc();
        int pos = 1;
        SignupStatus ss = new SignupStatus();
        
        for(SignupCode sc : scList){
        		log.debug(sc.code + " UUID:" + sc.externalToken + " email:" + sc.email + "(" + sc.date+"):" + (pos));
        		if(sc.externalToken.toString().equals(externalToken)){
        			if(!sc.active){
        				ss.position = queuePos + pos;
        				ss.status = SignupStatus.states.WAITLIST.toString();
        				return ss;
        			}else{
        				ss.code = sc.code;
        				ss.status = SignupStatus.states.ACCESS.toString();
        				return ss;
        			}
        		}
        		if(!sc.used)pos++;
        }
        ss.status = SignupStatus.states.NOT_FOUND.toString();
        return ss;
    }
    
    public void savePushToken(String hid, String pushToken) {
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext for user:" + hid));
        uc.putUserData("PUSH-TOKEN", pushToken);
        userrepo.saveAndFlush(uc);
    }
    
    public void saveTrackingInformation(String hid, TrackingDTO tracker) {
        TrackingEntity cc = new TrackingEntity(hid, tracker);
        trackerRepo.saveAndFlush(cc);
    }
    
    public String getPushToken(String hid) {
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext for user:" + hid));
        return uc.getDataEntry("PUSH-TOKEN");
    }
    
    
    public void recieveEvent(String eventtype, String value, String hid){

        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        EventTypes type = EventTypes.valueOf(eventtype);


        List<ConversationEntity> conversations = new ArrayList<>(uc.getConversations()); //We will add a new element to uc.conversationManager
        for(ConversationEntity c : conversations){

            // Only deliver messages to ongoing conversations
            if(!c.getConversationStatus().equals(Conversation.conversationStatus.ONGOING))continue;

            try {
                final Class<?> conversationClass = Class.forName(c.getClassName());
                final Conversation conversation = conversationFactory.createConversation(conversationClass);
                conversation.recieveEvent(type, value, uc);

            } catch (ClassNotFoundException e) {
                log.error("Could not load conversation from db!", e);
            }
        }

        userrepo.saveAndFlush(uc);
    }

    public BankIdCollectResponse collect(String hid, String referenceToken) {

        CollectService service = new CollectService(userrepo, memberService);

        return service.collect(hid, referenceToken, (BankIdChat) conversationFactory.createConversation(OnboardingConversationDevi.class));
    }

    /*
     * Kicks off onboarding conversation with either direct login or regular signup flow
     * */
    public void startOnboarding(String hid){
        startOnboardingConversation(hid, OnboardingConversationDevi.MESSAGE_WAITLIST_START);
    }

    public void startLogin(String hid) {
        startOnboardingConversation(hid, OnboardingConversationDevi.MESSAGE_START_LOGIN);
    }

    private void startOnboardingConversation(String hid, String startMsg) {
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        initChat(startMsg, uc);

        userrepo.saveAndFlush(uc);
    }

    private void initChat(String startMsg, UserContext uc) {
        uc.putUserData("{WEB_USER}", "FALSE");

        Conversation onboardingConversation = conversationFactory.createConversation(OnboardingConversationDevi.class);
        uc.startConversation(onboardingConversation, startMsg);
    }

    /*
     * Create a new users chat and context
     * */
    public void init(String hid, String linkUri){

        UserContext uc = userrepo.findByMemberId(hid).orElseGet(() -> {
            UserContext newUserContext = new UserContext(hid);
            userrepo.save(newUserContext);
            return newUserContext;
        });

        uc.putUserData("{LINK_URI}", linkUri);


        userrepo.saveAndFlush(uc);
    }
    
    /*
     * Mark all messages (incl) last input from user deleted
     * */
    public void editHistory(String hid){
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));
    	MemberChat mc = uc.getMemberChat();
    	mc.revertLastInput();
    	userrepo.saveAndFlush(uc);
    }

    public boolean addAnswerFromHedvig(BackOfficeAnswerDTO backOfficeAnswer) {
        UserContext uc = userrepo.findByMemberId(backOfficeAnswer.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));


        Conversation conversation = getActiveConversationOrStart(uc, MainConversation.class);

        if (!conversation.canAcceptAnswerToQuestion(uc)) {
            return false;
        }

        val msg = addBackOfficeMessage(uc, conversation, backOfficeAnswer.getMsg(), "message.answer");
        uc.getMemberChat().addToHistory(msg);

        userrepo.saveAndFlush(uc);
        return true;
    }


    public boolean addMessageFromHedvig(AddMessageRequestDTO backOfficeMessage) {
        val uc = userrepo.findByMemberId(backOfficeMessage.getMemberId()).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        Conversation activeConversation = getActiveConversationOrStart(uc, MainConversation.class);
        if(activeConversation.canAcceptAnswerToQuestion(uc) == false) {
            return false;
        }

        val msg = addBackOfficeMessage(uc, activeConversation, backOfficeMessage.getMsg(), "message.bo.message");
        uc.getMemberChat().addToHistory(msg);
        return true;
    }

    private Message addBackOfficeMessage(UserContext uc, Conversation activeConversation, String message, String id) {
        Message msg = new Message();
        val selectionItems = activeConversation.getSelectItemsForAnswer(uc);
        msg.body =  new MessageBodySingleSelect(message, selectionItems);
        msg.header.fromId = Conversation.HEDVIG_USER_ID;
        msg.globalId = null;
        msg.header.messageId = null;
        msg.body.id = null;
        msg.id = id;

        return msg;
    }

    private Conversation getActiveConversationOrStart(UserContext uc, Class<MainConversation> conversationToStart) {
        return uc.getActiveConversation().
                map(x -> conversationFactory.createConversation(x.getClassName())).
                orElseGet(() -> {
                    val newConversation = conversationFactory.createConversation(conversationToStart);
                    uc.startConversation(newConversation);
                    return newConversation;
                });
    }

    /*
     * Mark all messages (incl) last input from user deleted
     * */
    public void resetOnboardingChat(String hid){
    	UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));
        MemberChat mc = uc.getMemberChat();
        
        // Conversations can only be reset during onboarding
        if(!uc.hasCompletedOnboarding()){
        	
        	String email = uc.getOnBoardingData().getEmail();
	    	mc.reset(); // Clear chat
	    	uc.clearContext(); // Clear context
	    	
	    	uc.getOnBoardingData().setEmail(email);

            Conversation onboardingConversation = conversationFactory.createConversation(OnboardingConversationDevi.class);
	        if(Objects.equals("true", uc.getDataEntry(LOGIN)) == true) {
                uc.startConversation(onboardingConversation, MESSAGE_START_LOGIN);
            }else {
                uc.startConversation(onboardingConversation);
            }

	    	userrepo.saveAndFlush(uc);
        }
    }

    public List<Message> getAllMessages(String hid, Intent intent) {

        /*
         * Find users chat and context. First time it is created
         * */

        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        MemberChat chat = uc.getMemberChat();

        if(uc.getActiveConversation().isPresent() == false) {
            if(intent == Intent.LOGIN) {
                initChat(OnboardingConversationDevi.MESSAGE_START_LOGIN, uc);
            } else {
                initChat(OnboardingConversationDevi.MESSAGE_WAITLIST_START, uc);
            }
        }

        // Mark last user input with as editAllowed
        chat.markLastInput();



        // Check for deleted messages
        ArrayList<Message> returnList = new ArrayList<Message>();
        for(Message m : chat.chatHistory){
        	if(m.deleted==null | !m.deleted){ // TODO:remove null test
        		returnList.add(m); 
        	}
        }
        
        /*
         * Sort in global Id order
         * */
    	Collections.sort(returnList, new Comparator<Message>(){
      	     public int compare(Message m1, Message m2){
      	         if(m1.globalId == m2.globalId)
      	             return 0;
      	         return m1.globalId < m2.globalId ? -1 : 1;
      	     }
      	});
    	
    	if(returnList.size() > 0){
	    	Message lastMessage = returnList.get(returnList.size() - 1);
	    	if(lastMessage!=null) {
                recieveEvent("MESSAGE_FETCHED", lastMessage.id, hid);
            }
    	}else{
    		log.info("No messages in chat....");
    	}

        userrepo.saveAndFlush(uc);

        return returnList;
    }
    
    /*
     * Add the "what do you want to do today" message to the chat
     * */
    public void mainMenu(String hid){
        log.info("Main menu from user:" + hid);
 
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        Conversation mainConversation = conversationFactory.createConversation(MainConversation.class);
        uc.startConversation(mainConversation);

        userrepo.saveAndFlush(uc);    	
    }

    public void trustlyClosed(String hid) {
        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext for user:" + hid));



        TrustlyConversation tr = (TrustlyConversation) conversationFactory.createConversation(TrustlyConversation.class);
        tr.windowClosed(uc);


        userrepo.save(uc);
    }

    public void receiveMessage(Message m, String hid) {
        log.info("Recieving messages from user:" + hid);
        try {
            log.info(objectMapper.writeValueAsString(m));
        }catch (JsonProcessingException ex) {
            log.error("Could not convert message to json in order to log: {}", m.toString());
        }

        m.header.fromId = new Long(hid);

        UserContext uc = userrepo.findByMemberId(hid).orElseThrow(() -> new ResourceNotFoundException("Could not find usercontext."));

        List<ConversationEntity> conversations = new ArrayList<>(uc.getConversations()); //We will add a new element to uc.conversationManager
        for(ConversationEntity c : conversations){
        	
        	// Only deliver messages to ongoing conversations
        	if(!c.getConversationStatus().equals(Conversation.conversationStatus.ONGOING))continue;

            try {
                final Class<?> conversationClass = Class.forName(c.getClassName());
                final Conversation conversation = conversationFactory.createConversation(conversationClass);
                conversation.receiveMessage(uc, m);

            } catch (ClassNotFoundException e) {
                log.error("Could not load conversation from db!", e);
            }
        }

        userrepo.saveAndFlush(uc);
    }

	public Integer getWaitlistPosition(String email) {
		return null;
	}
}
