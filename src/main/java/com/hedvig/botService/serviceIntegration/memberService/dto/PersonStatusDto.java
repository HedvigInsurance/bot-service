package com.hedvig.botService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class PersonStatusDto {
  public Flag flag;
  public boolean whitelisted;
}
