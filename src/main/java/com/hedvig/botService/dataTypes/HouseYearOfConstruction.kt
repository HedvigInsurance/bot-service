package com.hedvig.botService.dataTypes

import java.util.*

class HouseYearOfConstruction : HedvigDataType() {

    private var yearOfConstruction: Int? = null

    //TODO: is the cap at current year +10?
    private val capFuture: Int
        get() = Calendar.getInstance().get(Calendar.YEAR).plus(10)

    override fun validate(input: String): Boolean {
        try {
            val livingSpaceSquareMeters = Integer.parseInt(input)
            //TODO: Should the cap be at 1800?
            if (livingSpaceSquareMeters < 1800) {
                this.errorMessage = "{INPUT} låter väldigt tidigt. Prova igen tack!"
                return false
            }
            if (livingSpaceSquareMeters > capFuture) {
                this.errorMessage = "{INPUT} låter lite långt fram i tiden! Hmm... Prova igen tack!"
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
        return yearOfConstruction?.let { livingSpaceSquareMeters ->
            when {
                //TODO: Should the cap be at 1?
                livingSpaceSquareMeters < 0 -> "hedvig.data.type.house.year.of.construction.to.long.ago"
                //TODO: is the cap at 400?
                livingSpaceSquareMeters > capFuture -> "hedvig.data.type.house.year.of.construction.to.far.in.future"
                else -> null
            }
        } ?: "hedvig.data.type.house.year.of.construction.not.a.number"
    }
}
