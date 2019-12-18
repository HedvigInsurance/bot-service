package com.hedvig.botService.services.events

data class HouseUnderwritingLimitCallMeExceedsEvent(
    val memberId: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String,
    val reason: String
)
