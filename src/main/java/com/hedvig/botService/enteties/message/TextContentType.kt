package com.hedvig.botService.enteties.message

import com.fasterxml.jackson.annotation.JsonProperty

enum class TextContentType {
    @JsonProperty("none")
    NONE,
    @JsonProperty("URL")
    URL,
    @JsonProperty("addressCity")
    ADDRESS_CITY,
    @JsonProperty("addressCityAndState")
    ADDRESS_CITY_STATE,
    @JsonProperty("addressState")
    ADDRESS_STATE,
    @JsonProperty("countryName")
    COUNTRY_NAME,
    @JsonProperty("creditCardNumber")
    CREDIT_CARD_NUMBER,
    @JsonProperty("emailAddress")
    EMAIL_ADDRESS,
    @JsonProperty("familyName")
    FAMILY_NAME,
    @JsonProperty("fullStreetAddress")
    FULL_STREET_ADDRESS,
    @JsonProperty("givenName")
    GIVEN_NAME,
    @JsonProperty("jobTitle")
    JOB_TITLE,
    @JsonProperty("location")
    LOCATION,
    @JsonProperty("middleName")
    MIDDLE_NAME,
    @JsonProperty("name")
    NAME,
    @JsonProperty("namePrefix")
    NAME_PREFIX,
    @JsonProperty("nameSuffix")
    NAME_SUFFIX,
    @JsonProperty("nickname")
    NICK_NAME,
    @JsonProperty("organizationName")
    ORGANIZATION_NAME,
    @JsonProperty("postalCode")
    POSTAL_CODE,
    @JsonProperty("streetAddressLine1")
    STREET_ADDRESS_LINE1,
    @JsonProperty("streetAddressLine2")
    STREET_ADDRESS_LINE2,
    @JsonProperty("sublocality")
    SUBLOCALITY,
    @JsonProperty("telephoneNumber")
    TELEPHONE_NUMBER,
    @JsonProperty("username")
    USERNAME,
    @JsonProperty("password")
    PASSWORD
}
