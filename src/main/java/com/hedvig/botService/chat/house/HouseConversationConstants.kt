package com.hedvig.botService.chat.house

import com.hedvig.botService.enteties.message.KeyboardType
import com.hedvig.botService.enteties.message.TextContentType
import com.hedvig.botService.enteties.userContextHelpers.UserData

object HouseConversationConstants {

    const val HOUSE_CONVERSATION_DONE = "conversation.done"
    const val CONVERSATION_RENT_DONE = "conversation.rent.done"

    const val SPLIT = "\u000C"

    val SELECT_OWN = SingleSelectOption("message.house.own", "Jag äger huset")
    val SELECT_RENT = SingleSelectOption("message.house.rent", "Jag hyr huset")
    val HUS_FIRST = SingleSelectMessage(
        "message.house.first",
        "\uD83D\uDC4D${SPLIT}Hyr du eller äger du huset?",
        listOf(
            SELECT_OWN,
            SELECT_RENT
        )
    )

    val ASK_SSN = NumberInputMessage(
        "message.house.ask.ssn",
        "Vad är ditt personnummer? Jag behöver det så jag kan hämta din adress",
        "ååååmmddxxxx"
    )

    val ASK_LAST_NAME = TextInputMessage(
        "message.house.ask.last.name",
        "Konstigt, just nu kan jag inte hitta din adress. Så jag behöver ställa några extra frågor \uD83D\uDE0A${SPLIT}Vad heter du i efternamn?",
        TextContentType.FAMILY_NAME,
        KeyboardType.DEFAULT,
        "Efternamn"
    )

    val ASK_STREET_ADDRESS = TextInputMessage(
        "message.house.street.address",
        "Vilken gatuadress bor du på?",
        TextContentType.STREET_ADDRESS_LINE1,
        KeyboardType.DEFAULT,
        "Kungsgatan 1"
    )

    val ASK_ZIP_CODE = NumberInputMessage(
        "message.house.zip.code",
        "Vad är ditt postnummer?",
        "123 45"
    )

    val ASK_SQUARE_METERS = NumberInputMessage(
        "message.house.square.meters",
        "Vad är husets totala bostadsyta?${SPLIT}Exempelvis sovrum, vardagsrum, sällskapsutrymmen och kök. I boytan ingår inte förråd, ihopsittande garage, kallvind, pannrum och liknande.",
        "Bostadsyta"
    )

    val ASK_ANCILLARY_AREA = NumberInputMessage(
        "message.house.ancillary.area",
        "Vad är biytan på huset?${SPLIT}Exempel på utrymmen som räknas som biytor är förråd, kallvind, pannrum och garage som sitter ihop med huset.",
        "Bostadsyta"
    )

    val ASK_YEAR_OF_CONSTRUCTION = NumberInputMessage(
        "message.house.year.of.cunstruction",
        "Vilket år byggdes huset?",
        "åååå"
    )

    val ASK_HOUSE_HOUSEHOLD_MEMBERS = NumberInputMessage(
        "message.house.household.members",
        "Okej! Hur många bor där?",
        "Boende"
    )

    val ASK_NUMBER_OF_BATHROOMS = NumberInputMessage(
        "message.house.number.of.bathrooms",
        "Hur många badrum har du?",
        "Badrum"
    )

    val SELECT_EXTRA_BUILDING_YES =
        SingleSelectOption("message.house.extra.buildings.yes", "Ja, det har jag")
    val SELECT_EXTRA_BUILDING_NO =
        SingleSelectOption("message.house.extra.buildings.no", "Nej, gå vidare")
    val ASK_HAS_EXTRA_BUILDINGS = SingleSelectMessage(
        "message.house.extra.buildings",
        "Har du några övriga byggnader på tomten? T.ex. garage eller gäststuga",
        listOf(
            SELECT_EXTRA_BUILDING_YES,
            SELECT_EXTRA_BUILDING_NO
        )
    )

    val ASK_NUMBER_OF_EXTRA_BUILDINGS = NumberInputMessage(
        "message.house.number.of.extra.buildings",
        "Hur många extra byggnader har du?",
        "Byggnader"
    )

