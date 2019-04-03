package com.hedvig.botService.web;

import com.hedvig.botService.Profiles;
import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.services.exceptions.UnauthorizedException;
import com.hedvig.botService.services.triggerService.TriggerService;
import com.hedvig.botService.services.triggerService.dto.CreateDirectDebitMandateDTO;
import com.hedvig.botService.web.dto.TriggerResponseDTO;
import java.net.URI;
import java.util.UUID;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hedvig/trigger")
public class TriggerController {

  private final Logger log = LoggerFactory.getLogger(TriggerController.class);

  private final TriggerService triggerService;
  private final Environment environment;
  private final URI errorPageUrl;

  public TriggerController(
      TriggerService triggerService,
      Environment environment,
      @Value("${hedvig.trigger.errorPageUrl:http://hedvig.com/error}") String errorPageUrl) {
    this.triggerService = triggerService;
    this.environment = environment;
    this.errorPageUrl = URI.create(errorPageUrl);
  }

  @PostMapping("{triggerId}")
  public ResponseEntity<TriggerResponseDTO> index(
      @RequestHeader("hedvig.token") String hid, @PathVariable UUID triggerId) {

    final String triggerUrl = triggerService.getTriggerUrl(triggerId, hid);
    if (triggerUrl == null) {
      return ResponseEntity.notFound().build();
    }

    TriggerResponseDTO response =
        new TriggerResponseDTO(triggerUrl + "&gui=native&color=%23651EFF&bordercolor=%230F007A");
    return ResponseEntity.ok(response);
  }

  /**
   * Helper function that allows easy testing of the DirectDebitMandates during development.
   *
   * @return
   */
  @PostMapping("_/createDDM")
  public ResponseEntity<String> index(
      @RequestHeader("hedvig.token") String hid,
      @RequestBody CreateDirectDebitMandateDTO requestData) {

    if (ArrayUtils.contains(environment.getActiveProfiles(), Profiles.PRODUCTION)) {
      return ResponseEntity.notFound().build();
    }

    final UUID triggerId = triggerService.createTrustlyDirectDebitMandate(requestData, hid);

    return ResponseEntity.ok("{\"id\":\"" + triggerId.toString() + "\"}");
  }

  @GetMapping("/notification")
  public ResponseEntity<?> getNotification(
      @RequestParam("triggerId") UUID triggerId,
      @RequestParam("status") DirectDebitMandateTrigger.TriggerStatus status) {

    log.info(
        "GET /hedvig/trigger/notification, triggerId: {}, status: {}",
        triggerId.toString(),
        status.name());

    try {
      final String redirectionUrl = triggerService.clientNotificationReceived(triggerId, status);

      log.info("Redirecting to: {}", redirectionUrl);
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
          .location(URI.create(redirectionUrl))
          .build(); // .location()
    } catch (Exception ex) {
      log.error(
          "Exception caught in TriggerController.getNotificaiton, redirecting to " + errorPageUrl,
          ex);
    }

    log.info("Redirecting to: {}", this.errorPageUrl);
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(this.errorPageUrl).build();
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnauthorizedException.class)
  public String handleException(UnauthorizedException ex) {
    return ex.getMessage();
  }
}
