package com.hedvig.botService.web.dto;

public class AddMessageRequestDTO extends BackOfficeInputMessageDTO {
  public static final String MESSAGE_ID="message.bo.message";

  public AddMessageRequestDTO(){}

  public AddMessageRequestDTO(String memberId, String msg, Boolean forceSendMessage) {
    this.memberId = memberId;
    this.msg = msg;
    this.forceSendMessage = forceSendMessage;
  }
}
