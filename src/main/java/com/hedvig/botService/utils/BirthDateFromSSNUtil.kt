package com.hedvig.botService.utils

import java.time.LocalDate

class BirthDateFromSSNUtil {

    fun birthDateFromSSN(ssn: String): LocalDate {
        return LocalDate.parse(
            ssn.substring(0, 4) + "-" + ssn.substring(4, 6) + "-" + ssn.substring(6, 8)
        )
    }
}
