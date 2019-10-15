package com.hedvig.botService.dataTypes

class HouseExtraBuildingSQM : HedvigDataType() {

  private var extraBuildingSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val extraBuildingSquareMeters = Integer.parseInt(input)
      if (extraBuildingSquareMeters < 0) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!"
        return false
      }
      if (extraBuildingSquareMeters > 400) {
        this.errorMessage = "{INPUT}kvm?! Låter väldigt mycket? Hmm... Prova igen tack!"
        return false
      }
      this.extraBuildingSquareMeters = extraBuildingSquareMeters
    } catch (e: NumberFormatException) {
      extraBuildingSquareMeters = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal kvadratmeter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return extraBuildingSquareMeters?.let { extraBuildingSquareMeters ->
      when {
        extraBuildingSquareMeters < 0 -> "hedvig.data.type.extra.building.square.meters.to.small"
        extraBuildingSquareMeters > 400 -> "hedvig.data.type.extra.building.square.meters.to.big"
        else -> null
      }

    } ?: "hedvig.data.type.extra.building.square.meters.not.a.number"
  }
}
