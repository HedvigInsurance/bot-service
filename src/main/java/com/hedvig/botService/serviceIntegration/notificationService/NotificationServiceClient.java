package com.hedvig.botService.serviceIntegration.notificationService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "notification-service",
    url = "${hedvig.notificationservice.baseurl:notification-service}")
public interface NotificationServiceClient {
  @GetMapping("/_/notifications/{memberId}/token")
  ResponseEntity<String> getFirebaseToken(@PathVariable(name = "memberId") String memberId);

  @PostMapping("/_/notifications/{memberId}/token")
  ResponseEntity<?> setFirebaseToken(
      @PathVariable(name = "memberId") String memberId, String token);
}
