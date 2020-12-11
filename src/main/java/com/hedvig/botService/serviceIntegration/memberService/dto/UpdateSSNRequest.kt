package com.hedvig.botService.serviceIntegration.memberService.dto

data class UpdateSsnRequest(
    val ssn: String,
    val ssnWithNationality: SsnWithNationality
)
