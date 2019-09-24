package com.hedvig.botService.dataTypes

class SubFaceSquareMeters : HedvigDataType() {

  private var subFaceSquareMeters: Int? = null

  override fun validate(input: String): Boolean {
    try {
      val subFaceSquareMeters = Integer.parseInt(input)
      if (subFaceSquareMeters < 0) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!"
        return false
      }
      if (subFaceSquareMeters > 400) {
        this.errorMessage = "{INPUT}kvm biyta?! Låter väldigt mycket? Hmm... Prova igen tack!"
        return false
      }
      this.subFaceSquareMeters = subFaceSquareMeters
    } catch (e: NumberFormatException) {
      subFaceSquareMeters = null
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal kvadratmeter. Prova igen tack"
      return false
    }

    return true
  }

  override fun getErrorMessageId(): String? {
    return subFaceSquareMeters?.let {subFaceSquareMeters ->
      when {
        subFaceSquareMeters < 0 -> "hedvig.data.type.sub.face.square.meters.to.small"
        subFaceSquareMeters > 400 -> "hedvig.data.type.sub.face.square.meters.to.big"
        else -> null
      }
    } ?:  "hedvig.data.type.sub.face.square.meters.not.a.number"
  }
}