    val SELECT_EXTRA_BUILDING_GARAGE =
        SingleSelectOption("message.house.extra.building.garage", "Garage")
    val SELECT_EXTRA_BUILDING_FRIGGEBO =
        SingleSelectOption("message.house.extra.building.friggebod", "Friggebod")
    val SELECT_EXTRA_BUILDING_ATTEFALS =
        SingleSelectOption("message.house.extra.building.attefalls", "Attefalls")
    val SELECT_EXTRA_BUILDING_OTHER =
        SingleSelectOption("message.house.extra.building.other", "Annat")
    val ASK_EXTRA_BUILDING_TYPE = SingleSelectMessage(
        "message.house.extra.building.type",
        "Vad är det för typ av byggnad?",
        listOf(
            SELECT_EXTRA_BUILDING_GARAGE,
            SELECT_EXTRA_BUILDING_FRIGGEBO,
            SELECT_EXTRA_BUILDING_ATTEFALS,
            SELECT_EXTRA_BUILDING_OTHER
        )
    )

    val IN_LOOP_ASK_EXTRA_BUILDING_TYPE = SingleSelectMessage(
        "message.house.extra.building.type",
        "Sådär ja, då har vi lagt till {HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}. Vi går vidare till nästa byggnad${SPLIT}Vad är det för typ av byggnad?",
        listOf(
            SELECT_EXTRA_BUILDING_GARAGE,
            SELECT_EXTRA_BUILDING_FRIGGEBO,
            SELECT_EXTRA_BUILDING_ATTEFALS,
            SELECT_EXTRA_BUILDING_OTHER
        )
    )

    val ASK_SQUARE_METERS_EXTRA_BUILDING = NumberInputMessage(
        "message.house.square.meters.building",
        "Hur många kvadratmeter är ${UserData.HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}?",
        "kvm"
    )

    val SELECT_EXTRA_BUILDING_HAS_WATER_YES =
        SingleSelectOption("message.house.extra.building.has.water.yes", "Ja")
    val SELECT_EXTRA_BUILDING_HAS_WATER_NO =
        SingleSelectOption("message.house.extra.building.has.water.no", "Nej")
    val ASK_HAS_WATER_EXTRA_BUILDING = SingleSelectMessage(
        "message.house.has.water.building",
        "Finns det indraget vatten till ${UserData.HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}?",
        listOf(
            SELECT_EXTRA_BUILDING_HAS_WATER_YES,
            SELECT_EXTRA_BUILDING_HAS_WATER_NO
        )
    )


    val SELECT_SUBLETTING_HOUSE_YES =
        SingleSelectOption("message.house.sublet.yes", "Ja")
    val SELECT_SUBLETTING_HOUSE_NO =
        SingleSelectOption("message.house.sublet.no", "Nej")
    val ASK_SUBLETTING_HOUSE = SingleSelectMessage(
        "message.house.supletting.house",
        "Super! Ett par frågor till \uD83D\uDE0A${SPLIT}Hyr du ut någon del av ditt hus till någon?",
        listOf(
            SELECT_SUBLETTING_HOUSE_YES,
            SELECT_SUBLETTING_HOUSE_NO
        )
    )

    val MORE_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call",
        "Tack! Jag behöver ställa några frågor på telefon till dig, innan jag kan ge dig ditt förslag \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val SELECT_LOOK_UP_SUCCESS_YES =
        SingleSelectOption("merssage.house.look.up.success.yes", "Ja, det stämmer")
    val SELECT_LOOK_UP_SUCCESS_NO =
        SingleSelectOption("merssage.house.look.up.success.no", "Nej")
    val ASK_LOOK_UP_SUCCESS = SingleSelectMessage(
        "merssage.house.look.up.success",
        "Tack {NAME}! Är det huset på {ADDRESS} jag ska ta fram ett förslag för?",
        listOf(
            SELECT_LOOK_UP_SUCCESS_YES,
            SELECT_EXTRA_BUILDING_NO
        )
    )

    val SELECT_MORE_THAN_FOUR_FLOORS =
        SingleSelectOption("message.house.above.four.floors.yes", "Ja, mer än 4:a våningar")
    val SELECT_LESS_THAN_FIVE_FLOORS =
        SingleSelectOption("message.house.above.four.floors.no", "Nej, mindre än 5 våningar")
    val ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS = SingleSelectMessage(
        "message.house.below.four.floors",
        "Har huset mer än 4 våningar?${SPLIT}Inkludera källare",
        listOf(
            SELECT_MORE_THAN_FOUR_FLOORS,
            SELECT_LESS_THAN_FIVE_FLOORS
        )
    )
}
