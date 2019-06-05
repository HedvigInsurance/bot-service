package com.hedvig.botService.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.botService.BotServiceApplicationTests;
import com.hedvig.botService.enteties.MessageRepository;
import com.hedvig.botService.services.SessionManager;
import com.hedvig.botService.web.dto.AddMessageRequestDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.hedvig.botService.services.TriggerServiceTest.TOLVANSSON_MEMBERID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = InternalMessagesController.class)
@ContextConfiguration(classes = BotServiceApplicationTests.class)
public class InternalMessagesControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean private SessionManager sessionManager;
  @MockBean private MessageRepository messageRepository;

  @Before
  public void setUp() {}

  @Test
  public void givenConversationThatCanAcceptMessage_whenAddingMessage_shouldReturn204()
      throws Exception {

    when(sessionManager.addMessageFromHedvig(any())).thenReturn(true);

    ResultActions perform =
        mockMvc.perform(
            post("/_/messages/addmessage")
                .header("Authorization", "123456")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(
                    objectMapper.writeValueAsBytes(
                        new AddMessageRequestDTO(TOLVANSSON_MEMBERID, "Hejsan!", false))));

    perform.andExpect(status().isNoContent());
  }

  @Test
  public void givenConversationThatCanNotAcceptMessage_whenAddingMessage_shouldReturn204()
      throws Exception {

    when(sessionManager.addMessageFromHedvig(any())).thenReturn(false);

    ResultActions perform =
        mockMvc.perform(
            post("/_/messages/addmessage")
                .header("Authorization", "123456")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(
                    objectMapper.writeValueAsBytes(
                        new AddMessageRequestDTO(TOLVANSSON_MEMBERID, "Hejsan!", false))));

    perform.andExpect(status().isForbidden());
  }
}
