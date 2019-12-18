package com.hedvig.botService.utils

import java.time.LocalDate

object BirthDateFromSSNUtil {

    fun birthDateFromSSN(ssn: String): LocalDate {
        return LocalDate.parse(
            ssn.substring(0, 4) + "-" + ssn.substring(4, 6) + "-" + ssn.substring(6, 8)
        )
    }

    fun addCenturyToSSN(ssn: String): String {
        val ssnYear = ssn.substring(0, 2).toInt()
        val breakePoint = LocalDate.now().minusYears(10).year.toString().substring(2, 4).toInt()

        return if (ssnYear > breakePoint) {
            "19$ssn"
        } else {
            "20$ssn"
        }
    }
}
