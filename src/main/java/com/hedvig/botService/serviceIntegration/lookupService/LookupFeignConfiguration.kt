package com.hedvig.botService.serviceIntegration.lookupService

import feign.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class LookupFeignConfiguration {

    @Bean
    fun opts(
        @Value("\${feign.connectTimeoutMillis:1000}") connectTimeoutMillis: Int,
        @Value("\${feign.readTimeoutMillis:6000}") readTimeoutMillis: Int
    ): Request.Options {
        return Request.Options(connectTimeoutMillis, readTimeoutMillis)
    }
}
