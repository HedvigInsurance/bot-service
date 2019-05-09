package com.hedvig.botService.serviceIntegration.productPricing;

import com.hedvig.botService.serviceIntegration.productPricing.dto.AppleInitializationRequest;
import com.hedvig.botService.serviceIntegration.productPricing.dto.CalculateQuoteRequest;
import com.hedvig.botService.serviceIntegration.productPricing.dto.ContractSignedRequest;
import com.hedvig.botService.serviceIntegration.productPricing.dto.Created;
import feign.Headers;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Headers("Accept: application/xml")
@FeignClient(name = "productPricingClient", url = "${hedvig.product-pricing.url:product-pricing}")
public interface ProductPricingClient {

  @RequestMapping(
      value = "/insurance/{user_id}/{status}",
      method = RequestMethod.GET,
      produces = "application/json")
  ResponseEntity<?> setInsuranceStatus(
      @PathVariable("user_id") String userId, @PathVariable("status") String status);

  // @RequestMapping(value = "/insurance/member_id/", method = RequestMethod.POST, produces =
  // "application/json")
  // ResponseEntity<?> createProduct(@RequestBody CalculateQuoteRequest request);

  @RequestMapping(
      value = "/createProduct",
      method = RequestMethod.POST,
      produces = "application/json")
  ResponseEntity<Created> createQuote(@RequestBody CalculateQuoteRequest req);

  @RequestMapping(value = "/insurance/{userId}/quoteAccepted", method = RequestMethod.POST)
  ResponseEntity<String> quoteAccepted(@PathVariable("userId") String userId);

  @RequestMapping(value = "/_/insurance/contractSigned")
  ResponseEntity<String> contractSigned(@RequestBody ContractSignedRequest req);

  @RequestMapping(value = "/_/insurance/{memberId}/insurance", method = RequestMethod.GET)
  ResponseEntity<com.hedvig.botService.web.dto.InsuranceStatusDTO> getInsuranceStatus(
      @PathVariable("memberId") String memberId);

  @RequestMapping(value = "/i/insurance/initiateAppleProduct")
  ResponseEntity<Void> initAppleProduct(@RequestBody AppleInitializationRequest request);
}
