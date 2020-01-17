package com.hedvig.botService.serviceIntegration.underwriter

import com.hedvig.botService.chat.OnboardingConversationDevi
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.userContextHelpers.UserData
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.ArrayList
import java.util.UUID
import kotlin.math.roundToInt

@Component
class Underwriter(private val underwriterClient: UnderwriterClient) {

    fun createQuote(userContext: UserContext) {

        val onboardingData = userContext.onBoardingData

        val onboardingProductType = OnboardingConversationDevi.ProductTypes.valueOf(onboardingData.houseType)

        underwriterClient.createQuote(
            QuoteRequestDto(
                firstName = onboardingData.firstName,
                lastName = onboardingData.familyName,
                email = onboardingData.email,
                currentInsurer = onboardingData.currentInsurer,
                birthDate = onboardingData.birthDate,
                ssn = onboardingData.ssn,
                quotingPartner = Partner.HEDVIG,
                productType = createProductType(onboardingProductType),
                incompleteQuoteData = createQuoteData(onboardingProductType, onboardingData),
                memberId = userContext.memberId,
                originatingProductId = null,
                startDate = null,
                shouldComplete = true,
                underwritingGuidelinesBypassedBy = null
            )
        )
    }

    private fun createQuoteData(
        onboardingProductType: OnboardingConversationDevi.ProductTypes,
        onboardingData: UserData
    ): IncompleteQuoteRequestData {
        return when (onboardingProductType) {
            OnboardingConversationDevi.ProductTypes.HOUSE -> IncompleteHouseQuoteDataDto(
                onboardingData.addressStreet,
                onboardingData.addressZipCode,
                onboardingData.addressCity,
                onboardingData.livingSpace.roundToInt(),
                onboardingData.personsInHouseHold,
                onboardingData.houseAncillaryArea,
                onboardingData.yearOfConstruction,
                onboardingData.numberOfBathrooms,
                createExtraBuildings(onboardingData),
                onboardingData.isSubLetting,
                onboardingData.floor
            )
            else -> IncompleteApartmentQuoteDataDto(
                onboardingData.addressStreet,
                onboardingData.addressZipCode,
                onboardingData.addressCity,
                onboardingData.livingSpace.roundToInt(),
                onboardingData.personsInHouseHold,
                onboardingData.floor,
                createApartmentSubType(onboardingProductType)
            )
        }
    }

    private fun createProductType(productTypes: OnboardingConversationDevi.ProductTypes): ProductType {
        return when (productTypes) {
            OnboardingConversationDevi.ProductTypes.HOUSE -> ProductType.HOUSE
            OnboardingConversationDevi.ProductTypes.BRF -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.LODGER -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.RENT -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.RENT_BRF -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.STUDENT_BRF -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.STUDENT_RENT -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.SUBLET_BRF -> ProductType.APARTMENT
            OnboardingConversationDevi.ProductTypes.SUBLET_RENTAL -> ProductType.APARTMENT
        }
    }

    private fun createApartmentSubType(productTypes: OnboardingConversationDevi.ProductTypes): ApartmentProductSubType {
        return when (productTypes) {
            OnboardingConversationDevi.ProductTypes.BRF -> ApartmentProductSubType.BRF
            OnboardingConversationDevi.ProductTypes.LODGER -> ApartmentProductSubType.LODGER
            OnboardingConversationDevi.ProductTypes.RENT -> ApartmentProductSubType.RENT
            OnboardingConversationDevi.ProductTypes.RENT_BRF -> ApartmentProductSubType.RENT_BRF
            OnboardingConversationDevi.ProductTypes.STUDENT_BRF -> ApartmentProductSubType.STUDENT_BRF
            OnboardingConversationDevi.ProductTypes.STUDENT_RENT -> ApartmentProductSubType.STUDENT_RENT
            OnboardingConversationDevi.ProductTypes.SUBLET_BRF -> ApartmentProductSubType.SUBLET_BRF
            OnboardingConversationDevi.ProductTypes.SUBLET_RENTAL -> ApartmentProductSubType.SUBLET_RENTAL
            OnboardingConversationDevi.ProductTypes.HOUSE -> throw RuntimeException("Cannot create ApartmentProductSubType from ${productTypes.name}")
        }
    }

    private fun createExtraBuildings(onboardingData:UserData):List<ExtraBuildingRequestDto> {
        val extraBuildings: MutableList<ExtraBuildingRequestDto> = ArrayList()
        for (i in 1..onboardingData.nrExtraBuildings) {
            val extraBuilding = ExtraBuildingRequestDto(
                UUID.randomUUID(),
                ExtraBuildingType.valueOf(onboardingData.getHouseExtraBuildingType(i)),
                onboardingData.getHouseExtraBuildingSQM(i),
                onboardingData.getHouseExtraBuildingHasWater(i)
            )
            extraBuildings.add(extraBuilding)
        }
        return extraBuildings.toList()
    }
}
