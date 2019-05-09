package com.hedvig.botService.web;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.UserContextService;
import com.hedvig.botService.web.dto.UpdateUserContextDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("i")
public class AppleController {

  @Value("${hedvig.appleUser.memberId}")
  private String APPLE_USER_MEMBER_ID;
  @Value("${hedvig.appleUser.personalNumber}")
  private String APPLE_USER_PERSONAL_NUMBER;
  @Value("${hedvig.appleUser.firstName}")
  private String APPLE_USER_FIRST_NAME;
  @Value("${hedvig.appleUser.lastName}")
  private String APPLE_USER_LAST_NAME;
  @Value("${hedvig.appleUser.address.street}")
  private String APPLE_USER_ADDRESS_STREET;
  @Value("${hedvig.appleUser.address.city}")
  private String APPLE_USER_ADDRESS_CITY;
  @Value("${hedvig.appleUser.address.zipCode}")
  private String APPLE_USER_ADDRESS_ZIP_CODE;
  @Value("${hedvig.appleUser.phoneNumber}")
  private String APPLE_USER_PHONE_NUMBER;
  @Value("${hedvig.appleUser.email}")
  private String APPLE_USER_EMAIL;

  private final MemberService memberService;
  private final ProductPricingService productPricingService;
  private final UserContextService userContextService;

  @Autowired
  public AppleController(
    MemberService memberService,
    ProductPricingService productPricingService, UserContextService userContextService) {
    this.memberService = memberService;
    this.productPricingService = productPricingService;
    this.userContextService = userContextService;
  }

  @PostMapping("initAppleUser")
  ResponseEntity<?> initializeAppleUser() {
    log.info("Initializing Apple User, in memory of Steve Jobs!");

    Optional<UserContext> userContextMaybe = userContextService.findByMemberId(APPLE_USER_MEMBER_ID);

    if (userContextMaybe.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    memberService.initAppleUser(APPLE_USER_MEMBER_ID);

    productPricingService.initAppleProduct(APPLE_USER_MEMBER_ID);

    UserContext appleUserContext = new UserContext(APPLE_USER_MEMBER_ID);
    userContextService.save(appleUserContext);

    UpdateUserContextDTO updateUserContextDTO = new UpdateUserContextDTO(
      APPLE_USER_MEMBER_ID,
      APPLE_USER_PERSONAL_NUMBER,
      APPLE_USER_FIRST_NAME,
      APPLE_USER_LAST_NAME,
      APPLE_USER_PHONE_NUMBER,
      APPLE_USER_EMAIL,
      APPLE_USER_ADDRESS_STREET,
      APPLE_USER_ADDRESS_CITY,
      APPLE_USER_ADDRESS_ZIP_CODE,
      true
    );

    appleUserContext.updateUserContextWebOnboarding(updateUserContextDTO);

    return ResponseEntity.noContent().build();
  }
}
