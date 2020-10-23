package com.hedvig.botService.serviceIntegration.productPricing;

import com.hedvig.botService.serviceIntegration.productPricing.dto.AppleInitializationRequest;
import com.hedvig.botService.web.dto.InsuranceStatusDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ProductPricingService {

  private final ProductPricingClient productPricingClient;
  private static Logger log = LoggerFactory.getLogger(ProductPricingService.class);

  @Autowired
  ProductPricingService(ProductPricingClient productPricingClient) {

    this.productPricingClient = productPricingClient;
  }

  public void initAppleProduct(String appleMemberId) {
    try {
      this.productPricingClient.initAppleProduct(new AppleInitializationRequest(appleMemberId));
    } catch (FeignException | RestClientException ex) {
      log.error("Cannot init apple product with memberId: {}", appleMemberId);
    }
  }
}
