package com.hedvig.botService.serviceIntegration.claimsService;

import com.hedvig.botService.serviceIntegration.claimsService.dto.ClaimFileFromAppDTO;
import com.hedvig.botService.serviceIntegration.claimsService.dto.StartClaimAudioDTO;
import feign.FeignException;
import java.time.LocalDateTime;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClaimsService {

  private final Logger log = LoggerFactory.getLogger(ClaimsService.class);
  private final ClaimsServiceClient claimsClient;

  @Autowired
  public ClaimsService(ClaimsServiceClient claimsClient) {
    this.claimsClient = claimsClient;
  }

  public void createClaimFromAudio(final String memberId, final String audioUrl) {
    val dto = new StartClaimAudioDTO(memberId, LocalDateTime.now(), audioUrl);

    try {
      claimsClient.createClaimFromAudio(dto);
    } catch (FeignException ex) {
      log.error("Could not start claim at claim service", ex);
    }
  }

  public void linkFileFromAppToClaim(ClaimFileFromAppDTO dto) {
    claimsClient.linkFileFromAppToClaim(dto);
  }
}
