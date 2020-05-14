package com.hedvig.common.localization

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "lokalise")
class LokaliseConfigurationProperties {
    lateinit var projectId: String
    lateinit var apiToken: String
}
