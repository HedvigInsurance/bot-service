package com.hedvig.botService.services;

import com.hedvig.botService.services.events.ClaimAudioReceivedEvent;
import com.hedvig.botService.services.events.ClaimCallMeEvent;
import com.hedvig.botService.services.events.FileUploadedEvent;
import com.hedvig.botService.services.events.MemberSignedEvent;
import com.hedvig.botService.services.events.OnboardingFileUploadedEvent;
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import com.hedvig.botService.services.events.RequestObjectInsuranceEvent;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import com.hedvig.botService.services.events.RequestStudentObjectInsuranceEvent;
import com.hedvig.botService.services.events.SignedOnWaitlistEvent;
import com.hedvig.botService.services.events.UnderwritingLimitExcededEvent;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class NotificationService {

  private final NotificationMessagingTemplate template;
  private final Logger log = LoggerFactory.getLogger(NotificationService.class);

  public NotificationService(NotificationMessagingTemplate template) {

    this.template = template;
  }

  @EventListener
  @Deprecated
  public void on(SignedOnWaitlistEvent evt) {
    sendNewMemberNotification("Ny person på väntelistan! " + evt.getEmail(), "PersonOnWaitlist");
  }

  @EventListener
  public void on(RequestPhoneCallEvent evt) {
    final String message = String.format("Medlem %s(%s %s) vill bli kontaktad på %s",
        evt.getMemberId(), evt.getFirstName(), evt.getLastName(), evt.getPhoneNumber());
    sendMessageFromMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(UnderwritingLimitExcededEvent event) {
    final String message = String.format(
        "Underwriting guideline för onboarding-medlem: %s, ring upp medlem på nummer: %s",
        event.getMemberId(), event.getPhoneNumber());
    sendNewMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(OnboardingQuestionAskedEvent event) {
    final String message = String.format("Ny fråga från onboarding-medlem: %s, \"%s\"",
        event.getMemberId(), event.getQuestion());
    sendMessageFromMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(OnboardingFileUploadedEvent e) {
    final String message = String.format(
        "A new file during  is uploaded from onboarding-member %s with type %s. The file key is %s",
        e.getMemberId(), e.getMimeType(), e.getKey());
    sendMessageFromMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(ClaimAudioReceivedEvent event) {
    final String message = String.format("Ny skadeanmälan ifrån medlem: %s", event.getMemberId());
    sendNewClaimNotification(message, "CallMe");
  }

  @EventListener
  public void on(QuestionAskedEvent event) {
    final String message = String.format("Ny fråga från medlem: %s, \"%s\".", event.getMemberId(),
        event.getQuestion());
    sendMessageFromMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(FileUploadedEvent e) {
    final String message =
        String.format("A new file is uploaded from member %s with type %s. The file key is %s",
            e.getMemberId(), e.getMimeType(), e.getKey());
    sendMessageFromMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(RequestObjectInsuranceEvent event) {
    final String message = String.format(
        "Ny medlem signerad! Medlemmen har id %s och har någon pryl som är dyrare än 50':-. Produkttypen är %s",
        event.getMemberId(), event.getProductType());
    sendNewMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(RequestStudentObjectInsuranceEvent event) {
    val message = String.format(
        "Ny studentmedlem signerad! Medlemmen har id %s och har någon pryl som är dyrare än 25':-. Produkttypen är %s",
        event.getMemberId(), event.getProductType());
    sendNewMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(MemberSignedEvent event) {
    val message = String.format("Ny medlem signerad! Medlemmen har id %s och produkttypen är %s",
        event.getMemberId(), event.getProductType());
    sendNewMemberNotification(message, "CallMe");
  }

  @EventListener
  public void on(ClaimCallMeEvent event) {
    final String message = String.format(
        "Medlem %s(%s %s) med %s försäkring har fått en skada och vill bli uppringd på %s",
        event.getMemberId(), event.getFirstName(), event.getFamilyName(),
        event.isInsuranceActive() ? "AKTIV" : "INAKTIV", event.getPhoneNumber());
    sendNewClaimNotification(message, "CallMe");
  }

  private void sendNewMemberNotification(String message, String subject) {
    try {
      template.sendNotification("newMembers", message, subject);
    } catch (Exception ex) {
      log.error("Could not send SNS-notification", ex);
    }
  }

  private void sendMessageFromMemberNotification(String message, String subject) {
    try {
      template.sendNotification("newMessages", message, subject);
    } catch (Exception ex) {
      log.error("Could not send SNS-notification", ex);
    }
  }

  private void sendNewClaimNotification(String message, String subject) {
    try {
      template.sendNotification("newClaims", message, subject);
    } catch (Exception ex) {
      log.error("Could not send SNS-notification", ex);
    }
  }
}
