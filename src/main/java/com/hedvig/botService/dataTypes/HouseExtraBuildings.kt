package com.hedvig.botService.dataTypes

class HouseExtraBuildings : HedvigDataType() {

  private var extraBuildings: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val extraBuildings = Integer.parseInt(input)
      if (extraBuildings < MIN_NUMBER_OF_EXTRA_BUILDINGS) {
        this.errorMessage = "{INPUT} extra byggnader låter väldigt få. Prova igen tack!"
        return false
      }
      if (extraBuildings > MAX_NUMBER_OF_EXTRA_BUILDINGS) {
        this.errorMessage = "{INPUT} extra byggnader?! Låter som väldigt många! Hmm... Prova igen tack!"
        return false
      }
      this.extraBuildings = extraBuildings
    } catch (e: NumberFormatException) {
      extraBuildings = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal extra byggnader. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return extraBuildings?.let { extraBuildings ->
      if (extraBuildings < MIN_NUMBER_OF_EXTRA_BUILDINGS) {
        return "hedvig.data.type.house.extra.buildings.to.few"
      }

      if (extraBuildings > MAX_NUMBER_OF_EXTRA_BUILDINGS) {
        "hedvig.data.type.house.extra.buildings.to.many"
      } else null
    } ?: "hedvig.data.type.house.extra.buildings.not.a.number"
  }

  companion object {
    private const val MIN_NUMBER_OF_EXTRA_BUILDINGS = 0
    private const val MAX_NUMBER_OF_EXTRA_BUILDINGS = 20
  }
}
