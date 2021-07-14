package com.hedvig.botService.services;

import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.claimsService.dto.ClaimFileFromAppDTO;
import com.hedvig.botService.serviceIntegration.slack.SlackClient;
import com.hedvig.botService.serviceIntegration.slack.SlackMessage;
import com.hedvig.botService.serviceIntegration.ticketService.TicketService;
import com.hedvig.botService.services.events.*;
import io.sentry.Sentry;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "hedvig.notifications.enabled", havingValue = "true")
public class NotificationService {

  private final SlackClient slackClient;
  private final TicketService ticketService;
  private final ClaimsService claimsService;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  public NotificationService(
    SlackClient slackClient,
    TicketService ticketService,
    ClaimsService claimsService
  ) {
    this.slackClient = slackClient;
    this.ticketService = ticketService;
    this.claimsService = claimsService;
  }

  @EventListener
  public void on(RequestPhoneCallEvent evt) {
    ticketService.createCallMeTicket(evt.getMemberId(), evt.getPhoneNumber(), evt.getFirstName(), evt.getLastName());

    String message = String.format("Medlem %s(%s %s) vill bli kontaktad på %s",
      evt.getMemberId(), evt.getFirstName(), evt.getLastName(), evt.getPhoneNumber());
    sendSlackMessage(message, "#messages-to-hedvig");
    sendSlackMessage(message, "#callme");
  }

  @EventListener
  public void on(UnderwritingLimitExcededEvent event) {
    String message = String.format(
      "Underwriting guideline för onboarding-medlem: %s, ring upp medlem på nummer: %s",
      event.getMemberId(), event.getPhoneNumber());
    sendSlackMessage(message, "#signups-and-stuff");
  }

  @EventListener
  public void on(OnboardingQuestionAskedEvent event) {
    String message = String.format("Ny fråga från onboarding-medlem: %s, \"%s\"",
      event.getMemberId(), event.getQuestion());
    sendSlackMessage(message, "#messages-to-hedvig");
  }

  @EventListener
  public void on(OnboardingFileUploadedEvent e) {
    String message = String.format(
      "A new file during  is uploaded from onboarding-member %s with type %s. The file key is %s",
      e.getMemberId(), e.getMimeType(), e.getKey());
    sendSlackMessage(message, "#messages-to-hedvig");
  }

  @EventListener
  public void on(ClaimAudioReceivedEvent event) {
    String message = String.format("Ny skadeanmälan ifrån medlem: %s", event.getMemberId());
    sendSlackMessage(message, "#claims-and-such");
  }

  @EventListener
  public void on(QuestionAskedEvent event) {
    String message = String.format("Ny fråga från medlem: %s, \"%s\".", event.getMemberId(),
      event.getQuestion());
    sendSlackMessage(message, "#messages-to-hedvig");
  }

  @EventListener
  public void on(FileUploadedEvent e) {
    String message =
      String.format("A new file is uploaded from member %s with type %s. The file key is %s",
        e.getMemberId(), e.getMimeType(), e.getKey());
    sendSlackMessage(message, "#messages-to-hedvig");

    ClaimFileFromAppDTO claimFile = new ClaimFileFromAppDTO(
      e.getKey(),
      e.getMimeType(),
      e.getMemberId()
    );

    try {
      claimsService.linkFileFromAppToClaim(claimFile);
    } catch (Exception exception) {
      Sentry.capture(exception);
      log.error("Cannot link file " + e.getKey() + " to a claim" + exception);
    }
  }

  @EventListener
  public void on(RequestObjectInsuranceEvent event) {
    String message = String.format(
      "Ny medlem signerad! Medlemmen har id %s och har någon pryl som är dyrare än 50':-. Produkttypen är %s",
      event.getMemberId(), event.getProductType());
    sendSlackMessage(message, "#signups-and-stuff");
  }

  @EventListener
  public void on(RequestStudentObjectInsuranceEvent event) {
    val message = String.format(
      "Ny studentmedlem signerad! Medlemmen har id %s och har någon pryl som är dyrare än 25':-. Produkttypen är %s",
      event.getMemberId(), event.getProductType());
    sendSlackMessage(message, "#signups-and-stuff");
  }

  @EventListener
  public void on(MemberSignedEvent event) {
    val message = String.format("Ny medlem signerad! Medlemmen har id %s och produkttypen är %s",
      event.getMemberId(), event.getProductType());
    sendSlackMessage(message, "#signups-and-stuff");
  }

  @EventListener
  public void on(ClaimCallMeEvent event) {
    ticketService.createCallMeTicket(event.getMemberId(), event.getPhoneNumber(), event.getFirstName(), event.getFamilyName());

    String message = String.format(
      "Medlem %s(%s %s) med %s försäkring har fått en skada och vill bli uppringd på %s",
      event.getMemberId(), event.getFirstName(), event.getFamilyName(),
      event.isInsuranceActive() ? "AKTIV" : "INAKTIV", event.getPhoneNumber());
    sendSlackMessage(message, "#claims-and-such");
  }

  @EventListener
  public void on(OnboardingCallForQuoteEvent event) {
    String message = String.format(
      "Potential member %s - %s %s tried to sign-up, give them a call on %s",
      event.getMemberId(), event.getFirstName(), event.getLastName(), event.getPhoneNumber());
    sendSlackMessage(message, "#messages-to-hedvig");
  }

  private void sendSlackMessage(String message, String channel) {
      try {
        slackClient.post(
          new SlackMessage(
            message,
            channel,
            "Bender",
            ":robot-face:"
          )
        );
      } catch (Exception e) {
        log.error("Failed to post to Slack", e);
      }
  }
}
