package com.hedvig.botService.chat;

import java.time.Clock;
import java.util.Locale;

public interface StatusBuilder {
  String getStatusMessage(Clock c, Locale locale);
}
