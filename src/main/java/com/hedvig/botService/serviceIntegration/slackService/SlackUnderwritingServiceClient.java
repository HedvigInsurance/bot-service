package com.hedvig.botService.serviceIntegration.slackService;

import com.hedvig.botService.config.FeignConfiguration;
import com.hedvig.botService.serviceIntegration.slackService.dto.SlackData;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Headers("Accept: application/xml")
@Profile("slack")
@FeignClient(
  name = "SlackUnderwritingServiceClient",
  url = "${hedvig.slack.underwriting.url}",
  configuration = FeignConfiguration.class
)

public interface SlackUnderwritingServiceClient {
  @RequestMapping(
    method = RequestMethod.POST,
    produces = "application/json")
  ResponseEntity<String> sendNotification(@RequestBody SlackData req);
}

