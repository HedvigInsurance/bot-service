package com.hedvig.botService.serviceIntegration.productPricing.dto

data class ExtraBuilding(
    val type: ExtraBuildingType,
    val area: Int,
    val hasWaterConnected: Boolean
)
