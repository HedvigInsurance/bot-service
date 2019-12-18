package com.hedvig.botService.chat;

import com.hedvig.botService.serviceIntegration.memberService.exceptions.ErrorType;

public interface BankIdChat {
  void bankIdAuthComplete();

  void bankIdAuthGeneralCollectError();

  void memberSigned(String referenceId);

  void bankIdSignError();

  void oustandingTransaction();

  void noClient();

  void started();

  void userSign();

  void couldNotLoadMemberProfile();

  void signalSignFailure(ErrorType errorType, String detail);

  void signalAuthFailiure(ErrorType errorType, String detail);

  void bankIdAuthCompleteNoAddress();
}
