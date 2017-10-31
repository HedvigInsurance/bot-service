package com.hedvig.botService.chat;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hedvig.botService.enteties.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Conversation {

        public static final long  HEDVIG_USER_ID = 1; // The id hedvig uses to chat
        private Map<String, SelectItemMessageCallback> callbacks = new TreeMap<>();
        public static enum conversationStatus {INITIATED, ONGOING, COMPLETE}
        public static enum EventTypes {ANIMATION_COMPLETE, MODAL_CLOSED};
	private static final String regexPattern = "\\{(.*?)\\}";
	private static Logger log = LoggerFactory.getLogger(Conversation.class);
	private String conversationName; // Id for the conversation

	MemberChat memberChat;
	UserContext userContext;
	private TreeMap<String, Message> messageList = new TreeMap<String, Message>();
	//HashMap<String, String> conversationContext = new HashMap<String, String>(); // Context specific information learned during conversation
	
	Conversation(String conversationId, MemberChat mc, UserContext uc) {
		this.conversationName = conversationId;
		this.memberChat = mc;
		this.userContext = uc;
	}

	public Message getMessage(String key){
		Message m = messageList.get(key);
		if(m==null)log.info("Message not found with id:" + key);
		return m;
	}
	
	public void storeMessage(String key, Message m){
		messageList.put(key, m);
	}
	
	public String getConversationName() {
		return conversationName;
	}
	public ConversationMessage getCurrent() {
		return current;
	}
	public void setCurrent(ConversationMessage current) {
		this.current = current;
	}
	private ConversationMessage current = null; // Last message sent to client
	
	private String replaceWithContext(String input){
		log.debug("Contextualizing string:" + input);
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher m = pattern.matcher(input);
		while (m.find()) {
			String s = m.group();
			String r = userContext.getDataEntry(s);
			log.debug(s + ":" + r);
			if(r!=null){input = input.replace(s, r);}
		}
		log.debug("-->" + input);
		return input;
	}
	
	void addToChat(Message m) {
		log.info("Putting message:" + m.id + " content:" + m.body.text);
		m.body.text = replaceWithContext(m.body.text);
		if(m.body.getClass() == MessageBodySingleSelect.class) {
		    MessageBodySingleSelect mss = (MessageBodySingleSelect) m.body;
            mss.choices.forEach(x -> {
                if(x.getClass() == SelectLink.class) {
                    SelectLink link = (SelectLink) x;
                    if(link.appUrl != null) {
						link.appUrl = replaceWithContext(link.appUrl);
					}
					if(link.webUrl != null) {
                    	link.webUrl = replaceWithContext(link.webUrl);
					}
                }
            });
		}else if(m.body.getClass() == MessageBodyBankIdCollect.class) {
		    MessageBodyBankIdCollect mbc = (MessageBodyBankIdCollect) m.body;
		    mbc.referenceId = replaceWithContext(mbc.referenceId);
        }
		memberChat.addToHistory(m);
	}

	private void createMessage(String id, MessageHeader header, MessageBody body){
		Message m = new Message();
		m.id = id;
		m.header = header;
		m.body = body;
		messageList.put(m.id, m);
	}

	private void createMessage(String id, MessageHeader header, MessageBody body, Integer delay){
		Message m = new Message();
		m.id = id;
		m.header = header;
		m.body = body;
		m.header.pollingInterval = new Long(delay);
		messageList.put(m.id, m);
	}


	void createMessage(String id, MessageBody body, SelectItemMessageCallback callback) {
		this.createMessage(id, body);
		this.setMessageCallback(id, callback);
	}

	protected void setMessageCallback(String id, SelectItemMessageCallback callback) {
		this.callbacks.put(id, callback);
	}

	boolean hasSelectItemCallback(String messageId) {
	    return this.callbacks.containsKey(messageId);
    }

    String execSelectItemCallback(String messageId, UserContext uc, SelectItem item) {
	    return this.callbacks.get(messageId).operation(uc, item);
    }

    // -------------------------
    
    void createMessage(String id, MessageBody body, Integer delay){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		createMessage(id,header,body,delay);    	
    }
    
	void createMessage(String id, MessageBody body, String avatarName, Integer delay){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		header.avatarName = avatarName;
		createMessage(id,header,body,delay);		
	}
	
	void createMessage(String id, MessageBody body, Image image, Integer delay){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		body.imageURL = image.imageURL;
		body.imageHeight = image.imageHeight;
		body.imageWidth = image.imageWidth;
		createMessage(id,header,body,delay);			
	}
	 
	// -------------------------
	
	void createMessage(String id, MessageBody body){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		createMessage(id,header,body);
	}
	
	void createMessage(String id, MessageBody body, String avatarName){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		header.avatarName = avatarName;
		createMessage(id,header,body);		
	}
	
	void createMessage(String id, MessageBody body, Image image){
		MessageHeader header = new MessageHeader(Conversation.HEDVIG_USER_ID,"/response",-1); //Default value
		body.imageURL = image.imageURL;
		body.imageHeight = image.imageHeight;
		body.imageWidth = image.imageWidth;
		createMessage(id,header,body);			
	}
	
	public abstract void recieveEvent(EventTypes e, String value);
	
	void startConversation(String startId){
		log.info("Starting conversation with message:" + startId);
		addToChat(messageList.get(startId));
	}
	
    public int getValue(MessageBodyNumber body){
    	return Integer.parseInt(body.text);
    }
    
    public String getValue(MessageBodySingleSelect body){

		for(SelectItem o : body.choices){
			if(SelectOption.class.isInstance(o) && SelectOption.class.cast(o).selected){
				return SelectOption.class.cast(o).value;
			}
		}   	
		return "";
    }
    
    public ArrayList<String> getValue(MessageBodyMultipleSelect body){
		ArrayList<String> selectedOptions = new ArrayList<String>();
		for(SelectItem o : body.choices){
			if(SelectOption.class.isInstance(o) && SelectOption.class.cast(o).selected){
				 selectedOptions.add(SelectOption.class.cast(o).value);
			}
		}   
		return selectedOptions;
    }

    // ------------------------------------------------------------------------------- //
    
	public abstract void recieveMessage(Message m);
	public void completeRequest(String nxtMsg) {
		if(getMessage(nxtMsg)!=null)addToChat(getMessage(nxtMsg));	
	}
	public abstract void init();
}
