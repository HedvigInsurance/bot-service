package com.hedvig.botService.serviceIntegration.slackService;

import com.hedvig.botService.serviceIntegration.slackService.dto.SlackData;
import com.hedvig.botService.services.events.OnboardingCallForQuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("slack")
public class SlackService {
  private final SlackServiceClient slackServiceClient;
  private static Logger logger = LoggerFactory.getLogger(SlackService.class);

  @Autowired
  SlackService(SlackServiceClient slackServiceClient) {
    this.slackServiceClient = slackServiceClient;
  }

  @EventListener
  public void on(OnboardingCallForQuoteEvent event) {
    String SLACK_CHANNEL = "#call_member_for_quote";

    final String message = String.format(
      "Potential member %s - %s %s tried to sign-up, give them a call on %s",
      event.getMemberId(), event.getFirstName(), event.getLastName() != null ? event.getLastName() : "", event.getPhoneNumber());

    try {
      SlackData slackData = new SlackData();
      slackData.setChannel(SLACK_CHANNEL);
      slackData.setText(message);

      slackServiceClient.sendNotification(slackData);
    } catch(Exception e) {
      logger.error("Cannot send notification to slack channel {} with message {}", SLACK_CHANNEL, message);
    }
  }
}
