package com.hedvig.botService.chat;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.serviceIntegration.claimsService.ClaimsService;
import com.hedvig.botService.serviceIntegration.lookupService.LookupService;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.LocalizationService;
import com.hedvig.botService.services.triggerService.TriggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collection;

import static com.hedvig.botService.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(Parameterized.class)
public class ConversationFactoryTest {

  private final Class<?> conversationClass;
  @Mock ClaimsService claimsService;
  @Mock ApplicationEventPublisher applicationEventPublisher;
  @Mock StatusBuilder statusBuilder;
  @Mock Environment springEnvironment;
  @Mock private MemberService memberService;
  @Mock private LookupService lookupService;
  @Mock private ProductPricingService productPricingService;
  @Mock private TriggerService triggerService;
  @Mock private LocalizationService localizationService;
  @Mock private PhoneNumberUtil phoneNumberUtil;

  public ConversationFactoryTest(Class<?> conversationClass) {
    this.conversationClass = conversationClass;
  }

  @Parameterized.Parameters
  public static Collection<Object> data() {
    return Arrays.asList(
        new Object[] {
          TrustlyConversation.class,
          ClaimsConversation.class,
          CharityConversation.class,
          MainConversation.class,
          OnboardingConversationDevi.class,
          MemberSourceConversation.class
        });
  }

  @Before
  public void setUp() {
    springEnvironment = Mockito.mock(Environment.class);
    MockitoAnnotations.initMocks(this);
    given(springEnvironment.acceptsProfiles("development")).willReturn(true);
  }

  @Test
  public void test() {
    ConversationFactory factory =
        new ConversationFactoryImpl(
            memberService,
            lookupService,
            productPricingService,
            triggerService,
            applicationEventPublisher,
            claimsService,
            statusBuilder,
            localizationService,
            0,
          "Test",
          "Test",
          phoneNumberUtil);

    Conversation conversation = factory.createConversation(conversationClass, new UserContext(TOLVANSSON_MEMBER_ID));

    assertThat(conversation).isNotNull();
    assertThat(conversation).isInstanceOf(conversationClass);
  }
}
