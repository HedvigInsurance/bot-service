package com.hedvig.botService.Utils

import org.springframework.stereotype.Component


import java.time.LocalDate

@Component
class BirthDateFromSSNUtil {

    fun birthDateFromSSN(ssn: String): LocalDate {
        return LocalDate.parse(
            ssn.substring(0, 4) + "-" + ssn.substring(4, 6) + "-" + ssn.substring(6, 8)
        )
    }
}
