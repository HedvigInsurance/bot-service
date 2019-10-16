package com.hedvig.botService.services.events

class OnboardingCallForQuoteEvent(
    val memberId: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String
)

