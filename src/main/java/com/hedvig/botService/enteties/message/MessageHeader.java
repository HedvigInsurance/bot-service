package com.hedvig.botService.enteties.message;

import lombok.ToString;
import lombok.val;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@ToString
public class MessageHeader {

  public static final long HEDVIG_USER_ID = 1; // The id hedvig uses to chat
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer messageId;

  /*
   * Header elements
   * */
  public Long fromId;
  public Long timeStamp; // Time when sent/recieved on API-GW
  public String loadingIndicator; // Link to animation to show during load
  public String avatarName; // Link to avatar animation to show over message
  public Long pollingInterval; // Frequency of next request
  public String statusMessage = null;

  public Boolean markedAsRead; // if client has marked this message as read i.e displayed on screen

  @Nullable
  public Boolean richTextChatCompatible = false;

  @Transient
  public boolean editAllowed; // For client use to indicate if the last message is editable

  public Boolean
      shouldRequestPushNotifications; // Should responding to this message prompt user to turn on

  public MessageHeader(long hedvigUserId, long timeStamp) {
    this.fromId = hedvigUserId;
    this.timeStamp = timeStamp;
    this.pollingInterval = 1000L; // Default value = 1s
    this.loadingIndicator = "loader"; // Default value
    this.shouldRequestPushNotifications = false;
    this.markedAsRead = false;
  }

  public MessageHeader(long hedvigUserId, long timeStamp, boolean shouldRequestPushNotifications) {
    this.fromId = hedvigUserId;
    this.timeStamp = timeStamp;
    this.pollingInterval = 1000L;
    this.loadingIndicator = "loader";
    this.shouldRequestPushNotifications = shouldRequestPushNotifications;
    this.markedAsRead = false;
  }

  public static MessageHeader createRichTextHeader(){
    val header = new MessageHeader(HEDVIG_USER_ID, -1, true);
    header.richTextChatCompatible = true;
    return header;
  }

  public MessageHeader() {}
}
