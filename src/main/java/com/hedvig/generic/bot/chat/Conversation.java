package com.hedvig.generic.bot.chat;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedvig.generic.bot.session.UserContext;

public abstract class Conversation {

	public static final long  HEDVIG_USER_ID = 1; // The id hedvig uses to chat
	public static final String regexPattern = "\\{(.*?)\\}";
	private static Logger log = LoggerFactory.getLogger(Conversation.class);
	public String conversationId; // Id for the conversation
	public ChatHistory chatHistory;
	public UserContext userContext;
	public TreeMap<String, Message> messageList = new TreeMap<String, Message>();
	public HashMap<String, String> conversationContext = new HashMap<String, String>(); // Context specific information learned during conversation
	
	public Conversation(String conversationId, ChatHistory c, UserContext u) {
		this.conversationId = conversationId;
		this.chatHistory = c;
		this.userContext = u;
	}
	public String getConversationId() {
		return conversationId;
	}
	public ConversationMessage getCurrent() {
		return current;
	}
	public void setCurrent(ConversationMessage current) {
		this.current = current;
	}
	private ConversationMessage current = null; // Last message sent to client
	
	public String replaceWithContext(String input){
		log.debug("Contextualizing string:" + input);
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher m = pattern.matcher(input);
		while (m.find()) {
			String s = m.group();
			String r = conversationContext.get(s);
			log.debug(s + ":" + r);
			if(r!=null){input = input.replace(s, r);}
		}
		log.debug("-->" + input);
		return input;
	}
	
	public void sendMessage(Message m) {
		log.info("Sending message:" + m.id + " content:" + m.body.content);
		m.body.content = replaceWithContext(m.body.content);
		long t = System.currentTimeMillis();
		m.header.timeStamp = t;
		chatHistory.addMessage(t, m);
	}

	public abstract void recieveMessage(Message m);
	public abstract void init();
}
