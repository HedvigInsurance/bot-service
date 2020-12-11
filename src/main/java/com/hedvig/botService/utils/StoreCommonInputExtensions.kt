package com.hedvig.botService.utils

import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.MessageBodyNumber
import com.hedvig.botService.enteties.message.MessageBodyText
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.SsnWithNationality
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

fun UserContext.storeAndTrimAndAddSSNToChat(body: MessageBodyNumber, addToChat: (String) -> Unit): Pair<String, LocalDate> {
    val trimmedSSN = body.text.trim().let { ssn ->
        if (ssn.length == 10) {
            BirthDateFromSSNUtil.addCenturyToSSN(ssn)
        } else {
            ssn
        }
    }

    addToChat("${trimmedSSN.dropLast(4)}-****")

    val memberBirthDate = BirthDateFromSSNUtil.birthDateFromSSN(trimmedSSN)

    this.onBoardingData.apply {
        ssn = trimmedSSN
        birthDate = memberBirthDate
    }

    return Pair(trimmedSSN, memberBirthDate)
}

fun MemberService.ssnLookupAndStore(userContext: UserContext, ssnWithNationality: SsnWithNationality): Boolean {
    this.updateSSN(userContext.memberId, ssnWithNationality)
    return this.lookupAddressSWE(ssnWithNationality.ssn, userContext.memberId)?.let { response ->
        userContext.onBoardingData.let { userData ->
            userData.familyName = response.lastName
            userData.firstName = response.firstName

            response.address?.let {
                userData.addressCity = response.address.city
                userData.addressStreet = response.address.street
                userData.addressZipCode = response.address.zipCode
                true
            } ?: false
        }
    } ?: false
}


