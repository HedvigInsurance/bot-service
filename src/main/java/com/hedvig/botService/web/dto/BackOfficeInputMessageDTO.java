package com.hedvig.botService.web.dto;

import lombok.Data;

@Data
public class BackOfficeInputMessageDTO {
  String userId;
  String memberId;
  String msg;

  public BackOfficeInputMessageDTO() {
    super ();
  }
}
