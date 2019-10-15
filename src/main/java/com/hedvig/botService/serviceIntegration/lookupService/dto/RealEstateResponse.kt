package com.hedvig.botService.serviceIntegration.lookupService.dto

data class RealEstateResponse(
    val ancillaryArea: Int,
    val area: Int?,
    val assessmentValue: Int?,
    val drain: RealEstateDrain?,
    val livingSpace: Int,
    val numberOfExtraBuildings: Int?,
    val realEstateDesignation: String?,
    val water: RealEstateWater?,
    val yearOfConstruction: Int
)
