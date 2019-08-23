package com.hedvig.botService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

@Configuration
public class PhoneNumberConfig {

  @Bean(name = "phoneNumberUtil")
  public PhoneNumberUtil phoneNumberUtil() {
    return PhoneNumberUtil.getInstance();
  }
}
