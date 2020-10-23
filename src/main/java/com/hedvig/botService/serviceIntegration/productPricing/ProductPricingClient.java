package com.hedvig.botService.serviceIntegration.productPricing;

import com.hedvig.botService.serviceIntegration.productPricing.dto.AppleInitializationRequest;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Headers("Accept: application/xml")
@FeignClient(name = "productPricingClient", url = "${hedvig.product-pricing.url:product-pricing}")
public interface ProductPricingClient {
  @RequestMapping(value = "/i/insurance/initiateAppleProduct")
  ResponseEntity<Void> initAppleProduct(@RequestBody AppleInitializationRequest request);
}
