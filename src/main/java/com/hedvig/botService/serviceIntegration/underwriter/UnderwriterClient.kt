package com.hedvig.botService.serviceIntegration.underwriter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    "underwriterClient",
    url = "\${hedvig.underwriter.url:underwriter}")
interface UnderwriterClient {

    @PostMapping("/_/v1/quotes")
    fun createQuote(@RequestBody requestDTO: QuoteRequestDto): CompleteQuoteResponseDto

}
