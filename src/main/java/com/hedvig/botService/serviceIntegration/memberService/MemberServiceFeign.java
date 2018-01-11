package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.dto.*;
import com.hedvig.botService.web.dto.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Service
public class MemberServiceFeign implements MemberService {

    private final Logger log = LoggerFactory.getLogger(MemberServiceFeign.class);
    private final MemberServiceClient client;

    public MemberServiceFeign(MemberServiceClient client) {

        this.client = client;
    }

    @Override
    public Optional<BankIdAuthResponse> auth(String memberId) {
        return auth(null, memberId);
    }

    @Override
    public Optional<BankIdAuthResponse> auth(String ssn, String memberId) {
        BankIdAuthRequest authRequest = new BankIdAuthRequest(null, memberId);
        ResponseEntity<BankIdAuthResponse> auth = this.client.auth(authRequest);
        return Optional.of(auth.getBody());
    }

    @Override
    public String startBankAccountRetrieval(String memberId, String bankShortId) {
        return null;
    }

    @Override
    public Optional<BankIdSignResponse> sign(String ssn, String userMessage, String memberId) {
        BankIdSignRequest request = new BankIdSignRequest(ssn, userMessage, memberId);
        ResponseEntity<BankIdSignResponse> response = this.client.sign(request);
        return Optional.of(response.getBody());
    }

    @Override
    public void finalizeOnBoarding(String memberId, UserData data) {

        FinalizeOnBoardingRequest req = new FinalizeOnBoardingRequest();
        req.setFirstName(data.getFirstName());
        req.setLastName(data.getFamilyName());
        req.setMemberId(memberId);
        req.setSsn(data.getSSN());
        req.setEmail(data.getEmail());

        Address address = new Address();
        address.setStreet(data.getAddressStreet());
        address.setCity(data.getAddressCity());
        address.setZipCode(data.getAddressZipCode());
        req.setAddress(address);
        try {
            ResponseEntity<FinalizeOnBoardingResponse> response = this.client.finalizeOnBoarding(memberId, req);
        }catch (RestClientResponseException ex) {
            log.error("Could not finalize member {}", memberId, ex);
        }
    }

    @Override
    public BankIdCollectResponse collect(String referenceToken, String memberId) {

        ResponseEntity<BankIdCollectResponse> collect = this.client.collect(memberId, referenceToken);
        return collect.getBody();
    }

    @Override
    public Member convertToFakeUser(String memberId) {
        throw new RuntimeException("Cannot create fake user in live environment!");
    }

    @Override
    public Member getProfile(String hid) {
        return this.client.profile(hid).getBody();
    }

    @Override
    public void startOnBoardingWithSSN(String memberId, String ssn) {
        this.client.startOnBoardingWithSSN(memberId, new StartOnboardingWithSSNRequest(ssn));
    }
}
