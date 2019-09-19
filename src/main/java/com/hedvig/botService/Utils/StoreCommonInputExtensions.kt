package com.hedvig.botService.Utils

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.MessageBodyNumber
import com.hedvig.botService.enteties.message.MessageBodyText
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import java.time.LocalDate

fun UserContext.storeFamilyName(body: MessageBodyText): String {
    val familyName = body.text.trim().capitalizeAll()
    val firstName = this.onBoardingData.firstName
    if (firstName != null) {
        if (firstName.split(" ").size > 1 && firstName.endsWith(familyName, true) == true) {
            val lastNameIndex = firstName.length - (familyName.length + 1)
            if (lastNameIndex > 0) {
                this.onBoardingData.firstName = firstName.substring(0, lastNameIndex)
            }
        }
    }
    this.onBoardingData.familyName = familyName
    return familyName
}

private fun String.capitalizeAll(): String {
    return this.split(regex = Regex("\\s")).map { it.toLowerCase().capitalize() }.joinToString(" ")
}

fun UserContext.storeAndTrimAndAddSSNToChat(body: MessageBodyNumber, addToChat: (String) -> Unit): String {

    val trimmedSSN = body.text.trim()
    addToChat("${trimmedSSN.dropLast(4)}-****")

    this.onBoardingData.apply {
        ssn = trimmedSSN
        birthDate = LocalDate.parse(
            "${trimmedSSN.substring(0, 4)}-${trimmedSSN.substring(
                4,
                6
            )}-${trimmedSSN.substring(6, 8)}"
        )
    }

    return trimmedSSN
}
