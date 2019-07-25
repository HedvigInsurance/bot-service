package com.hedvig.botService.web.v2.dto

import com.hedvig.botService.enteties.UserContext

data class OnboardingData(
     val personalNumber: String,
     val email: String
) {
    companion object {
        fun from(userContext: UserContext) = OnboardingData(
            personalNumber = userContext.onBoardingData.ssn,
            email = userContext.onBoardingData.email
        )
    }
}
