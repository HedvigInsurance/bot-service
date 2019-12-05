package com.hedvig.botService.serviceIntegration.claimsService.dto

data class ClaimFileFromAppDTO(
    val fileUploadKey: String,
    val mimeType: String,
    val memberId: String
)
