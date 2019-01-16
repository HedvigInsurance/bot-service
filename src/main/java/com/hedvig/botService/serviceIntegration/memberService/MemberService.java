package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.LookupResponse;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface MemberService {
  Optional<BankIdAuthResponse> auth(String memberId);

  Optional<BankIdAuthResponse> auth(String ssn, String memberId);

  Optional<BankIdSignResponse> sign(String ssn, String userMessage, String memberId);

  BankIdSignResponse signEx(String ssn, String userMessage, String memberId);

  void finalizeOnBoarding(String memberId, UserData data);

  BankIdCollectResponse collect(String referenceToken, String memberId);

  MemberProfile getProfile(String hid);

  void startOnBoardingWithSSN(String memberId, String ssn);

  void selectCashback(String memberId, UUID charityId);

  void updateEmail(String memberId, String email);

  @Nullable
  LookupResponse lookupAddressSWE(String ssn, String memberId);

  void updatePhoneNumber(String memberId, String phoneNumber);
}
