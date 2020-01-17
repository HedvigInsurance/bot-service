package com.hedvig.botService.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.enteties.DirectDebitRepository;
import com.hedvig.botService.serviceIntegration.paymentService.PaymentService;
import com.hedvig.botService.serviceIntegration.paymentService.dto.DirectDebitResponse;
import com.hedvig.botService.serviceIntegration.paymentService.dto.OrderInformation;
import com.hedvig.botService.serviceIntegration.paymentService.dto.OrderState;
import com.hedvig.botService.services.exceptions.UnauthorizedException;
import com.hedvig.botService.services.triggerService.TriggerService;
import com.hedvig.botService.services.triggerService.dto.CreateDirectDebitMandateDTO;

import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriggerServiceTest {

  public static final String TOLVANSSON_SSN = "19121212-1221";
  public static final String TOLVANSSON_FIRSTNAME = "Tolvan";
  public static final String TOLVANSSON_LAST_NAME = "Tolvansson";
  public static final String TOLVANSSON_EMAIL = "tolvan@tolvansson.se";
  public static final String TOLVANSSON_MEMBERID = "1337";
  public static final String TRIGGER_URL = "http://localhost:8080";
  public static final UUID ORDER_ID = UUID.randomUUID();
  public static final UUID TRIGGER_ID = UUID.randomUUID();
  public static final String IFRAME_URL = "https://trustly.com/iframeURL";
  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock DirectDebitRepository repo;
  @Mock PaymentService pService;
  @Captor ArgumentCaptor<DirectDebitMandateTrigger> mandateCaptor;
  private TriggerService sut;;
  private UUID generatedTriggerId;

  @Before
  public void setUp() {

    sut = new TriggerService(repo, pService);

    given(repo.save(any(DirectDebitMandateTrigger.class)))
        .will(
            x -> {
              DirectDebitMandateTrigger trigger = (DirectDebitMandateTrigger) x.getArgument(0);
              trigger.setId(generatedTriggerId);
              return trigger;
            });
  }

  @Test
  public void createDirecteDebitMandate_returns_triggerUUID() {
    // arrange

    generatedTriggerId = UUID.randomUUID();

    CreateDirectDebitMandateDTO requestData =
        directDebitMandateRequest(
            TOLVANSSON_SSN, TOLVANSSON_FIRSTNAME, TOLVANSSON_LAST_NAME, TOLVANSSON_EMAIL);

    // act
    UUID triggerId = sut.createTrustlyDirectDebitMandate(requestData, TOLVANSSON_MEMBERID);

    // assert
    assertThat(triggerId).isNotNull();
  }

  @Test
  public void createDirectDebitmandate_saves_DirectDebitMandate() {
    // arrange
    generatedTriggerId = UUID.randomUUID();

    CreateDirectDebitMandateDTO requestData =
        directDebitMandateRequest(
            TOLVANSSON_SSN, TOLVANSSON_FIRSTNAME, TOLVANSSON_LAST_NAME, TOLVANSSON_EMAIL);

    // act

    final UUID triggerURL = sut.createTrustlyDirectDebitMandate(requestData, TOLVANSSON_MEMBERID);

    // assert

    assertThat(triggerURL).isEqualByComparingTo(generatedTriggerId);
  }

  @Test
  public void getTriggerUrl_willCall_paymentService() {
    // arrange
    UUID triggerId = UUID.randomUUID();

    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(triggerId, null, TOLVANSSON_MEMBERID);
    given(repo.findById(triggerId)).willReturn(Optional.of(ddm));

    given(
            pService.registerTrustlyDirectDebit(
                TOLVANSSON_FIRSTNAME,
                TOLVANSSON_LAST_NAME,
                TOLVANSSON_SSN,
                TOLVANSSON_MEMBERID,
                triggerId))
        .willReturn(new DirectDebitResponse(TRIGGER_URL, ORDER_ID.toString()));

    // act

    final String actualTriggerUrl = sut.getTriggerUrl(triggerId, TOLVANSSON_MEMBERID);

    // assert
    assertThat(actualTriggerUrl).isEqualTo(TRIGGER_URL);
    then(repo).should().save(mandateCaptor.capture());
    assertThat(mandateCaptor.getValue().getUrl()).isEqualTo(TRIGGER_URL);
    assertThat(mandateCaptor.getValue().getOrderId()).isEqualTo(ORDER_ID.toString());
  }

  @Test
  public void getTriggerUrl_willNotCall_paymentService_WhenTriggerHasURL() {

    // arrange
    UUID triggerId = UUID.randomUUID();
    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(triggerId, TRIGGER_URL, TOLVANSSON_MEMBERID);

    given(repo.findById(triggerId)).willReturn(Optional.of(ddm));

    // act
    final String actualTriggerUrl = sut.getTriggerUrl(triggerId, TOLVANSSON_MEMBERID);

    // assert
    assertThat(actualTriggerUrl).isEqualTo(TRIGGER_URL);
    then(pService).should(never()).registerTrustlyDirectDebit(any(), any(), any(), any(), any());
  }

  @Test
  public void getTriggerUrl_willThrow_UnathorizedException_if_memberIdDoesNotMatch() {
    // arrange

    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(TRIGGER_ID, TRIGGER_URL, TOLVANSSON_MEMBERID);
    given(repo.findById(TRIGGER_ID)).willReturn(Optional.of(ddm));

    // act
    thrown.expect(UnauthorizedException.class);
    sut.getTriggerUrl(TRIGGER_ID, "1338");

    // assert

  }

  @Test
  public void
      GIVEN_directDebitTriggerWithStatusNull_THEN_getTrustlyOrderInformation_WILL_callPaymentService() {

    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(TRIGGER_ID, TRIGGER_URL, TOLVANSSON_MEMBERID);
    given(repo.findById(TRIGGER_ID)).willReturn(Optional.of(ddm));

    getTrustlyOrderInformationWillReturnGiven(OrderState.COMPLETE, ddm.getOrderId());

    final DirectDebitMandateTrigger.TriggerStatus trustlyOrderInformation =
        sut.getTrustlyOrderInformation(TRIGGER_ID.toString());

    assertThat(trustlyOrderInformation).isEqualTo(DirectDebitMandateTrigger.TriggerStatus.SUCCESS);
    assertThat(ddm.getStatus()).isEqualTo(DirectDebitMandateTrigger.TriggerStatus.SUCCESS);
  }

  public void getTrustlyOrderInformationWillReturnGiven(OrderState returns, String given) {
    given(this.pService.getTrustlyOrderInformation(given))
        .willReturn(new OrderInformation(ORDER_ID, IFRAME_URL, returns));
  }

  @Test
  public void
      GIVEN_directDebitTriggerWithStatusCOMPLETE_THEN_getTrustlyOrderInformation_WILL_NotCallPaymentService() {

    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(TRIGGER_ID, TRIGGER_URL, TOLVANSSON_MEMBERID);

    given(repo.findById(TRIGGER_ID)).willReturn(Optional.of(ddm));
    getTrustlyOrderInformationWillReturnGiven(OrderState.CANCELED, ddm.getOrderId());

    final DirectDebitMandateTrigger.TriggerStatus trustlyOrderInformation =
        sut.getTrustlyOrderInformation(TRIGGER_ID.toString());

    assertThat(trustlyOrderInformation).isEqualTo(DirectDebitMandateTrigger.TriggerStatus.FAILED);
    assertThat(ddm.getStatus()).isEqualTo(DirectDebitMandateTrigger.TriggerStatus.FAILED);
  }

  @Test
  public void
      GIVEN_getTrustlyOrderInformationCONFIRMED_THEN_directdebittriggerStatus_WILL_EQ_IN_PROGRESS() {

    DirectDebitMandateTrigger ddm =
        createDirectDebitMandateTrigger(TRIGGER_ID, TRIGGER_URL, TOLVANSSON_MEMBERID);

    given(repo.findById(TRIGGER_ID)).willReturn(Optional.of(ddm));
    getTrustlyOrderInformationWillReturnGiven(OrderState.CONFIRMED, ddm.getOrderId());

    final DirectDebitMandateTrigger.TriggerStatus trustlyOrderInformation =
        sut.getTrustlyOrderInformation(TRIGGER_ID.toString());

    assertThat(trustlyOrderInformation)
        .isEqualTo(DirectDebitMandateTrigger.TriggerStatus.IN_PROGRESS);
    assertThat(ddm.getStatus()).isEqualTo(DirectDebitMandateTrigger.TriggerStatus.IN_PROGRESS);
  }

  private CreateDirectDebitMandateDTO directDebitMandateRequest(
      String ssn, String firstName, String lastName, String email) {
    return new CreateDirectDebitMandateDTO(ssn, firstName, lastName, email);
  }

  private DirectDebitMandateTrigger createDirectDebitMandateTrigger(
      UUID triggerId, String triggerUrl, String tolvanssonMemberid) {
    DirectDebitMandateTrigger ddm = new DirectDebitMandateTrigger();
    ddm.setId(triggerId);
    ddm.setSsn(TOLVANSSON_SSN);
    ddm.setFirstName(TOLVANSSON_FIRSTNAME);
    ddm.setLastName(TOLVANSSON_LAST_NAME);
    ddm.setEmail(TOLVANSSON_EMAIL);
    ddm.setUrl(triggerUrl);
    ddm.setMemberId(tolvanssonMemberid);
    ddm.setOrderId(ORDER_ID.toString());
    return ddm;
  }
}
