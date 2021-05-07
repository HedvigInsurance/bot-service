package com.hedvig.botService.serviceIntegration.claimsService;

import com.hedvig.botService.serviceIntegration.claimsService.dto.ActiveClaimsDTO;
import com.hedvig.botService.serviceIntegration.claimsService.dto.ClaimFileFromAppDTO;
import com.hedvig.botService.serviceIntegration.claimsService.dto.StartClaimAudioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "claimsServiceClient", url = "${hedvig.claims-service.url:claims-service}")
public interface ClaimsServiceClient {

  @RequestMapping(value = "/_/claims/startClaimFromAudio", method = RequestMethod.POST)
  void createClaimFromAudio(@RequestBody StartClaimAudioDTO dto);

  @PostMapping("/_/claims/linkFileToClaim")
  ResponseEntity<Void> linkFileFromAppToClaim(@RequestBody ClaimFileFromAppDTO dto);
}
