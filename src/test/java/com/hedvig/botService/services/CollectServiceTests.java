package com.hedvig.botService.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hedvig.botService.chat.BankIdChat;
import com.hedvig.botService.enteties.BankIdSessionImpl;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.serviceIntegration.memberService.MemberAddress;
import com.hedvig.botService.serviceIntegration.memberService.MemberProfile;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdCollectResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdProgressStatus;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdStatusType;
import feign.FeignException;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import feign.Request;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringRunner.class)
public class CollectServiceTests {

  @MockBean private MemberService memberService;

  @MockBean private UserContextRepository userContextRepository;

  @MockBean private BankIdChat chat;

  @After
  public void reset_mocks() {
    Mockito.reset(userContextRepository);
    Mockito.reset(memberService);
    Mockito.reset(chat);
  }

  @Test
  public void testCollectError() {

    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    BankIdSessionImpl bankIdSessionImpl = new BankIdSessionImpl();
    bankIdSessionImpl.setLastCallTime(Instant.now());
    bankIdSessionImpl.setCollectionType(BankIdSessionImpl.CollectionType.AUTH);

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(
        new HashMap<String, BankIdSessionImpl>() {
          {
            put(referenceToken, bankIdSessionImpl);
          }
        });

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    BankIdCollectResponse response =
        new BankIdCollectResponse(
            BankIdProgressStatus.OUTSTANDING_TRANSACTION, referenceToken, null);
    when(memberService.collect(referenceToken, memberId)).thenReturn(response);

    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, null);

