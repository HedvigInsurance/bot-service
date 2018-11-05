package com.hedvig.botService.web;

import static net.logstash.logback.argument.StructuredArguments.value;

import com.hedvig.botService.services.SessionManager;
import com.hedvig.botService.web.dto.UpdateUserContextDTO;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/member/")
public class InternalUserDataController {

  private static Logger log = LoggerFactory.getLogger(InternalUserDataController.class);
  private final SessionManager sessionManager;

  @Autowired
  public InternalUserDataController(SessionManager sessions) {
    this.sessionManager = sessions;
  }

  @GetMapping(value = "{memberId}/push-token", produces = "application/json")
  ResponseEntity<?> pushToken(@PathVariable String memberId) {
    log.info("Get pushtoken for memberId:{}, is: {}", value("memberId", ""));
    String token = sessionManager.getPushToken(memberId);
    if (token == null) {
      return ResponseEntity.ok("{\"token\":null}");
    }
    return new ResponseEntity<String>(token, HttpStatus.OK);
  }

  @PostMapping(value = "{memberId}/enableTrustlyButton")
  ResponseEntity<?> enableTrustlyButton(@PathVariable String memberId) {
    log.info("Enabling trustly button");
    sessionManager.enableTrustlyButtonForMember(memberId);
    return ResponseEntity.accepted().build();
  }

  @PostMapping(value = "{memberId}/initSessionWebOnBoarding", consumes = "application/json")
  ResponseEntity<?> updateMemberContext(@PathVariable(name = "memberId") String memberId,
    @RequestBody @Valid
      UpdateUserContextDTO req) {
    log.info("Update user context request for member {} with request {}", memberId, req);
    sessionManager.init_web_onboarding(memberId, req);
    return ResponseEntity.noContent().build();
  }
}
