package com.hedvig.botService.dataTypes

class AncillaryAreaSquareMeters : HedvigDataType() {

  private var ancillaryAreaSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val ancillaryAreaSquareMeters = Integer.parseInt(input)
      if (ancillaryAreaSquareMeters < 0) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!"
        return false
      }
      if (ancillaryAreaSquareMeters > 400) {
        this.errorMessage = "{INPUT}kvm biyta?! Låter väldigt mycket? Hmm... Prova igen tack!"
        return false
      }
      this.ancillaryAreaSquareMeters = ancillaryAreaSquareMeters
    } catch (e: NumberFormatException) {
      ancillaryAreaSquareMeters = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal kvadratmeter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return ancillaryAreaSquareMeters?.let { subFaceSquareMeters ->
      when {
        subFaceSquareMeters < 0 -> "hedvig.data.type.ancillary.area.square.meters.to.small"
        subFaceSquareMeters > 400 -> "hedvig.data.type.ancillary.area.square.meters.to.big"
        else -> null
      }
    } ?:  "hedvig.data.type.ancillary.area.square.meters.not.a.number"
  }
}
