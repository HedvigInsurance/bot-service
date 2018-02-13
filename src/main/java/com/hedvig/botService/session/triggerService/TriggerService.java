package com.hedvig.botService.session.triggerService;

import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.enteties.DirectDebitRepository;
import com.hedvig.botService.serviceIntegration.paymentService.PaymentService;
import com.hedvig.botService.serviceIntegration.paymentService.dto.UrlResponse;
import com.hedvig.botService.session.exceptions.UnathorizedException;
import com.hedvig.botService.session.triggerService.dto.CreateDirectDebitMandateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@Service
public class TriggerService {


    private final DirectDebitRepository repo;
    private final PaymentService paymentService;

    public TriggerService(DirectDebitRepository repo, PaymentService paymentService) {
        this.repo = repo;
        this.paymentService = paymentService;
    }

    public UUID createDirectDebitMandate(CreateDirectDebitMandateDTO data, String memberId) {

        DirectDebitMandateTrigger mandate = new DirectDebitMandateTrigger();
        mandate.setEmail(data.getEmail());
        mandate.setFirstName(data.getFirstName());
        mandate.setLastName(data.getLastName());
        mandate.setSsn(data.getSsn());
        mandate.setMemberId(memberId);
        repo.save(mandate);

        return mandate.getId();
    }

    public UUID createDirectDebitMandate(String ssn, String firstName, String lastName, String email, String memberId) {

        CreateDirectDebitMandateDTO mandate = new CreateDirectDebitMandateDTO(
                ssn,
                firstName,
                lastName,
                email
        );

        return createDirectDebitMandate(mandate, memberId);
    }

    public String getTriggerUrl(UUID triggerId, String memberId) {

        final DirectDebitMandateTrigger one = repo.findOne(triggerId);
        if(!one.getMemberId().equals(memberId)) {
            throw new UnathorizedException("No allowed to access trigger");
        }

        if(one.getUrl() == null) {
            final UrlResponse urlResponse = paymentService.startPayment(one.getFirstName(), one.getLastName(), one.getSsn(), one.getEmail());
            one.setUrl(urlResponse.getUrl());
            repo.save(one);
        }

        return one.getUrl();
    }
}
