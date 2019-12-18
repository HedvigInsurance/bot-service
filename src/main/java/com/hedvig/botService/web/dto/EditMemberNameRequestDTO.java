package com.hedvig.botService.web.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class EditMemberNameRequestDTO {
  @NotNull private final String memberId;
  @NotNull private final String firstName;
  @NotNull private final String lastName;
}
