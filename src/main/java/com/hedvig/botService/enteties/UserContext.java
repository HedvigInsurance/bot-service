package com.hedvig.botService.enteties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.hedvig.botService.enteties.userContextHelpers.AutogiroData;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.session.SessionManager;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedvig.botService.chat.Conversation;
import com.hedvig.botService.chat.OnboardingConversationDevi;

import lombok.Getter;

/*
 * Contains all state information related to a member
 * */
@Entity
public class UserContext implements Serializable {
	
	private static Logger log = LoggerFactory.getLogger(UserContext.class);
	private static HashMap<String, String> requiredData = new HashMap<String, String>(){{
		put("{ADDRESS}","T.ex har jag vet jag inte var du bor. Vad har du för gatuadress?");
		put("{ADDRESS_ZIP}", "T.ex har jag inte ditt postnummer?");
		//put("{EMAIL}"); Email is not required to get a quote
		put("{FAMILY_NAME}","T.ex vet jag inte vad heter i efternamn... " + OnboardingConversationDevi.emoji_flushed_face + " ?");
		put("{HOUSE}", "T.ex vet jag inte om du bor i hus eller lägenhet?");
		put("{KVM}","T.ex vet jag inte hur stor din bostad är?");
		put("{SSN}", "T.ex har jag inte ditt personnummer?");
		put("{NAME}", "T.ex vet jag inte vad heter... " + OnboardingConversationDevi.emoji_flushed_face + " ?");
		put("{NR_PERSONS}", "Tex. hur många är ni i hushållet");
		put("{SECURE_ITEMS_NO}", "T.ex skulle jag behöver veta hur många säkerhetsgrejer du har?"); // TODO: Redirect...
		}};
			
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Getter
    private String memberId;
    
    @ElementCollection
    @CollectionTable(name="user_data")
    @MapKeyColumn(name="key")
    @Column(name="value", length = 3000)
    private Map<String, String> userData = new HashMap<String, String>();

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="conversationManager_id")
    private ConversationManager conversationManager;

	@OneToOne(cascade = CascadeType.ALL)
	@Getter
	@Setter
	private MemberChat memberChat;

	@Getter
	@Setter
	@OneToMany(mappedBy = "userContext", cascade = CascadeType.ALL)
	@MapKey(name="referenceToken")
	private Map<String, CollectionStatus> bankIdStatus;
    
    /*
     * Lookup if there is a value for the key in the user context
     * */
    public String getDataEntry(String key){
    	return userData.get(key);
    }

    public void putUserData(String key, String value){
    	log.info("Adding ("+key+":"+value+") to user context:" + this.getMemberId());
    	userData.put(key, value);
    }

    public AutogiroData getAutogiroData(){
        return new AutogiroData(this);
    };
    
    public List<ConversationEntity> getConversations(){
    	return this.conversationManager.getConversations();
    }
    
    public void addConversation(ConversationEntity c){
    	c.setConversationManager(this.conversationManager);
    	this.conversationManager.add(c);
    }
    
    /*
     * Check if user has an ongoing conversation of type conversationClassName
     * */
    public boolean hasOngoingConversation(String conversationClassName){
    	if(conversationClassName.indexOf(".")==-1)conversationClassName = ("com.hedvig.botService.chat." + conversationClassName); // TODO: Refactor/remove hack
    	String c =  getDataEntry("{" +conversationClassName+ "}") ;
    	return(c!=null && c.equals(Conversation.conversationStatus.ONGOING.toString()));
    }

    public void startOngoingConversation(String conversationClassName){
        if(conversationClassName.indexOf(".")==-1)conversationClassName = ("com.hedvig.botService.chat." + conversationClassName); // TODO: Refactor/remove hack
        this.putUserData("{" +conversationClassName+ "}", Conversation.conversationStatus.ONGOING.toString());
    }
    
    /*
     * Set conversation to COMPLETE
     * */
    public void completeConversation(String conversationClassName){
    	if(conversationClassName.indexOf(".")==-1)conversationClassName = ("com.hedvig.botService.chat." + conversationClassName); // TODO: Refactor/remove hack
    	putUserData("{" +conversationClassName+ "}", Conversation.conversationStatus.COMPLETE.toString());
    }
    
    public UserContext(String memberId) {
    	log.info("Instantiating UserContext for member:" + memberId + " :" + this );
        this.memberId = memberId;
        this.conversationManager = new ConversationManager(memberId);
    }
    
    public UserContext() {
    	log.info("Instantiating empty UserContext " + this );
    }
    public void clearContext(){
    	this.userData.clear();
    }

    public void mockMe(){
    	putUserData("{ADDRESS}", "Margaretavägen 8D");
    	putUserData("{ADDRESS_ZIP}","18774");
    	putUserData("{AUTOSTART_TOKEN}","cbead301-1796-401e-bec3-26d67fd7bc9e");
    	putUserData("{com.hedvig.botService.chat.OnboardingConversationDevi}","ONGOING");
    	putUserData("{EMAIL}","johan@hedvi.com");
    	putUserData("{FAMILY_NAME}","Tjelldén");
    	putUserData("{HOUSE}","bostdsrätt");
    	putUserData("{INSURANCE_COMPANY_TODAY}","trygg-hansa");
    	putUserData("{KVM}","123");
    	putUserData("{MEMBER_BIRTH_DATE}","1984-09-18");
    	putUserData("{NAME}","Johan");
    	putUserData("{NR_PERSONS}","3");
    	putUserData("{REFERENCE_TOKEN}","a903a93f-a53a-4b4a-93a4-9cf4a4c9f15e");
    	putUserData("{SECURE_ITEM_0}","safety.extinguisher");
    	putUserData("{SECURE_ITEM_1}","safety.door");
    	putUserData("{SECURE_ITEMS_NO}","2");
    	putUserData("{SSN}", "190101013443");

    }
    
    /*
     * Validate that all required information is collected during onboarding and if not enable Hedvig to ask for it
     * */
    public String getMissingDataItem(){
    	for(String s : requiredData.keySet()){
    		if(!userData.containsKey(s)){
    			return requiredData.get(s);
    		}
    	}
    	return null;
    }
    
    public UserData getOnBoardingData() {
        return new UserData(this);
    }

	public CollectionStatus getBankIdCollectStatus(String referenceToken) {
		CollectionStatus collectionStatus = bankIdStatus.get(referenceToken);
		return collectionStatus;
	}

	public void startBankIdAuth(BankIdAuthResponse bankIdAuthResponse) {
		CollectionStatus collectionStatus = new CollectionStatus();
		collectionStatus.setCollectionType(CollectionStatus.CollectionType.AUTH);
		collectionStatus.setLastStatus(bankIdAuthResponse.getBankIdStatus().name());
		String referenceToken = bankIdAuthResponse.getReferenceToken();
		collectionStatus.setReferenceToken(referenceToken);
		collectionStatus.setAutoStartToken(bankIdAuthResponse.getAutoStartToken());

		collectionStatus.setUserContext(this);
		this.bankIdStatus.put(referenceToken, collectionStatus);

		this.putUserData("{AUTOSTART_TOKEN}", bankIdAuthResponse.getAutoStartToken());
		this.putUserData("{REFERENCE_TOKEN}", bankIdAuthResponse.getReferenceToken());
	}
}


