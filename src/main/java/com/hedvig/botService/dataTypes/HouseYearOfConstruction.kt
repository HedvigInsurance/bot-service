package com.hedvig.botService.dataTypes

import java.util.*

class HouseYearOfConstruction : HedvigDataType() {

    private var yearOfConstruction: Int? = null

    private val currentYear: Int
        get() = Calendar.getInstance().get(Calendar.YEAR)

    override fun validate(input: String): Boolean {
        try {
            val livingSpaceSquareMeters = Integer.parseInt(input)
            if (livingSpaceSquareMeters < MIN_YEAR_OF_CONSTRUCTION) {
                this.errorMessage = "{INPUT} låter väldigt gammalt! Prova igen tack!"
                return false
            }
            if (livingSpaceSquareMeters > currentYear) {
                this.errorMessage = "{INPUT} har ju inte varit än! Prova igen tack!"
                return false
            }
            this.yearOfConstruction = livingSpaceSquareMeters
        } catch (e: NumberFormatException) {
            yearOfConstruction = null
            this.errorMessage = "{INPUT} verkar vara ett konstigt byggnas år. Prova igen tack"
            return false
        }

        return true
    }

    override fun getErrorMessageId(): String? {
        return yearOfConstruction?.let { yearOfConstruction ->
            when {
                yearOfConstruction < MIN_YEAR_OF_CONSTRUCTION -> "hedvig.data.type.house.year.of.construction.to.long.ago"
                yearOfConstruction > currentYear -> "hedvig.data.type.house.year.of.construction.to.far.in.future"
                else -> null
            }
        } ?: "hedvig.data.type.house.year.of.construction.not.a.number"
    }

    companion object {
        private const val MIN_YEAR_OF_CONSTRUCTION = 1217
    }
}
