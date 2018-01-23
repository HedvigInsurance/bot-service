package com.hedvig.botService.serviceIntegration.memberService;

import com.hedvig.botService.serviceIntegration.memberService.dto.*;
import com.hedvig.botService.web.dto.Member;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="memberServiceClient", url = "${hedvig.member-service.url:member-service}", configuration = FeignConfiguration.class)
public interface MemberServiceClient {

    @RequestMapping(value = "/member/bankid/auth", method = RequestMethod.POST)
    ResponseEntity<BankIdAuthResponse> auth(@RequestBody BankIdAuthRequest request);

    @RequestMapping(value = "/member/bankid/sign", method = RequestMethod.POST)
    ResponseEntity<BankIdSignResponse> sign(@RequestBody BankIdSignRequest request);

    @RequestMapping(value = "/member/bankid/collect", method = RequestMethod.POST)
    ResponseEntity<BankIdCollectResponse> collect(@RequestHeader("hedvig.token") String memberId, @RequestParam("referenceToken") String referenceToken);

    @RequestMapping(value = "/member/{memberId}", method = RequestMethod.GET)
    ResponseEntity<Member> profile(@PathVariable("memberId") String memberId);

    @RequestMapping(value = "/i/member/{memberId}/startOnboardingWithSSN")
    ResponseEntity<Void> startOnBoardingWithSSN(@PathVariable("memberId") String memberId, @RequestBody StartOnboardingWithSSNRequest request);

    @RequestMapping(value = "/i/member/{memberId}/finalizeOnboarding")
    ResponseEntity<FinalizeOnBoardingResponse> finalizeOnBoarding(@PathVariable("memberId") String memberId, @RequestBody FinalizeOnBoardingRequest req);
}