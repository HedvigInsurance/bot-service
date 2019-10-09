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
    val HOUSE_FIRST = SingleSelectMessage(
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

    val ASK_STREET_ADDRESS_FAILED_LOOKUP = TextInputMessage(
        "message.house.street.address.failed.lookup",
        "Inga problem!${SPLIT}Vilken gatuadress bor du på?",
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
        "Vad är husets boyta?${SPLIT}I boytan ingår inte förråd, ihopsittande garage, kallvind, pannrum och liknande",
        ""
    )

    val ASK_SQUARE_METERS_FAILED_LOOKUP = NumberInputMessage(
        "message.house.square.meters.failed.lookup",
        "Typiskt \uD83D\uDE48, då behöver jag ställa lite fler frågor till dig${SPLIT}Vad är husets boyta?${SPLIT}I boytan ingår inte förråd, ihopsittande garage, kallvind, pannrum och liknande",
        ""
    )

    val ASK_ANCILLARY_AREA = NumberInputMessage(
        "message.house.ancillary.area",
        "Hur stor är biytan på huset?${SPLIT}Exempel på utrymmen som räknas som biytor är förråd, kallvind, pannrum och garage som sitter ihop med huset",
        ""
    )

    val ASK_YEAR_OF_CONSTRUCTION = NumberInputMessage(
        "message.house.year.of.cunstruction",
        "Vilket år byggdes huset?",
        "åååå"
    )

    val ASK_NUMBER_OF_BATHROOMS = NumberInputMessage(
        "message.house.number.of.bathrooms",
        "Hur många badrum finns i huset?",
        ""
    )

    val ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP = NumberInputMessage(
        "message.house.number.of.from.success.lookup",
        "Perfekt! \uD83D\uDE0AHur många badrum finns i huset?",
        ""
    )

    val ASK_HOUSE_HOUSEHOLD_MEMBERS = NumberInputMessage(
        "message.house.household.members",
        "Okej! Hur många bor där?",
        ""
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

    val SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES =
        SingleSelectOption("message.real.estate.lookup.yes", "Ja, allting stämmer")
    val SELECT_REAL_ESTATE_LOOKUP_CORRECT_NO =
        SingleSelectOption("message.real.estate.lookup.no", "Nej, en eller flera saker stämmer inte")
    val ASK_REAL_ESTATE_LOOKUP_CORRECT = SingleSelectMessage(
        "message.real.estate.lookup",
        "Toppen! Vi har hämtat lite information om ditt hus som vi behöver${SPLIT}" +
                "Stämmer det att:\n " +
                "\uD83D\uDC49 Boytan är {KVM} kvadratmeter\n" +
                "\uD83D\uDC49 Biytan är {HOUSE_ANCILLARY_AREA_KVM} kvadratmeter\n" +
                "\uD83D\uDC49 Och att huset är byggt {HOUSE_YEAR_OF_CUNSTRUCTION} kvadratmeter?",
        listOf(
            SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES,
            SELECT_REAL_ESTATE_LOOKUP_CORRECT_NO
        )
    )

    val ASK_NUMBER_OF_EXTRA_BUILDINGS = NumberInputMessage(
        "message.house.number.of.extra.buildings",
        "Hur många extra byggnader har du?",
        ""
    )

    val SELECT_EXTRA_BUILDING_GARAGE =
        SingleSelectOption("message.house.extra.building.garage", "Garage")
    val SELECT_EXTRA_BUILDING_CARPORT =
        SingleSelectOption("message.house.extra.building.car.port", "Carport")
    val SELECT_EXTRA_BUILDING_SHED =
        SingleSelectOption("message.house.extra.building.shed", "Skjul")
    val SELECT_EXTRA_BUILDING_STORAGE =
        SingleSelectOption("message.house.extra.building.storage", "Förråd")
    val SELECT_EXTRA_BUILDING_FRIGGEBO =
        SingleSelectOption("message.house.extra.building.friggebod", "Friggebod")
    val SELECT_EXTRA_BUILDING_ATTEFALL =
        SingleSelectOption("message.house.extra.building.attefalls", "Attefallshus")
    val SELECT_EXTRA_BUILDING_OUTHOUSE =
        SingleSelectOption("message.house.extra.building.outhouse", "Uthus")
    val SELECT_EXTRA_BUILDING_GUESTHOUSE =
        SingleSelectOption("message.house.extra.building.guest.house", "Gästhus")
    val SELECT_EXTRA_BUILDING_GAZEBO =
        SingleSelectOption("message.house.extra.building.gazebo", "Lusthus")
    val SELECT_EXTRA_BUILDING_GREENHOUSE =
        SingleSelectOption("message.house.extra.building.green.house", "Växthus")
    val SELECT_EXTRA_BUILDING_SAUNA =
        SingleSelectOption("message.house.extra.building.sauna", "Bastu")
    val SELECT_EXTRA_BUILDING_BARN =
        SingleSelectOption("message.house.extra.building.barn", "Lada")
    val SELECT_EXTRA_BUILDING_BOATHOUSE =
        SingleSelectOption("message.house.extra.building.boathouse", "Båthus")
    val SELECT_EXTRA_BUILDING_OTHER =
        SingleSelectOption("message.house.extra.building.other", "Ingen av dessa")
    val SELECT_EXTRA_BUILDING_MORE_OTHER =
        SingleSelectOption("message.house.extra.building.more.other", "Annat")
    val ASK_EXTRA_BUILDING_TYPE = SingleSelectMessage(
        "message.house.extra.building.type",
        "Vad är det för typ av byggnad?",
        listOf(
            SELECT_EXTRA_BUILDING_GARAGE,
            SELECT_EXTRA_BUILDING_FRIGGEBO,
            SELECT_EXTRA_BUILDING_ATTEFALL,
            SELECT_EXTRA_BUILDING_OTHER
        )
    )

    val ASK_MORE_EXTRA_BUILDING_TYPE = SingleSelectMessage(
        "message.house.more.extra.building.type",
        "Är det någon av dessa typer?",
        listOf(
            SELECT_EXTRA_BUILDING_GUESTHOUSE,
            SELECT_EXTRA_BUILDING_CARPORT,
            SELECT_EXTRA_BUILDING_SAUNA,
            SELECT_EXTRA_BUILDING_BOATHOUSE,
            SELECT_EXTRA_BUILDING_MORE_OTHER
        )
    )

    val IN_LOOP_ASK_EXTRA_BUILDING_TYPE = SingleSelectMessage(
        "message.in.loop.house.extra.building.type",
        "Sådär ja, då har vi lagt till {HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}. Vi går vidare till nästa byggnad${SPLIT}Vad är det för typ av byggnad?",
        listOf(
            SELECT_EXTRA_BUILDING_GARAGE,
            SELECT_EXTRA_BUILDING_FRIGGEBO,
            SELECT_EXTRA_BUILDING_ATTEFALL,
            SELECT_EXTRA_BUILDING_OTHER
        )
    )

    val ASK_SQUARE_METERS_EXTRA_BUILDING = NumberInputMessage(
        "message.house.square.meters.building",
        "Hur många kvadratmeter är ${UserData.HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}?",
        ""
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
        "Hyr du ut någon del av ditt hus till någon?",
        listOf(
            SELECT_SUBLETTING_HOUSE_YES,
            SELECT_SUBLETTING_HOUSE_NO
        )
    )

    val SELECT_ADDRESS_LOOK_UP_SUCCESS_YES =
        SingleSelectOption("message.house.look.up.success.yes", "Ja, det stämmer")
    val SELECT_ADDRESS_LOOK_UP_SUCCESS_NO =
        SingleSelectOption("message.house.look.up.success.no", "Nej")
    val ASK_ADDRESS_LOOK_UP_SUCCESS = SingleSelectMessage(
        "message.house.look.up.success",
        "Tack {NAME}! Är det huset på {ADDRESS} jag ska ta fram ett förslag för?",
        listOf(
            SELECT_ADDRESS_LOOK_UP_SUCCESS_YES,
            SELECT_ADDRESS_LOOK_UP_SUCCESS_NO
        )
    )

    val SELECT_MORE_THAN_FOUR_FLOORS =
        SingleSelectOption("message.house.above.four.floors.yes", "Ja")
    val SELECT_LESS_THAN_FIVE_FLOORS =
        SingleSelectOption("message.house.above.four.floors.no", "Nej")
    val ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_YES = SingleSelectMessage(
        "message.house.below.four.floors.from.yes",
        "Bra, då har vi koll på det \uD83D\uDE42${SPLIT}Har huset mer än 4 våningar? Källare inkluderat",
        listOf(
            SELECT_MORE_THAN_FOUR_FLOORS,
            SELECT_LESS_THAN_FIVE_FLOORS
        )
    )

    val ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_NO = SingleSelectMessage(
        "message.house.below.four.floors.from.no",
        "\uD83D\uDC4D${SPLIT}Har huset mer än 4 våningar? Bara så du vet så räknas källaren som en våning",
        listOf(
            SELECT_MORE_THAN_FOUR_FLOORS,
            SELECT_LESS_THAN_FIVE_FLOORS
        )
    )

    val ASK_SSN_UNDER_EIGHTEEN = NumberInputMessage(
        "message.house.ask.ssn.under.eighteen",
        "Hoppsan! \uD83D\uDE4A För att skaffa en försäkring hos mig behöver du tyvärr ha fyllt 18 år"
                + "Om du råkade skriva fel personnummer så kan du testa att skriva igen \uD83D\uDE42${SPLIT}" +
                "Vad är ditt personnummer?",
        "ååååmmddxxxx"
    )

    val MORE_SQM_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.sqm",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor större än 250 kvadratmeter \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_HOUSEHOLD_MEMBERS_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.house.hold.members",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor med fler än 6 personer som bor på samma adress \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_TOTAL_SQM_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.total.sqm",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor större än 300 kvadratmeter sammanlagt \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_YEAR_OF_CONSTRUCTION_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.year.of.construction",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor byggda tidigare än 1925 \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_FLOORS_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.floors",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor med fler än 4 våningar inkl. källare \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_BATHROOMS_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.bathrooms",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor med fler än 2 badrum \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_EXTRA_BUILDINGS_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.extra.buildings",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för villor med fler än 4 extra byggnader \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )

    val MORE_EXTRA_BUILDING_SQM_QUESTIONS_CALL = NumberInputMessage(
        "message.house.more.questions.call.extra.building.sqm",
        "Tack! Jag behöver ställa några frågor på telefon till dig eftersom vi för tillfället inte har stöd för extra byggnader som är större än 75 kvadratmeter \uD83D\uDE42${SPLIT}Vilket telefonnummer kan jag nå dig på?",
        "070 123 45 67"
    )
}
