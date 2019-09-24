package com.hedvig.botService.dataTypes

class HouseBathrooms : HedvigDataType() {

  private var livingSpaceSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val livingSpaceSquareMeters = Integer.parseInt(input)
      //TODO: Should the cap be at 1?
      if (livingSpaceSquareMeters < 0) {
        this.errorMessage = "{INPUT} toaletter låter väldigt få. Prova igen tack!"
        return false
      }
      //TODO: is the cap at 400?
      if (livingSpaceSquareMeters > 400) {
        this.errorMessage = "{INPUT} toaletter?! Låter väldigt många? Hmm... Prova igen tack!"
        return false
      }
      this.livingSpaceSquareMeters = livingSpaceSquareMeters
    } catch (e: NumberFormatException) {
      livingSpaceSquareMeters = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal toaletter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return livingSpaceSquareMeters?.let { livingSpaceSquareMeters ->
      when {
        //TODO: Should the cap be at 1?
        livingSpaceSquareMeters < 0 ->  "hedvig.data.type.house.bathrooms.to.few"
        //TODO: is the cap at 400?
        livingSpaceSquareMeters > 400 -> "hedvig.data.type.house.bathrooms.to.many"
        else -> null
      }
    } ?: "hedvig.data.type.house.bathrooms.not.a.number"
  }
}
