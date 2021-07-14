package com.hedvig.botService.services;

import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_FIRSTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_LASTNAME;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PHONE_NUMBER;
import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_PRODUCT_TYPE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.slack.SlackClient;
import com.hedvig.botService.serviceIntegration.ticketService.TicketService;
import com.hedvig.botService.services.events.ClaimAudioReceivedEvent;
import com.hedvig.botService.services.events.ClaimCallMeEvent;
import com.hedvig.botService.services.events.FileUploadedEvent;
import com.hedvig.botService.services.events.OnboardingFileUploadedEvent;
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent;
import com.hedvig.botService.services.events.QuestionAskedEvent;
import com.hedvig.botService.services.events.RequestObjectInsuranceEvent;
import com.hedvig.botService.services.events.RequestPhoneCallEvent;
import com.hedvig.botService.services.events.UnderwritingLimitExcededEvent;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {


  private static final String UPLOAD_KEY = "UPLOAD_KEY";
  private static final String UPLOAD_TYPE = "UPLOAD_TYPE";
  private static final String GOOD_QUESTION = "A long and good question";
  @Mock
  private SlackClient slackClient;
  @Mock
  private TicketService ticketService;
  @Mock
  private ClaimsService claimsService;
  private NotificationService notificationService;

  @Before
  public void setup() {
    notificationService = new NotificationService(slackClient, ticketService, claimsService);
  }

  @Test
  public void RequestPhoneCall_SendsEventThatContains_PhoneNumerMemberId() {

    RequestPhoneCallEvent event =
      new RequestPhoneCallEvent(
        TOLVANSSON_MEMBER_ID,
        TOLVANSSON_PHONE_NUMBER,
        TOLVANSSON_FIRSTNAME,
        TOLVANSSON_LASTNAME);
    notificationService.on(event);

    verify(slackClient, times(2)).post(any());
  }

  @Test
  public void UnderwritinglimitExcededEcent_SendsEventThatContains_PhoneNumber() {
    UnderwritingLimitExcededEvent event =
      new UnderwritingLimitExcededEvent(
        TOLVANSSON_MEMBER_ID,
        TOLVANSSON_PHONE_NUMBER,
        TOLVANSSON_FIRSTNAME,
        TOLVANSSON_LASTNAME,
        UnderwritingLimitExcededEvent.UnderwritingType.HouseingSize);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void OnboardingQuestionAskedEvent_SendsEventThatContains_MemberId() {
    OnboardingQuestionAskedEvent event =
      new OnboardingQuestionAskedEvent(TOLVANSSON_MEMBER_ID, GOOD_QUESTION);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void ClaimAudioReceivedEvent_SendEventThatContains_MembeId() {
    ClaimAudioReceivedEvent event = new ClaimAudioReceivedEvent(TOLVANSSON_MEMBER_ID);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void ClaimCallMeEventWithActiveInsurace_SendsEventThatContains_MemberId_InsuranceStatus() {
    ClaimCallMeEvent event =
      new ClaimCallMeEvent(
        TOLVANSSON_MEMBER_ID,
        TOLVANSSON_FIRSTNAME,
        TOLVANSSON_LASTNAME,
        TOLVANSSON_PHONE_NUMBER,
        true);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void
  ClaimCallMeEventWithInactiveInsurace_SendsEventThatContains_PhoneNumber_InsuranceStatus() {
    ClaimCallMeEvent event =
      new ClaimCallMeEvent(
        TOLVANSSON_MEMBER_ID,
        TOLVANSSON_FIRSTNAME,
        TOLVANSSON_LASTNAME,
        TOLVANSSON_PHONE_NUMBER,
        false);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void QuestionAskedEvent_SendsEventThatContains_MemberId() {
    QuestionAskedEvent event = new QuestionAskedEvent(TOLVANSSON_MEMBER_ID, GOOD_QUESTION);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void RequestObjectInsuranceEvent_SendsEventThatContains_MemberId() {
    RequestObjectInsuranceEvent event =
      new RequestObjectInsuranceEvent(TOLVANSSON_MEMBER_ID, TOLVANSSON_PRODUCT_TYPE);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());
  }

  @Test
  public void OnboardingFileUploadedEvent_SendsEventThatContains_MemberIdAndKeyAndType() {
    val event = new OnboardingFileUploadedEvent(TOLVANSSON_MEMBER_ID, UPLOAD_KEY, UPLOAD_TYPE);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());

  }

  @Test
  public void FileUploadedEvent_SendsEventThatContains_MemberIdAndKeyAndType() {
    val event = new FileUploadedEvent(TOLVANSSON_MEMBER_ID, UPLOAD_KEY, UPLOAD_TYPE);

    notificationService.on(event);

    then(slackClient)
      .should()
      .post(any());

  }
}
