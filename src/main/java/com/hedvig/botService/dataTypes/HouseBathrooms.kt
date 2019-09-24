package com.hedvig.botService.dataTypes

class HouseBathrooms : HedvigDataType() {

  private var numberOfBathrooms: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val numberOfBathrooms = Integer.parseInt(input)
      if (numberOfBathrooms < MIN_NUMBER_OF_BATHROOMS) {
        this.errorMessage = "{INPUT} toaletter låter som väldigt få. Prova igen tack!"
        return false
      }
      if (numberOfBathrooms > MAX_NUMBER_OF_BATHROOMS) {
        this.errorMessage = "{INPUT} toaletter?! Låter väldigt många? Hmm... Prova igen tack!"
        return false
      }
      this.numberOfBathrooms = numberOfBathrooms
    } catch (e: NumberFormatException) {
      numberOfBathrooms = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal toaletter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return numberOfBathrooms?.let { numberOfBathrooms ->
      when {
        numberOfBathrooms < MIN_NUMBER_OF_BATHROOMS ->  "hedvig.data.type.house.number.of.bathrooms.to.few"
        numberOfBathrooms > MAX_NUMBER_OF_BATHROOMS -> "hedvig.data.type.house.number.of.bathrooms.to.many"
        else -> null
      }
    } ?: "hedvig.data.type.house.number.of.bathrooms.not.a.number"
  }

  companion object {
    private const val MIN_NUMBER_OF_BATHROOMS = 1
    private const val MAX_NUMBER_OF_BATHROOMS = 20
  }
}