    BankIdCollectResponse expected =
        new BankIdCollectResponse(BankIdProgressStatus.OUTSTANDING_TRANSACTION, null, null);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void
      collect_Should_callBankIdGenericErrorANDReturnComplete_WHEN_userContextHasNoActiveBankIDSession() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(new HashMap<>());

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    verify(chat, times(1)).bankIdAuthGeneralCollectError();
    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.COMPLETE);
  }

  @Test
  public void collect_Should_ReturnComplete_WHEN_BankIDSessionISDone() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(new HashMap<>());
    userContext.startBankIdAuth(
        new BankIdAuthResponse(BankIdStatusType.COMPLETE, "", referenceToken));
    userContext.getBankIdCollectStatus(referenceToken).setDone();

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.COMPLETE);
  }

  @Test
  public void
      collect_Should_ReturnOutstandinTransaction_WHEN_CollectResponseIsOutstandingTransaction() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(new HashMap<>());
    userContext.startBankIdAuth(
        new BankIdAuthResponse(BankIdStatusType.STARTED, "", referenceToken));
    userContext
        .getBankIdCollectStatus(referenceToken)
        .setLastCallTime(Instant.now().minusSeconds(3));

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    when(memberService.collect(referenceToken, memberId))
        .thenReturn(
            new BankIdCollectResponse(
                BankIdProgressStatus.OUTSTANDING_TRANSACTION, referenceToken, null));

    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
  }

  @Test
  public void collect_CollectResponseIsComplete() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    // Arrange
    UserContext userContext = new UserContext();
    startAuth(userContext, referenceToken);
    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));
    when(memberService.collect(referenceToken, memberId))
        .thenReturn(
            new BankIdCollectResponse(BankIdProgressStatus.COMPLETE, referenceToken, memberId));
    when(memberService.getProfile(memberId)).thenReturn(newFakeMember(memberId));
    // Act
    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    // Assert
    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.COMPLETE);
    verify(chat, times(1)).bankIdAuthComplete();
    assertThat(userContext.getBankIdCollectStatus(referenceToken).getDone()).isTrue();
    assertThat(userContext.getBankIdCollectStatus(referenceToken).getLastStatus())
        .isEqualTo("COMPLETE");
  }

  @Test
  public void collect_CollectResponseIs_NOClient() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    // Arrange
    UserContext userContext = new UserContext();
    startAuth(userContext, referenceToken);
    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));
    when(memberService.collect(referenceToken, memberId))
        .thenReturn(
            new BankIdCollectResponse(BankIdProgressStatus.NO_CLIENT, referenceToken, memberId));

    // Act
    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    // Assert
    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.NO_CLIENT);
    verify(chat, times(1)).noClient();
    // assertThat(userContext.getBankIdCollectStatus(referenceToken).getDone()).isFalse();
    assertThat(userContext.getBankIdCollectStatus(referenceToken).getLastStatus())
        .isEqualTo("NO_CLIENT");
  }

  @Test
  public void collect_memberServiceGetProfile_throwsException() {
    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    // Arrange
    UserContext userContext = new UserContext();
    startAuth(userContext, referenceToken);
    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));
    when(memberService.collect(referenceToken, memberId))
        .thenReturn(
            new BankIdCollectResponse(BankIdProgressStatus.COMPLETE, referenceToken, memberId));
    when(memberService.getProfile(memberId)).thenThrow(new RestClientException("adsad"));
    // Act
    CollectService sut = new CollectService(userContextRepository, memberService);
    BankIdCollectResponse actual = sut.collect(memberId, referenceToken, chat);

    // Assert
    assertThat(actual.getBankIdStatus()).isEqualTo(BankIdProgressStatus.COMPLETE);
    verify(chat, times(1)).couldNotLoadMemberProfile();
    assertThat(userContext.getBankIdCollectStatus(referenceToken).getDone()).isTrue();
    assertThat(userContext.getBankIdCollectStatus(referenceToken).getLastStatus())
        .isEqualTo("COMPLETE");
  }

  private void startAuth(UserContext userContext, String referenceToken) {
    if (userContext.getBankIdStatus() == null) userContext.setBankIdStatus(new HashMap<>());

    userContext.startBankIdAuth(
        new BankIdAuthResponse(BankIdStatusType.STARTED, "", referenceToken));
    userContext
        .getBankIdCollectStatus(referenceToken)
        .setLastCallTime(Instant.now().minusSeconds(3));
  }

  private MemberProfile newFakeMember(String memberId) {

    MemberAddress address = new MemberAddress("Street", "City", "13345", "1111", 1);

    return new MemberProfile(
        memberId,
        "",
        "FirstName",
        "LastName",
        Optional.of(address),
        "seomtji@gmail.com",
        "0070334251",
        "se",
        LocalDate.parse("1990-12-12"));
  }

  @Test
  public void responseStatusIsCOMPLETE_AfterMany_Errors() {

    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    BankIdSessionImpl bankIdSessionImpl = new BankIdSessionImpl();
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    bankIdSessionImpl.setCollectionType(BankIdSessionImpl.CollectionType.AUTH);
    bankIdSessionImpl.setLastStatus("OUTSTANDING_TRANSACTION");

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(
        new HashMap<String, BankIdSessionImpl>() {
          {
            put(referenceToken, bankIdSessionImpl);
          }
        });

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    FeignException ex =
        FeignException.errorStatus(
            "collect",
            feign.Response.builder()
                .status(500)
                .headers(new HashMap<>())
                .body("".getBytes())
                .request(Request.create("GET", "", new HashMap<>(), new byte[0], Charset.defaultCharset()))
                .build());
    when(memberService.collect(referenceToken, memberId)).thenThrow(ex);

    CollectService service = new CollectService(userContextRepository, memberService);

    // ACT

    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.COMPLETE);

    // VERIFY

    verify(chat, times(1)).bankIdAuthGeneralCollectError();
  }

  @Test
  public void responseStatusIsCOMPLETE_AfterMany_SignComplete() {

    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    BankIdSessionImpl bankIdSessionImpl = new BankIdSessionImpl();
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    bankIdSessionImpl.setCollectionType(BankIdSessionImpl.CollectionType.SIGN);
    bankIdSessionImpl.setLastStatus("OUTSTANDING_TRANSACTION");

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(
        new HashMap<String, BankIdSessionImpl>() {
          {
            put(referenceToken, bankIdSessionImpl);
          }
        });

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    BankIdCollectResponse response =
        new BankIdCollectResponse(BankIdProgressStatus.COMPLETE, referenceToken, null);
    when(memberService.collect(referenceToken, memberId)).thenReturn(response);

    CollectService service = new CollectService(userContextRepository, memberService);

    BankIdCollectResponse expected =
        new BankIdCollectResponse(BankIdProgressStatus.COMPLETE, referenceToken, null);

    // ACT

    assertThat(service.collect(memberId, referenceToken, chat)).isEqualTo(expected);

    // VERIFY

    verify(chat, times(1)).memberSigned(referenceToken);
  }

  @Test
  public void responseStatusIsCOMPLETE_AfterMany_SignError() {

    final String memberId = "1234";
    final String referenceToken = "referenceToken";

    BankIdSessionImpl bankIdSessionImpl = new BankIdSessionImpl();
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    bankIdSessionImpl.setCollectionType(BankIdSessionImpl.CollectionType.SIGN);
    bankIdSessionImpl.setLastStatus("OUTSTANDING_TRANSACTION");

    UserContext userContext = new UserContext();
    userContext.setBankIdStatus(
        new HashMap<String, BankIdSessionImpl>() {
          {
            put(referenceToken, bankIdSessionImpl);
          }
        });

    when(userContextRepository.findByMemberId(memberId)).thenReturn(Optional.of(userContext));

    FeignException ex =
        FeignException.errorStatus(
            "collect",
            feign.Response.builder()
                .status(500)
                .headers(new HashMap<>())
                .body("".getBytes())
                .request(Request.create("GET", "", new HashMap<>(), new byte[0], Charset.defaultCharset()))
                .build());
    when(memberService.collect(referenceToken, memberId)).thenThrow(ex);

    CollectService service = new CollectService(userContextRepository, memberService);

    // ACT

    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.OUTSTANDING_TRANSACTION);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.COMPLETE);
    bankIdSessionImpl.setLastCallTime(Instant.now().minusSeconds(2));
    assertThat(service.collect(memberId, referenceToken, chat).getBankIdStatus())
        .isEqualTo(BankIdProgressStatus.COMPLETE);

    // VERIFY

    verify(chat, times(1)).bankIdSignError();
  }
}
