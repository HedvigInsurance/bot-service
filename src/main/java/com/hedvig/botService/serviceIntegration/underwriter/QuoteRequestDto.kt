package com.hedvig.botService.serviceIntegration.underwriter

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class QuoteRequestDto(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val currentInsurer: String?,
    val birthDate: LocalDate?,
    val ssn: String?,
    val quotingPartner: Partner?,
    val productType: ProductType?,
    @field:JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @field:JsonSubTypes(
        JsonSubTypes.Type(value = IncompleteApartmentQuoteDataDto::class, name = "apartment"),
        JsonSubTypes.Type(value = IncompleteHouseQuoteDataDto::class, name = "house")
    ) val incompleteQuoteData: IncompleteQuoteRequestData?,
    val memberId: String? = null,
    val originatingProductId: UUID? = null,
    val startDate: Instant? = null,
    val dataCollectionId: UUID? = null,
    val shouldComplete: Boolean,
    val underwritingGuidelinesBypassedBy: String?
)

sealed class IncompleteQuoteRequestData

data class IncompleteHouseQuoteDataDto(
    val street: String?,
    val zipCode: String?,
    val city: String?,
    val livingSpace: Int?,
    val householdSize: Int?,
    val ancillaryArea: Int?,
    val yearOfConstruction: Int?,
    val numberOfBathrooms: Int?,
    val extraBuildings: List<ExtraBuildingRequestDto>?,
    @field:JsonProperty("subleted")
    val isSubleted: Boolean?,
    val floor: Int? = 0
) : IncompleteQuoteRequestData()

data class IncompleteApartmentQuoteDataDto(
    val street: String?,
    val zipCode: String?,
    val city: String?,
    val livingSpace: Int?,
    val householdSize: Int?,
    val floor: Int?,
    val subType: ApartmentProductSubType?
) : IncompleteQuoteRequestData()

data class ExtraBuildingRequestDto(
    val id: UUID?,
    val type: ExtraBuildingType,
    val area: Int,
    val hasWaterConnected: Boolean
)


enum class Partner(val campaignCode: String? = null) {
    HEDVIG,
    INSPLANET,
    COMPRICER,
    INSURLEY
}


enum class ProductType {
    APARTMENT,
    HOUSE,
    OBJECT,
    UNKNOWN
}

enum class ExtraBuildingType {
    GARAGE,
    CARPORT,
    SHED,
    STOREHOUSE,
    FRIGGEBOD,
    ATTEFALL,
    OUTHOUSE,
    GUESTHOUSE,
    GAZEBO,
    GREENHOUSE,
    SAUNA,
    BARN,
    BOATHOUSE,
    OTHER
}

enum class ApartmentProductSubType {
    BRF,
    RENT,
    RENT_BRF,
    SUBLET_RENTAL,
    SUBLET_BRF,
    STUDENT_BRF,
    STUDENT_RENT,
    LODGER,
    UNKNOWN
}

data class CompleteQuoteResponseDto(
    val id: UUID,
    val price: BigDecimal,
    val validTo: Instant
)
