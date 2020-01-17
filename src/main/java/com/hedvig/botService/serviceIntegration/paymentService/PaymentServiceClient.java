package com.hedvig.botService.serviceIntegration.paymentService;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.hedvig.botService.serviceIntegration.paymentService.dto.DirectDebitRequest;
import com.hedvig.botService.serviceIntegration.paymentService.dto.DirectDebitResponse;
import com.hedvig.botService.serviceIntegration.paymentService.dto.OrderInformation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "payment-service", url = "${hedvig.payment-service.url:payment-service}")
public interface PaymentServiceClient {

  @RequestMapping(
      value = "/_/trustlyOrder/registerDirectDebit",
      method = POST,
      produces = "application/json")
  ResponseEntity<DirectDebitResponse> registerDirectDebit(
      @RequestBody DirectDebitRequest requestBody);

  @RequestMapping(value = "/_/trustlyOrder/{orderId}", method = GET)
  ResponseEntity<OrderInformation> orderInformation(@PathVariable("orderId") String orderId);
}
