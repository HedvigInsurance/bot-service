package com.hedvig.botService.web;

import com.hedvig.botService.enteties.MessageRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.services.SessionManager;
import com.hedvig.botService.web.dto.AddMessageRequestDTO;
import com.hedvig.botService.web.dto.BackOfficeAnswerDTO;
import com.hedvig.botService.web.dto.BackOfficeMessageDTO;
import com.hedvig.botService.web.dto.ExpoDeviceInfoDTO;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class InternalMessagesController {

  private final SessionManager sessionManager;

  private final MessageRepository messageRepository;

  @Autowired
  public InternalMessagesController(SessionManager sessions, MessageRepository messageRepository) {
    this.sessionManager = sessions;
    this.messageRepository = messageRepository;
  }

  /** This endpoint is used internally to send messages from back-office personnel to end users */
  @RequestMapping(path = {"/_/messages/addmessage", "/addmessage"}, method = RequestMethod.POST,
      produces = "application/json; charset=utf-8")
  public ResponseEntity<?> addMessage(@Valid @RequestBody AddMessageRequestDTO backOfficeMessage, @RequestHeader("Authorization") String token) {
    log.info("Message from Hedvig to hid: {} with messageId: {}", backOfficeMessage.getMemberId(), backOfficeMessage.getMsg());

    backOfficeMessage.setUserId(token);
    if (sessionManager.addMessageFromHedvig(backOfficeMessage)) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  /**
   * This endpoint is used internally to question answers from back-office personnel to end users
   */
  @RequestMapping(path = "/_/messages/addanswer", method = RequestMethod.POST,
      produces = "application/json; charset=utf-8")
  public ResponseEntity<?> addAnswer(@Valid @RequestBody() BackOfficeAnswerDTO backOfficeAnswer, @RequestHeader("Authorization") String token) {
    log.info("Received answer from Hedvig to hid: {} with message {}", backOfficeAnswer.getMemberId(), backOfficeAnswer.getMsg());

    backOfficeAnswer.setUserId(token);
    if (sessionManager.addAnswerFromHedvig(backOfficeAnswer)) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @RequestMapping(path = "/_/messages/{from}", method = RequestMethod.GET)
  public List<BackOfficeMessageDTO> messages(@PathVariable Long from) {
    Instant timestamp = Instant.ofEpochMilli(from);
    List<Message> messages = messageRepository.findFromTimestamp(timestamp);

    return messages.stream().map(m -> new BackOfficeMessageDTO(m, m.chat.getMemberId()))
        .collect(Collectors.toList());
  }

  /**
   * Initialize chat with member. The method is used in api-gateway "/helloHedvig" method handler.
   */
  @RequestMapping(path = {"/_/messages/init", "/init"}, method = RequestMethod.POST)
  public ResponseEntity<?> create(
      @RequestHeader(value = "hedvig.token", required = false) String hid,
      @RequestBody(required = false) ExpoDeviceInfoDTO json) {
    log.info("Init recieved from api-gateway: {}", hid);

    String linkUri = "hedvig://+";
    if (json != null && json.getDeviceInfo() != null) {
      log.info(json.toString());

      final String clientLinkingUri = json.getDeviceInfo().getLinkingUri();
      if (clientLinkingUri != null && clientLinkingUri.contains("://")) {
        linkUri = clientLinkingUri;
      }
    }
    sessionManager.init(hid, linkUri);

    return ResponseEntity.noContent().build();
  }
}
