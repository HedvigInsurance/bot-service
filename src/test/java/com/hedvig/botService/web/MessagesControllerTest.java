package com.hedvig.botService.web;

import static com.hedvig.botService.services.TriggerServiceTest.TOLVANSSON_MEMBERID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.hedvig.botService.BotServiceApplicationTests;
import com.hedvig.botService.services.SessionManager;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MessagesController.class)
@ContextConfiguration(classes = BotServiceApplicationTests.class)
public class MessagesControllerTest {

  @MockBean SessionManager sessionManager;

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Test
  public void allMessages() throws Exception {
    when(sessionManager.getAllMessages(contains(TOLVANSSON_MEMBERID), any()))
        .thenReturn(Lists.newArrayList());

    ResultActions resultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.get("/messages")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .header("hedvig.token", TOLVANSSON_MEMBERID));

    resultActions.andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$").isEmpty());
  }
}
