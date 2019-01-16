package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.serviceIntegration.memberService.dto.*;
import com.hedvig.botService.web.dto.Member;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
  name = "memberServiceClient",
  url = "${hedvig.member-service.url:member-service}",
  configuration = FeignConfiguration.class)
public interface MemberServiceClient {

  @RequestMapping(value = "/member/bankid/auth", method = RequestMethod.POST)
  ResponseEntity<BankIdAuthResponse> auth(@RequestBody BankIdAuthRequest request);

  @RequestMapping(value = "/member/bankid/sign", method = RequestMethod.POST)
  ResponseEntity<BankIdSignResponse> sign(@RequestBody BankIdSignRequest request);

  @RequestMapping(value = "/member/bankid/collect", method = RequestMethod.POST)
  ResponseEntity<BankIdCollectResponse> collect(
    @RequestHeader("hedvig.token") String memberId,
    @RequestParam("referenceToken") String referenceToken);

  @RequestMapping(value = "/i/member/{memberId}", method = RequestMethod.GET)
  ResponseEntity<Member> profile(@PathVariable("memberId") String memberId);

  @RequestMapping(value = "/i/member/{memberId}/startOnboardingWithSSN")
  ResponseEntity<Void> startOnBoardingWithSSN(
    @PathVariable("memberId") String memberId,
    @RequestBody StartOnboardingWithSSNRequest request);

  @RequestMapping(value = "/i/member/{memberId}/finalizeOnboarding")
  ResponseEntity<FinalizeOnBoardingResponse> finalizeOnBoarding(
    @PathVariable("memberId") String memberId, @RequestBody FinalizeOnBoardingRequest req);

  @RequestMapping(value = "/i/member/{memberId}/updateEmail")
  ResponseEntity<String> updateEmail(
    @PathVariable("memberId") String memberId, @RequestBody UpdateEmailRequest request);

  @RequestMapping(value = "/cashback", method = RequestMethod.POST)
  ResponseEntity<String> selectCashback(
    @RequestHeader("hedvig.token") String hid, @RequestParam("optionId") UUID optionId);

  @RequestMapping(value = "/_/addresslookup/swe", method = RequestMethod.POST)
  SweAddressResponse lookupAddressSwe(@NotNull SweAddressRequest request);

  @RequestMapping(value = "/{memberId}/updatePhoneNumber", method = RequestMethod.POST)
  void updatePhoneNumber(@PathVariable("memberId") String memberId, @RequestBody UpdatePhoneNumberRequest request);
}
