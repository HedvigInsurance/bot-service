package com.hedvig.botService.services.events


data class RequestPhoneCallEvent(
  var memberId: String,
  var phoneNumber: String,
  var firstName: String?,
  var lastName: String?
)

