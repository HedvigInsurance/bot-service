package com.hedvig.botService.services.events;

import lombok.Value;

@Value
public class OnboardingCallForQuoteEvent {
  private final String memberId;
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;

  public OnboardingCallForQuoteEvent(
    final String memberId,
    final String firstName,
    final String lastName,
    final String phoneNumber
  ) {
    this.memberId = memberId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
  }
}

