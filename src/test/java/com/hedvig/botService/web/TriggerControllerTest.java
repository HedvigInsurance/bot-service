package com.hedvig.botService.web;

import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.botService.testHelpers.TestData.toJson;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hedvig.botService.BotServiceApplicationTests;
import com.hedvig.botService.enteties.DirectDebitMandateTrigger;
import com.hedvig.botService.services.exceptions.UnauthorizedException;
import com.hedvig.botService.services.triggerService.TriggerService;
import com.hedvig.botService.services.triggerService.dto.CreateDirectDebitMandateDTO;
import com.hedvig.botService.testHelpers.TestData;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TriggerController.class)
@ActiveProfiles("development")
@ContextConfiguration(classes = BotServiceApplicationTests.class)
@TestPropertySource(
    properties = {"hedvig.trigger.errorPageUrl=" + TriggerControllerTest.ERROR_PAGE_URL})
public class TriggerControllerTest {

  static final String ERROR_PAGE_URL = "https://hedvig.com/error-redirect";
  @MockBean private TriggerService triggerService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void GIVEN_triggerIdThatDoesNotBelongToMember_THEN_trigger_WILL_return_404()
      throws Exception {
    final UUID triggerId = UUID.randomUUID();

    //noinspection unchecked
    given(triggerService.getTriggerUrl(triggerId, TestData.TOLVANSSON_MEMBER_ID))
        .willThrow(UnauthorizedException.class);

    mockMvc
        .perform(
            post("/hedvig/trigger/" + triggerId.toString())
                .header("hedvig.token", TestData.TOLVANSSON_MEMBER_ID))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void GIVEN_validTriggerId_THEN_trigger_WILL_return_triggerURL() throws Exception {
    final UUID triggerId = UUID.randomUUID();
    final String triggerURL = "http://localhost:8080";

    given(triggerService.getTriggerUrl(triggerId, TestData.TOLVANSSON_MEMBER_ID))
        .willReturn(triggerURL);

    mockMvc
        .perform(
            post("/hedvig/trigger/" + triggerId.toString())
                .header("hedvig.token", TestData.TOLVANSSON_MEMBER_ID))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            jsonPath("$.url")
                .value(triggerURL + "&gui=native&color=%23651EFF&bordercolor=%230F007A"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
  }

  @Test
  public void GIVEN_noTriggerId_trigger_WILL_return_404() throws Exception {
    final UUID triggerId = UUID.randomUUID();

    given(triggerService.getTriggerUrl(triggerId, TestData.TOLVANSSON_MEMBER_ID)).willReturn(null);

    mockMvc
        .perform(
            post("/hedvig/trigger/" + triggerId.toString())
                .header("hedvig.token", TestData.TOLVANSSON_MEMBER_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  public void GIVEN_invalidUUID_THEN_trigger_WILL_return400() throws Exception {

    mockMvc
        .perform(
            post("/hedvig/trigger/" + "blablabla")
                .header("hedvig.token", TestData.TOLVANSSON_MEMBER_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void GIVEN_developmentProfile_THEN_createDDMWorks() throws Exception {
    final UUID triggerId = UUID.randomUUID();

    CreateDirectDebitMandateDTO createDirectDebitMandateDTO =
        new CreateDirectDebitMandateDTO(
            TestData.TOLVANSSON_SSN,
            TestData.TOLVANSSON_FIRSTNAME,
            TestData.TOLVANSSON_LASTNAME,
            TestData.TOLVANSSON_EMAIL);

    given(
            triggerService.createTrustlyDirectDebitMandate(
                createDirectDebitMandateDTO, TOLVANSSON_MEMBER_ID))
        .willReturn(triggerId);

    mockMvc
        .perform(
            post("/hedvig/trigger/_/createDDM")
                .header("hedvig.token", TestData.TOLVANSSON_MEMBER_ID)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(toJson(createDirectDebitMandateDTO)))
        .andExpect(jsonPath("$.id").value(triggerId.toString()));
  }

  @Test
  public void GIVEN_notificationReceived_THEN_notification_WILL_RedirectToURL() throws Exception {
    final UUID triggerId = UUID.randomUUID();

    final String SUCCESS_URL = "https://hedvig.com/notification-success";
    given(
            triggerService.clientNotificationReceived(
                triggerId, DirectDebitMandateTrigger.TriggerStatus.SUCCESS))
        .willReturn(SUCCESS_URL);

    mockMvc
        .perform(get("/hedvig/trigger/notification?status=SUCCESS&triggerId=" + triggerId))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("location", SUCCESS_URL));
  }

  @Test
  public void GIVEN_triggerService_throwsException_THEN_notification_willRedirectToErrorPage()
      throws Exception {
    final UUID triggerId = UUID.randomUUID();

    given(
            triggerService.clientNotificationReceived(
                triggerId, DirectDebitMandateTrigger.TriggerStatus.SUCCESS))
        .willThrow(RuntimeException.class);

    mockMvc
        .perform(get("/hedvig/trigger/notification?status=SUCCESS&triggerId=" + triggerId))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("location", ERROR_PAGE_URL));
  }
}
