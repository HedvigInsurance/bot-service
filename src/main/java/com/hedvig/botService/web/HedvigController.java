package com.hedvig.botService.web;

import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.services.SessionManager;
import com.hedvig.botService.web.dto.CollectResponse;
import com.hedvig.botService.web.dto.TrackingDTO;
import org.checkerframework.common.value.qual.UnknownVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
@RequestMapping("/hedvig")
public class HedvigController {

  private final Logger log = LoggerFactory.getLogger(HedvigController.class);
  private final SessionManager sessionManager;

  @Value("${hedvig.appleUser.memberId}")
  private String APPLE_USER_MEMBER_ID;

  @Autowired
  public HedvigController(SessionManager sessions) {
    this.sessionManager = sessions;
  }

  @PostMapping(path = "/register_campaign")
  public ResponseEntity<Void> campaign(
    @RequestBody TrackingDTO tracker, @RequestHeader(value = "hedvig.token") String hid) {
    log.info("Received tracking information for user " + hid);
    sessionManager.saveTrackingInformation(hid, tracker);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/push-token")
  ResponseEntity<Void> pushToken(
    @RequestBody String tokenJson, @RequestHeader(value = "hedvig.token") String hid) {
    log.info("Push token for memberId:{}, is: {}", value("memberId", ""), tokenJson);
    sessionManager.saveExpoPushToken(hid, tokenJson);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("trustlyClosed")
  ResponseEntity<String> trustlyClosed(@RequestHeader(value = "hedvig.token") String hid) {
    log.info("GET trustlyClosed");

    this.sessionManager.trustlyClosed(hid);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("collect")
  ResponseEntity<?> collect(
    @RequestParam String referenceToken,
    @RequestHeader(value = "hedvig.token") String hid
  ) {

    log.info("Post collect with reference token: {}", value("referenceToken", referenceToken));
    try {
      BankIdCollectResponse response = this.sessionManager.collect(hid, referenceToken);

      ResponseEntity.BodyBuilder responseEntity = ResponseEntity.ok();

      String newMemberId = response.getNewMemberId();

      if (newMemberId != null && !newMemberId.equals(hid)) {
        responseEntity = responseEntity.header("Hedvig.Id", newMemberId);
      }

      CollectResponse responseBody = new CollectResponse(response.getBankIdStatus().name());

      return responseEntity.body(responseBody);
    } catch (Exception e) {
      log.error("Error collecting: ", e);
      return ResponseEntity.ok(new CollectResponse("ERROR"));
    }
  }

  @PostMapping("emailSign")
  ResponseEntity<?> emailSign(@RequestHeader(value = "hedvig.token") String memberId) {
    log.info("Email login for member: {}", memberId);

    boolean isEmailSignSuccessful = this.sessionManager.emailSign(memberId);

    if (isEmailSignSuccessful) {
      log.info("APPLE USER: {}", APPLE_USER_MEMBER_ID);
      return ResponseEntity.ok().header("Hedvig.Id", APPLE_USER_MEMBER_ID).build();
    }
    return ResponseEntity.badRequest().build();
  }
}
