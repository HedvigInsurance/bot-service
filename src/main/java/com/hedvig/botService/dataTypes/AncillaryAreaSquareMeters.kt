package com.hedvig.botService.dataTypes

class AncillaryAreaSquareMeters : HedvigDataType() {

  private var ancillaryAreaSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val ancillaryAreaSquareMeters = Integer.parseInt(input)
      if (ancillaryAreaSquareMeters < MIN_NUMBER_OF_ANCILLARY_AREA_SQM) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!"
        return false
      }
      if (ancillaryAreaSquareMeters > MAX_NUMBER_OF_ANCILLARY_AREA_SQM) {
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
    return ancillaryAreaSquareMeters?.let { ancillaryAreaSquareMeters ->
      when {
        ancillaryAreaSquareMeters < MIN_NUMBER_OF_ANCILLARY_AREA_SQM -> "hedvig.data.type.ancillary.area.square.meters.to.small"
        ancillaryAreaSquareMeters > MAX_NUMBER_OF_ANCILLARY_AREA_SQM -> "hedvig.data.type.ancillary.area.square.meters.to.big"
        else -> null
      }
    } ?:  "hedvig.data.type.ancillary.area.square.meters.not.a.number"
  }

  companion object {
    private const val MIN_NUMBER_OF_ANCILLARY_AREA_SQM = 0
    private const val MAX_NUMBER_OF_ANCILLARY_AREA_SQM = 400
  }
}
