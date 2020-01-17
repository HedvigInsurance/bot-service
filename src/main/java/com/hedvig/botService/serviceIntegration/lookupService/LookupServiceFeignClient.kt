package com.hedvig.botService.serviceIntegration.lookupService

import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateDto
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateResponse
import org.springframework.cloud.openfeign.FeignClient

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "lookupServiceClient",
    url = "\${hedvig.lookup-service.url:lookup-service}",
    configuration = [LookupFeignConfiguration::class])
interface LookupServiceFeignClient {

    @RequestMapping(value = ["_/lookup/visma/realestate"], method = [RequestMethod.POST])
    fun realEstateLookup(@RequestParam("memberId") memberId: String, @RequestBody request: RealEstateDto): ResponseEntity<RealEstateResponse>
}
