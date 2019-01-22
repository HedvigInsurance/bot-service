package com.hedvig.botService.enteties;

import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.web.dto.AddMessageRequestDTO;
import com.hedvig.botService.web.dto.BackOfficeAnswerDTO;
import lombok.Getter;
import lombok.ToString;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_FROM_BO;
import static com.hedvig.botService.chat.OnboardingConversationDevi.MESSAGE_FORSLAG2;

/*
 * All timestamp information is set from here
 * */

@Entity
@ToString(exclude = "userContext")
public class MemberChat {

  private static Logger log = LoggerFactory.getLogger(MemberChat.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Getter private String memberId;

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey(name = "timestamp")
  @OrderBy("globalId DESC")
  public List<Message> chatHistory;

  @OneToOne() public UserContext userContext;

  public MemberChat() {
    chatHistory = new ArrayList<>();
    // new Exception().printStackTrace(System.out);
  }

  public MemberChat(String memberId) {
    log.info("Instantiating MemberChat for member: " + memberId);
    this.memberId = memberId;
    this.chatHistory = new ArrayList<>();
  }

  /*
   * Removes (by marking them as deleted) all messages
   * */
  public void reset() {

    chatHistory.clear(); // TODO: Make non delete solutions
  }

  /*
   * Removes (by marking them as deleted) all messages until the last point of user input
   * */
  public void revertLastInput() {

    /*
     * If there is no input message to revert to yet then leave the chat as is
     * */
    boolean hasUserInput = false;
    for (Message m : chatHistory) {
      if (!m.deleted && m.header.fromId != MessageHeader.HEDVIG_USER_ID) {
        hasUserInput = true;
        break;
      }
    }
    if (!hasUserInput) {
      return;
    }

    for (Message m : chatHistory) {
      m.deleted = true;
      if (!(m.header.fromId == MessageHeader.HEDVIG_USER_ID)) {
        break;
      }
    }
  }

  /*
   * Mark ONLY last input from user as editAllowed -> pen symbol in client
   * */
  private void markLastInput() {
    /*
     * If there is no input message to revert to yet then leave the chat as is
     * */
    boolean hasUserInput = false;
    for (Message m : chatHistory) {
      if (!m.deleted && m.header.fromId != MessageHeader.HEDVIG_USER_ID) {
        hasUserInput = true;
        m.header.editAllowed = false;
      }
    }
    if (!hasUserInput) {
      return;
    }

    var backOfficeReplied = false;
    for (Message m : chatHistory) {
      backOfficeReplied = backOfficeReplied
        || m.id.equals(BackOfficeAnswerDTO.MESSAGE_ID)
        || m.id.equals(AddMessageRequestDTO.MESSAGE_ID)
        || m.id.equals(FREE_CHAT_FROM_BO);

      if (!(m.header.fromId == MessageHeader.HEDVIG_USER_ID)) {
        if (!backOfficeReplied && !m.getBaseMessageId().equals(MESSAGE_FORSLAG2)) {
          m.header.editAllowed = true;
        }
        break;
      }
    }
  }

  public void addToHistory(Message m) {
    log.info("MemberChat.addToHistory(Message: " + m);
    Instant time = Instant.now();
    m.deleted = false;
    m.setTimestamp(time);
    m.header.timeStamp = time.toEpochMilli();
    m.chat = this;
    this.chatHistory.add(m);
  }

  public List<Message> getMessages() {
    // Mark last user input with as editAllowed
    this.markLastInput();

    // Check for deleted messages
    ArrayList<Message> returnList = new ArrayList<>();
    for (Message m : this.chatHistory) {
      if (m.deleted == null || !m.deleted) {
        returnList.add(m);
      }
    }

    return returnList;
  }
}
