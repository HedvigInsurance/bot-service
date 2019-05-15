package com.hedvig.botService.services.triggerService;

import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.enteties.DirectDebitRepository;
import com.hedvig.botService.serviceIntegration.paymentService.PaymentService;
import com.hedvig.botService.serviceIntegration.paymentService.dto.DirectDebitResponse;
import com.hedvig.botService.serviceIntegration.paymentService.dto.OrderInformation;
import com.hedvig.botService.serviceIntegration.paymentService.dto.OrderState;
import com.hedvig.botService.services.exceptions.UnauthorizedException;
import com.hedvig.botService.services.triggerService.dto.CreateDirectDebitMandateDTO;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class TriggerService {

  @Value("${hedvig.trustly.notification.successUrl}")
  public String NOTIFICATION_SUCCESS_URL;

  @Value("${hedvig.trustly.notification.errorUrl}")
  public String NOTIFICATION_ERROR_URL;

  private final DirectDebitRepository repo;
  private final PaymentService paymentService;

  public TriggerService(DirectDebitRepository repo, PaymentService paymentService) {
    this.repo = repo;
    this.paymentService = paymentService;
  }

  public UUID createTrustlyDirectDebitMandate(CreateDirectDebitMandateDTO data, String memberId) {

    DirectDebitMandateTrigger mandate = new DirectDebitMandateTrigger();
    mandate.setEmail(data.getEmail());
    mandate.setFirstName(data.getFirstName());
    mandate.setLastName(data.getLastName());
    mandate.setSsn(data.getSsn());
    mandate.setMemberId(memberId);
    repo.save(mandate);

    return mandate.getId();
  }

  public UUID createTrustlyDirectDebitMandate(
      String ssn, String firstName, String lastName, String email, String memberId) {

    CreateDirectDebitMandateDTO mandate =
        new CreateDirectDebitMandateDTO(ssn, firstName, lastName, email);

    return createTrustlyDirectDebitMandate(mandate, memberId);
  }

  public String getTriggerUrl(UUID triggerId, String memberId) {

    final DirectDebitMandateTrigger one = repo.findOne(triggerId);
    if (!one.getMemberId().equals(memberId)) {
      throw new UnauthorizedException("No allowed to access trigger");
    }

    if (one.getUrl() == null) {
      final DirectDebitResponse directDebitResponse =
          paymentService.registerTrustlyDirectDebit(
              one.getFirstName(), one.getLastName(), one.getSsn(), one.getMemberId(), triggerId);
      one.setUrl(directDebitResponse.getUrl());
      one.setOrderId(directDebitResponse.getOrderId());
      repo.save(one);
    }

    return one.getUrl();
  }

  public DirectDebitMandateTrigger.TriggerStatus getTrustlyOrderInformation(String triggerId) {

    final DirectDebitMandateTrigger trigger = repo.findOne(UUID.fromString(triggerId));

    if (trigger.getStatus() == null
        || trigger.getStatus() == DirectDebitMandateTrigger.TriggerStatus.IN_PROGRESS
        || trigger.getStatus() == DirectDebitMandateTrigger.TriggerStatus.CRATED) {

      OrderInformation trustlyOrderInformation =
          paymentService.getTrustlyOrderInformation(trigger.getOrderId());

      if (trustlyOrderInformation.getState() == OrderState.COMPLETE) {
        trigger.setStatus(DirectDebitMandateTrigger.TriggerStatus.SUCCESS);
      } else if (trustlyOrderInformation.getState() == OrderState.CANCELED) {
        trigger.setStatus(DirectDebitMandateTrigger.TriggerStatus.FAILED);
      } else {
        trigger.setStatus(DirectDebitMandateTrigger.TriggerStatus.IN_PROGRESS);
      }
    }

    repo.save(trigger);

    return trigger.getStatus();
  }

  public String clientNotificationReceived(
      UUID triggerId, DirectDebitMandateTrigger.TriggerStatus status) {
    DirectDebitMandateTrigger trigger = repo.findOne(triggerId);
    if (trigger.getStatus() == null
        || trigger.getStatus() == DirectDebitMandateTrigger.TriggerStatus.IN_PROGRESS
        || trigger.getStatus() == DirectDebitMandateTrigger.TriggerStatus.CRATED) {
      trigger.setStatus(status);
    }

    repo.save(trigger);

    if (status == DirectDebitMandateTrigger.TriggerStatus.SUCCESS) {
      return NOTIFICATION_SUCCESS_URL;
    }

    return NOTIFICATION_ERROR_URL;
  }
}
