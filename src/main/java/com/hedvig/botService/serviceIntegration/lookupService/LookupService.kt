package com.hedvig.botService.serviceIntegration.lookupService

import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateDto
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class LookupService(
    val feignClient: LookupServiceFeignClient
) {

    fun realEstateLookup(memberId: String, realEstateDto: RealEstateDto): RealEstateResponse? =
        try {
            val response = feignClient.realEstateLookup(memberId, realEstateDto)
            response.body
        } catch (exception: RuntimeException) {
            log.error("Caught failed to look up real estate", exception)
            null
        }

    companion object {
        private val log = LoggerFactory.getLogger(LookupService::class.java)
    }
}
