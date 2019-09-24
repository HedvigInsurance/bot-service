package com.hedvig.botService.dataTypes

class HouseExtraBuildings : HedvigDataType() {

  private var extraBuildings: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val extraBuildings = Integer.parseInt(input)
      //TODO: Should the cap be at 1?
      if (extraBuildings < 0) {
        this.errorMessage = "{INPUT} extra byggnader låter väldigt få. Prova igen tack!"
        return false
      }
      //TODO: is the cap at 20?
      if (extraBuildings > 20) {
        this.errorMessage = "{INPUT} extra byggnader?! Låter väldigt många? Hmm... Prova igen tack!"
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
      if (extraBuildings < 0) {
        return "hedvig.data.type.house.extra.buildings.to.few"
      }

      if (extraBuildings > 20) {
        "hedvig.data.type.house.extra.buildings.to.many"
      } else null
    } ?: "hedvig.data.type.house.extra.buildings.not.a.number"
  }

}
