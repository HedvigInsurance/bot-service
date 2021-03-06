package com.hedvig.botService.serviceIntegration.memberService.dto;

import lombok.Data;

@Data
public class FinalizeOnBoardingRequest {

  private String memberId;

  private String ssn;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Address address;
}
