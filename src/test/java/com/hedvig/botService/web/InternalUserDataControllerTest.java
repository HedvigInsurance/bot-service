package com.hedvig.botService.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.botService.BotServiceApplicationTests;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.services.SessionManager;
import com.hedvig.botService.services.UserContextService;
import com.hedvig.botService.web.dto.EditMemberNameRequestDTO;
import com.hedvig.botService.web.dto.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.hedvig.botService.testHelpers.TestData.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MockMvc;


@ContextConfiguration(classes = BotServiceApplicationTests.class)
@WebMvcTest(controllers = InternalUserDataController.class)
@RunWith(SpringRunner.class)
public class InternalUserDataControllerTest {

  @MockBean
  private SessionManager sessionManager;

  @MockBean
  private UserContextService userContextService;

  @MockBean
  private UserContextRepository userContextRepository;

  @MockBean
  private UserContext userContext;

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void returnsResponseOkIfFirstAndLastNameAreNotNull() throws Exception {

    Member member = new Member(
      Long.parseLong(TOLVANSSON_MEMBER_ID),
      TOLVANSSON_SSN,
      TOLVANSSON_FIRSTNAME,
      TOLVANSSON_LASTNAME,
      TOLVANSSON_STREET,
      TOLVANSSON_CITY,
      TOLVANSSON_ZIP,
      TOLVANSSON_FLOOR,
      TOLVANSSON_EMAIL,
      TOLVANSSON_PHONE_NUMBER,
      "SWEDEN",
      TOLVANSSON_BIRTH_DATE,
      "1203"
    );

    ObjectMapper jsonMapper = new ObjectMapper();

    EditMemberNameRequestDTO editMemberNameRequestDTO = new EditMemberNameRequestDTO(
      Long.toString(member.getMemberId()),
      "Jon",
      "Doe"
    );

    mockMvc
      .perform(
        post("/_/member/{memberId}/editMemberName", member.getMemberId())
          .contentType(MediaType.APPLICATION_JSON_UTF8)
          .content(jsonMapper.writeValueAsBytes(editMemberNameRequestDTO))
      )
      .andExpect(status().isOk());

    verify(userContextService).editMemberName(eq(Long.toString(member.getMemberId())), any());
  }

  @Test
  public void returnsBadRequestStatusIfFirstNameOrLastNameIsNull() throws Exception {

    Member member = new Member(
      Long.parseLong(TOLVANSSON_MEMBER_ID),
      TOLVANSSON_SSN,
      TOLVANSSON_FIRSTNAME,
      TOLVANSSON_LASTNAME,
      TOLVANSSON_STREET,
      TOLVANSSON_CITY,
      TOLVANSSON_ZIP,
      TOLVANSSON_FLOOR,
      TOLVANSSON_EMAIL,
      TOLVANSSON_PHONE_NUMBER,
      "SWEDEN",
      TOLVANSSON_BIRTH_DATE,
      "1203"
    );

    ObjectMapper jsonMapper = new ObjectMapper();

    EditMemberNameRequestDTO editMemberNameRequestDTO = new EditMemberNameRequestDTO(
      Long.toString(member.getMemberId()),
      null,
      "Doe"
    );

    mockMvc
      .perform(
        post("/_/member/{memberId}/editMemberName", member.getMemberId())
          .contentType(MediaType.APPLICATION_JSON_UTF8)
          .content(jsonMapper.writeValueAsBytes(editMemberNameRequestDTO))
      )
      .andExpect(status().isBadRequest());

    verify(userContextService, times(0)).editMemberName(eq(Long.toString(member.getMemberId())), any());
  }
}
