package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignResponse;
import com.hedvig.botService.web.dto.Member;

import java.util.Optional;

public interface MemberService {
    Optional<BankIdAuthResponse> auth();

    Optional<BankIdAuthResponse> auth(String ssn);

    String  startBankAccountRetrieval(String memberId, String bankShortId);

    Optional<BankIdSignResponse> sign(String ssn, String userMessage);

    void finalizeOnBoarding(String memberId, UserData data);

    BankIdAuthResponse collect(String referenceToken, String memberId);

    Member convertToFakeUser(String memberId);

    Member getProfile(String hid);

    void startOnBoardingWithSSN(String memberId, String ssn);
}
