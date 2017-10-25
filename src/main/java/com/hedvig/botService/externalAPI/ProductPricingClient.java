package com.hedvig.botService.externalAPI;

import feign.Headers;
import feign.RequestLine;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;


@Headers("Accept: application/xml")
@FeignClient(name = "productPricingClient", url ="${hedvig.product-pricing.url}")
public interface ProductPricingClient {

    @RequestMapping(value = "/insurance/{user_id}/{status}", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<?> setInsuranceStatus(
    		@PathVariable("user_id") String userId,
    		@PathVariable("status") String status);

    /*@RequestMapping(value = "/v1/debtors/debtor", method = RequestMethod.POST, produces = "application/xml")
    Created createDebtor(@RequestHeader("Authorization") String token, @RequestBody String debtor);


    @RequestMapping(value = "/v1/bank/accounts/{id}", method =  RequestMethod.POST, produces = "application/xml")
    Created initiateBankAccountRetrieval(
            @RequestBody MultiValueMap<String, String> m,
            @RequestHeader("Authorization") String token,
            @PathVariable("id") String id,
            @RequestParam("bank") String bank,
            @RequestParam("ssn") String ssn
            );

    @RequestMapping(value = "/v1/bank/accounts/{id}", method = RequestMethod.GET, produces = "application/xml")
    ResponseEntity<BankAccountRequest> getBankAccountNumbers(@RequestHeader("Authorization") String token, @PathVariable("id") String publicId);*/


}
