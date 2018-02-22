package com.hedvig.botService.web;

import static net.logstash.logback.argument.StructuredArguments.value;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hedvig.botService.enteties.MessageRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.session.SessionManager;

@RestController
@RequestMapping("/_/member/")
public class InternalUserDataController {

	private static Logger log = LoggerFactory.getLogger(InternalUserDataController.class);
	private final SessionManager sessionManager;
	private final MessageRepository messageRepository;

    @Autowired
    public InternalUserDataController(SessionManager sessions, MessageRepository messageRepository)
	{
		this.sessionManager = sessions;
		this.messageRepository = messageRepository;
    }

    @GetMapping(value = "{hid}/push-token", produces="application/json")
    ResponseEntity<String> pushToken(@PathVariable String hid){
        log.info("Get pushtoken for memberId:{}, is: {}", value("memberId", ""));
        String token = sessionManager.getPushToken(hid);
        return new ResponseEntity<String>(token ,HttpStatus.OK);
    }

}