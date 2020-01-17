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

  public String getInsuranceStatus(String hid) {
    ResponseEntity<InsuranceStatusDTO> isd = this.productPricingClient.getInsuranceStatus(hid);
    log.info("Getting insurance status: " + (isd == null ? null : isd.getStatusCodeValue()));
    if (isd != null) {
      return isd.getBody().getInsuranceStatus();
    }
    return null;
  }

  public Boolean isMemberInsuranceActive(final String memberId) {
    Boolean isActive = true;
    try {
      isActive = this.getInsuranceStatus(memberId).equals("ACTIVE");
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error(ex.getMessage());
      }
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }
    return isActive;
  }

  public void initAppleProduct(String appleMemberId) {
    try {
      this.productPricingClient.initAppleProduct(new AppleInitializationRequest(appleMemberId));
    } catch (FeignException | RestClientException ex) {
      log.error("Cannot init apple product with memberId: {}", appleMemberId);
    }
  }
}
