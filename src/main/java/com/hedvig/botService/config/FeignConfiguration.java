package com.hedvig.botService.config;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@EnableFeignClients(basePackages = "com.hedvig")
@Configuration
public class FeignConfiguration {

  @Bean
  public Request.Options opts(
      @Value("${feign.connectTimeoutMillis:1000}") int connectTimeoutMillis,
      @Value("${feign.readTimeoutMillis:3000}") int readTimeoutMillis) {
    return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
  }
}
