package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.dto.Address;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.FinalizeOnBoardingRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.LookupResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.StartOnboardingWithSSNRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.SweAddressRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.UpdateEmailRequest;
import com.hedvig.botService.serviceIntegration.memberService.dto.UpdatePhoneNumberRequest;
import com.hedvig.botService.web.dto.Member;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MemberServiceFeign implements MemberService {

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
    try {
      ResponseEntity<BankIdAuthResponse> auth = this.client.auth(authRequest);
      return Optional.of(auth.getBody());
    } catch (Throwable ex) {
      log.error("Got error response when calling memberService.auth", ex);
      return Optional.empty();
    }
  }

  @Override
  public Optional<BankIdSignResponse> sign(String ssn, String userMessage, String memberId) {
    BankIdSignRequest request = new BankIdSignRequest(ssn, userMessage, memberId);
    try {
      ResponseEntity<BankIdSignResponse> response = this.client.sign(request);
      return Optional.of(response.getBody());
    } catch (Throwable ex) {
      log.error("Got error response when calling memberService.sign", ex);
      return Optional.empty();
    }
  }

  @Override
  public BankIdSignResponse signEx(String ssn, String userMessage, String memberId) {
    BankIdSignRequest request = new BankIdSignRequest(ssn, userMessage, memberId);
    ResponseEntity<BankIdSignResponse> response = this.client.sign(request);
    return response.getBody();
  }

  @Override
  public void finalizeOnBoarding(String memberId, UserData data) {

    FinalizeOnBoardingRequest req = new FinalizeOnBoardingRequest();
    req.setFirstName(data.getFirstName());
    req.setLastName(data.getFamilyName());
    req.setMemberId(memberId);
    req.setSsn(data.getSSN());
    req.setEmail(data.getEmail());
    req.setPhoneNumber(data.getPhoneNumber());

    Address address = new Address(
      data.getAddressStreet(),
      data.getAddressCity(),
      data.getAddressZipCode(),
      "",
      data.getFloor());
    req.setAddress(address);
    try {
      this.client.finalizeOnBoarding(memberId, req);
    } catch (RestClientResponseException ex) {
      log.error("Could not finalize member {}", memberId, ex);
    }
  }

  @Override
  public BankIdCollectResponse collect(String referenceToken, String memberId) {

    ResponseEntity<BankIdCollectResponse> collect = this.client.collect(memberId, referenceToken);
    return collect.getBody();
  }

  @Override
  public MemberProfile getProfile(String memberId) {

    final Member profile = this.client.profile(memberId).getBody();
    MemberAddress address = null;
    if (profile.getStreet() != null && profile.getZipCode() != null && profile.getCity() != null) {
      address =
        new MemberAddress(
          profile.getStreet(),
          profile.getCity(),
          profile.getZipCode(),
          profile.getApartment(),
          profile.getFloor() == null ? 0 : profile.getFloor());
    }

    return new MemberProfile(
      memberId,
      profile.getSsn(),
      profile.getFirstName(),
      profile.getLastName(),
      Optional.ofNullable(address),
      "",
      profile.getPhoneNumber(),
      profile.getCountry(),
      profile.getBirthDate());
  }

  @Override
  public void startOnBoardingWithSSN(String memberId, String ssn) {
    this.client.startOnBoardingWithSSN(memberId, new StartOnboardingWithSSNRequest(ssn));
  }

  @Override
  public void selectCashback(String memberId, UUID charityId) {
    send(() -> this.client.selectCashback(memberId, charityId));
  }

  @Override
  public void updateEmail(String memberId, String email) {
    send(() -> this.client.updateEmail(memberId, new UpdateEmailRequest(email)));
  }

  @Nullable
  @Override
  public LookupResponse lookupAddressSWE(String trimmedSSN, String memberId) {
    try {
      val response = this.client.lookupAddressSwe(new SweAddressRequest(trimmedSSN, memberId));
      return new LookupResponse(response.getFirstName(), response.getLastName(), response.getAddress());
    } catch (RuntimeException ex) {
      log.error("Caught error lookingup memberAddress", ex);
      return null;
    }
  }

  @Override
  public void updatePhoneNumber(String memberId, String phoneNumber) {
    try {
      this.client.updatePhoneNumber(memberId, new UpdatePhoneNumberRequest(phoneNumber));
    } catch (RestClientResponseException e) {
      log.error("Cannot update phoneNumber for memberId {} with the following phone number {}", memberId, phoneNumber);
    }catch (FeignException ex){
      log.error("Cannot update phoneNumber for memberId {} with the following phone number {}", memberId, phoneNumber);
    }
  }

  @Override
  public void initAppleUser(String appleMemberId) {
    try {
      this.client.initAppleUser(appleMemberId);
    }catch (FeignException | RestClientResponseException ex){
      log.error("Cannot init apple member {}", appleMemberId);
    }
  }

  private void send(Runnable supplier) {
    try {
      supplier.run();
    } catch (FeignException ex) {
      log.error("Could not send request to member-service", ex);
    }
  }
}
