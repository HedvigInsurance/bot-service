package com.hedvig.botService.dataTypes

class HouseLivingSpaceSquareMeters : HedvigDataType() {

  private var livingSpaceSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val livingSpaceSquareMeters = Integer.parseInt(input)
      if (livingSpaceSquareMeters < MIN_LIVING_SPACE_SQM) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!"
        return false
      }
      if (livingSpaceSquareMeters > MAX_LIVING_SPACE_SQM) {
        this.errorMessage = "{INPUT}kvm?! Kan man bo så stort? Hmm... Prova igen tack!"
        return false
      }
      this.livingSpaceSquareMeters = livingSpaceSquareMeters
    } catch (e: NumberFormatException) {
      livingSpaceSquareMeters = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal kvadratmeter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return livingSpaceSquareMeters?.let { livingSpaceSquareMeters ->
      when {
        livingSpaceSquareMeters < MIN_LIVING_SPACE_SQM ->
          "hedvig.data.type.house.living.space.square.meters.to.small"
        livingSpaceSquareMeters > MAX_LIVING_SPACE_SQM ->
          "hedvig.data.type.house.living.space.square.meters.to.big"
        else -> null
      }
    } ?: "hedvig.data.type.house.living.space.square.meters.not.a.number"
  }

  companion object {
    private const val MIN_LIVING_SPACE_SQM = 5
    private const val MAX_LIVING_SPACE_SQM = 1000
  }
}
