package com.hedvig.botService.web.dto;

import com.hedvig.botService.enteties.message.Message;

/*
 * This class is a wrapper for sending messages from internal Hedvig administrations to users
 * Apart from the message object is meta information about users
 * */

public class BackOfficeMessageDTO {

    public String userId;
    public Message msg;
    public Boolean addActionButtons;
    public BackOfficeMessageDTO(){}

    public BackOfficeMessageDTO(Message msg, String userId) {
        this.msg = msg;
        this.userId = userId;
        this.addActionButtons = false;
    }
    
    public BackOfficeMessageDTO(Message msg, String userId, Boolean addActionButtons) {
        this.msg = msg;
        this.userId = userId;
        this.addActionButtons = addActionButtons;
    }
    
}
