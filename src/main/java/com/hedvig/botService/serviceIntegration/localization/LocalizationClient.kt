package com.hedvig.botService.serviceIntegration.localization

import com.hedvig.botService.enteties.localization.LocalizationResponse
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(value = "localizationClient", url = "\${graphcms.url}")
interface LocalizationClient {

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"])
    fun fetchLocalization(@RequestBody query: GraphQLQueryWrapper): LocalizationResponse
}

