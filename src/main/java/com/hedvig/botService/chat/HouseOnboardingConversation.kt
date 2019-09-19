package com.hedvig.botService.chat

import com.hedvig.botService.Utils.storeAndTrimAndAddSSNToChat
import com.hedvig.botService.Utils.storeFamilyName
import com.hedvig.botService.chat.HouseConversationConstants.ASK_AGE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_BATHROOMS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_HAS_EXTRA_BUILDINGS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING_FOUR
import com.hedvig.botService.chat.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING_ONE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING_THREE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING_TWO
import com.hedvig.botService.chat.HouseConversationConstants.ASK_LAST_NAME
import com.hedvig.botService.chat.HouseConversationConstants.ASK_NUMBER_OF_EXTRA_BUILDINGS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_RESIDENTS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SQUARE_METERS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING_ONE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING_THREE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING_TWO
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SSN
import com.hedvig.botService.chat.HouseConversationConstants.ASK_STREET_ADDRESS
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SUBFACE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_SUBLETTING_HOUSE
import com.hedvig.botService.chat.HouseConversationConstants.ASK_ZIP_CODE
import com.hedvig.botService.chat.HouseConversationConstants.CONVERSATION_RENT_DONE
import com.hedvig.botService.chat.HouseConversationConstants.HOUSE_CONVERSATION_DONE
import com.hedvig.botService.chat.HouseConversationConstants.HUS_FIRST
import com.hedvig.botService.chat.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_NO
import com.hedvig.botService.chat.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_YES
import com.hedvig.botService.chat.HouseConversationConstants.SELECT_EXTRA_BUILDING_YES
import com.hedvig.botService.chat.HouseConversationConstants.SELECT_RENT
import com.hedvig.botService.chat.HouseConversationConstants.SELECT_SUBLETTING_HOUSE_YES
import com.hedvig.botService.dataTypes.HouseholdMemberNumber
import com.hedvig.botService.dataTypes.LivingSpaceSquareMeters
import com.hedvig.botService.dataTypes.SSNSweden
import com.hedvig.botService.dataTypes.ZipCodeSweden
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.services.LocalizationService

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher

class HouseOnboardingConversation
constructor(
    override var eventPublisher: ApplicationEventPublisher,
    private val conversationFactory: ConversationFactory,
    localizationService: LocalizationService,
    userContext: UserContext
) : Conversation(eventPublisher, localizationService, userContext) {

    var queuePos: Int? = null

    init {
        createInputMessage(
            HUS_FIRST
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_RENT.value -> {
                    userContext.completeConversation(this)
                    val conversation =
                        conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)
                    userContext.startConversation(
                        conversation,
                        OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER
                    )
                    CONVERSATION_RENT_DONE
                }
                else -> {
                    ASK_SSN.id
                }
            }
        }

        createInputMessage(
            ASK_SSN
        ) { body, userContext, message ->
            userContext.storeAndTrimAndAddSSNToChat(body) {
                message.body.text = it
                addToChat(message)
            }
            //TODO look up
            ASK_LAST_NAME.id
        }
        this.setExpectedReturnType(ASK_SSN.id, SSNSweden())

        createInputMessage(
            ASK_LAST_NAME
        ) { body, userContext, message ->
            userContext.storeFamilyName(body)
            addToChat(message)
            //TODO is this really necessary?
            ASK_AGE.id
        }

        createInputMessage(
            ASK_AGE
        ) { body, userContext, message ->
            //TODO store age
            addToChat(message)
            ASK_STREET_ADDRESS.id
        }

        createInputMessage(
            ASK_STREET_ADDRESS
        ) { body, userContext, message ->
            userContext.onBoardingData.addressStreet = message.body.text
            addToChat(message)
            ASK_ZIP_CODE.id
        }

        createInputMessage(
            ASK_ZIP_CODE
        ) { body, userContext, message ->
            userContext.onBoardingData.addressZipCode = message.body.text
            addToChat(message)
            ASK_SQUARE_METERS.id
        }
        this.setExpectedReturnType(ASK_ZIP_CODE.id, ZipCodeSweden())

        createInputMessage(
            ASK_SQUARE_METERS
        ) { body, userContext, message ->
            //TODO store square meters
            addToChat(message)
            ASK_SUBFACE.id
        }
        this.setExpectedReturnType(ASK_SQUARE_METERS.id, LivingSpaceSquareMeters())

        createInputMessage(
            ASK_SUBFACE
        ) { body, userContext, message ->
            userContext.onBoardingData.houseSubface = message.body.text
            addToChat(message)
            ASK_AGE.id
        }
        //TODO subface data type

        createInputMessage(
            ASK_RESIDENTS
        ) { body, userContext, message ->
            val nrPersons = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.setPersonInHouseHold(nrPersons)
            addToChat(message)
            ASK_BATHROOMS.id
        }
        //TODO check if same as apartment
        this.setExpectedReturnType(ASK_RESIDENTS.id, HouseholdMemberNumber())

        createInputMessage(
            ASK_BATHROOMS
        ) { body, userContext, message ->
            val bathrooms = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.bathroomsInHouse = bathrooms
            addToChat(message)
            ASK_HAS_EXTRA_BUILDINGS.id
        }

        createInputMessage(
            ASK_HAS_EXTRA_BUILDINGS
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_EXTRA_BUILDING_YES.value -> {
                    userContext.onBoardingData.hasExtraBuildings = true
                    ASK_NUMBER_OF_EXTRA_BUILDINGS.id
                }
                else -> {
                    userContext.onBoardingData.hasExtraBuildings = false
                    ASK_SUBLETTING_HOUSE.id
                }
            }
        }

        createInputMessage(
            ASK_NUMBER_OF_EXTRA_BUILDINGS
        ) { body, userContext, message ->
            addToChat(message)
            when {
                body.value <= 0 -> {
                    // todo should not happen with
                    ASK_SUBLETTING_HOUSE.id
                }
                body.value >= 5 -> {
                    userContext.onBoardingData.nrExtraBuildings = body.value
                    //TODO ask some questions
                    "phone"
                }
                else -> {
                    userContext.onBoardingData.nrExtraBuildings = body.value
                    ASK_SQUARE_METERS_EXTRA_BUILDING_ONE.id
                }
            }
        }
        //TODO this.setExpectedReturnType(ASK_RESIDENTS.id, HouseExtraBuildings())

        createInputMessage(
            ASK_SQUARE_METERS_EXTRA_BUILDING_ONE
        ) { body, userContext, message ->
            userContext.onBoardingData.houseExtraBuildingOneSQM = (message.body as MessageBodyNumber).value
            addToChat(message)
            ASK_HAS_WATER_EXTRA_BUILDING_ONE.id
        }
        //TODO this.setExpectedReturnType(ASK_SQUARE_METERS_EXTRA_BUILDING_ONE.id, HouseExtraBuildingSQM())

        createInputMessage(
            ASK_HAS_WATER_EXTRA_BUILDING_ONE
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_EXTRA_BUILDING_HAS_WATER_YES.value -> {
                    userContext.onBoardingData.extraBuildingOneHasWater = true
                }
                SELECT_EXTRA_BUILDING_HAS_WATER_NO.value -> {
                    userContext.onBoardingData.extraBuildingOneHasWater = false
                }
            }
            if (userContext.onBoardingData.nrExtraBuildings <= 1) {
                ASK_SUBLETTING_HOUSE.id
            } else {
                ASK_SQUARE_METERS_EXTRA_BUILDING_TWO.id
            }
        }

        createInputMessage(
            ASK_SQUARE_METERS_EXTRA_BUILDING_TWO
        ) { body, userContext, message ->
            userContext.onBoardingData.houseExtraBuildingTwoSQM = (message.body as MessageBodyNumber).value
            addToChat(message)
            ASK_HAS_WATER_EXTRA_BUILDING_TWO.id
        }
        //TODO this.setExpectedReturnType(ASK_SQUARE_METERS_EXTRA_BUILDING_TWO.id, HouseExtraBuildingSQM())

        createInputMessage(
            ASK_HAS_WATER_EXTRA_BUILDING_TWO
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_EXTRA_BUILDING_HAS_WATER_YES.value -> {
                    userContext.onBoardingData.extraBuildingTwoHasWater = true
                }
                SELECT_EXTRA_BUILDING_HAS_WATER_NO.value -> {
                    userContext.onBoardingData.extraBuildingTwoHasWater = false
                }
            }
            if (userContext.onBoardingData.nrExtraBuildings <= 2) {
                ASK_SUBLETTING_HOUSE.id
            } else {
                ASK_SQUARE_METERS_EXTRA_BUILDING_THREE.id
            }
        }

        createInputMessage(
            ASK_SQUARE_METERS_EXTRA_BUILDING_THREE
        ) { body, userContext, message ->
            userContext.onBoardingData.houseExtraBuildingThreeSQM = (message.body as MessageBodyNumber).value
            addToChat(message)
            ASK_SQUARE_METERS_EXTRA_BUILDING_THREE.id
        }
        //TODO this.setExpectedReturnType(ASK_SQUARE_METERS_EXTRA_BUILDING_THREE.id, HouseExtraBuildingSQM())

        createInputMessage(
            ASK_HAS_WATER_EXTRA_BUILDING_THREE
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_EXTRA_BUILDING_HAS_WATER_YES.value -> {
                    userContext.onBoardingData.extraBuildingThreeHasWater = true
                }
                SELECT_EXTRA_BUILDING_HAS_WATER_NO.value -> {
                    userContext.onBoardingData.extraBuildingThreeHasWater = false
                }
            }
            if (userContext.onBoardingData.nrExtraBuildings <= 3) {
                ASK_SUBLETTING_HOUSE.id
            } else {
                ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR.id
            }
        }

        createInputMessage(
            ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR
        ) { body, userContext, message ->
            userContext.onBoardingData.houseExtraBuildingFourSQM = (message.body as MessageBodyNumber).value
            addToChat(message)
            ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR.id
        }
        //TODO this.setExpectedReturnType(ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR.id, HouseExtraBuildingSQM())

        createInputMessage(
            ASK_HAS_WATER_EXTRA_BUILDING_FOUR
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_EXTRA_BUILDING_HAS_WATER_YES.value -> {
                    userContext.onBoardingData.extraBuildingFourHasWater = true
                }
                SELECT_EXTRA_BUILDING_HAS_WATER_NO.value -> {
                    userContext.onBoardingData.extraBuildingFourHasWater = false
                }
            }
            ASK_SUBLETTING_HOUSE.id
        }

        createInputMessage(
            ASK_SUBLETTING_HOUSE
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            message.body.text = body.selectedItem.text
            when (body.selectedItem.value) {
                SELECT_SUBLETTING_HOUSE_YES.value -> {
                    userContext.onBoardingData.isSubLetting = true
                }
                else -> {
                    userContext.onBoardingData.isSubLetting = false
                }
            }
            HOUSE_CONVERSATION_DONE
        }
    }

    public override fun completeRequest(nxtMsg: String) {
        var nxtMsg = nxtMsg

        when (nxtMsg) {
            HOUSE_CONVERSATION_DONE -> {
                userContext.completeConversation(this)
                val conversation =
                    conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)
                userContext.startConversation(conversation, OnboardingConversationDevi.MESSAGE_50K_LIMIT)
            }

            "" -> {
                HouseOnboardingConversation.log.error("I dont know where to go next...")
                nxtMsg = "error"
            }
        }
        super.completeRequest(nxtMsg)
    }

    override fun init() {
        HouseOnboardingConversation.log.info("Starting house conversation")
        startConversation(HUS_FIRST.id)
    }


    override fun init(startMessage: String) {
        log.info("Starting house onboarding conversation with message: $startMessage")
        startConversation(startMessage) // Id of first message
    }

    override fun handleMessage(m: Message) {
        var nxtMsg = ""

        if (!validateReturnType(m)) {
            return
        }

        // Lambda
        if (this.hasSelectItemCallback(m.id) && m.body.javaClass == MessageBodySingleSelect::class.java) {
            // MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
            nxtMsg = this.execSelectItemCallback(m.id, m.body as MessageBodySingleSelect)
            addToChat(m)
        }

        val onBoardingData = userContext.onBoardingData

        // ... and then the incoming message id

        nxtMsg = m.id
        addToChat(m)

        completeRequest(nxtMsg)
    }

    override fun receiveEvent(e: Conversation.EventTypes, value: String) {
        when (e) {
            // This is used to let Hedvig say multiple message after another
            Conversation.EventTypes.MESSAGE_FETCHED -> {
                log.info("Message fetched: $value")

                // New way of handeling relay messages
                val relay = getRelay(value)

                if (relay != null) {
                    completeRequest(relay)
                }

            }
        }
    }

    override fun canAcceptAnswerToQuestion(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSelectItemsForAnswer(): List<SelectItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        private val log = LoggerFactory.getLogger(HouseOnboardingConversation::class.java)
    }


    private fun createInputMessage(
        message: SingleSelectMessage,
        callback: (MessageBodySingleSelect, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id,
            WrappedMessage(
                MessageBodySingleSelect(
                    message.text,
                    message.selectOptions.map { SelectOption(it.text, it.value) }
                ),
                receiveMessageCallback = callback
            )
        )
    }

    private fun createInputMessage(
        message: NumberInputMessage,
        callback: (MessageBodyNumber, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id,
            WrappedMessage(
                MessageBodyNumber(
                    message.text,
                    message.placeholder
                ),
                receiveMessageCallback = callback
            )
        )
    }

    fun createInputMessage(
        message: TextInputMessage,
        callback: (MessageBodyText, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id,
            WrappedMessage(
                MessageBodyText(
                    message.text,
                    message.textContentType,
                    message.keyboardType,
                    message.placeholder
                ),
                receiveMessageCallback = callback
            )
        )
    }
}

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

    val ASK_AGE = NumberInputMessage(
        "message.house.ask.age",
        "Hur gammal är du?",
        "Ålder"
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

    val ASK_SUBFACE = NumberInputMessage(
        "message.house.sub.face",
        "Vad är biytan på huset?${SPLIT}Exempel på utrymmen som räknas som biytor är förråd, kallvind, pannrum och garage som sitter ihop med huset.",
        "Bostadsyta"
    )

    val ASK_BUILDING_YEAR = NumberInputMessage(
        "message.house.building.year",
        "Vilket år byggdes huset?",
        "åååå"
    )

    val ASK_RESIDENTS = NumberInputMessage(
        "message.house.residents",
        "Okej! Hur många bor där?",
        "Boende"
    )

    val ASK_BATHROOMS = NumberInputMessage(
        "message.house.bathrooms",
        "Hur många badrum har du?",
        "Badrum"
    )

    val SELECT_EXTRA_BUILDING_YES = SingleSelectOption("message.house.extra.buildings.yes", "Ja, det har jag")
    val SELECT_EXTRA_BUILDING_NO = SingleSelectOption("message.house.extra.buildings.no", "Nej, gå vidare")
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

    val ASK_SQUARE_METERS_EXTRA_BUILDING_ONE = NumberInputMessage(
        "message.house.square.meters.building.one",
        "Hur stor är extra byggnad ett?",
        "kvm"
    )

    val ASK_SQUARE_METERS_EXTRA_BUILDING_TWO = NumberInputMessage(
        "message.house.square.meters.building.two",
        "Hur stor är extra byggnad två?",
        "kvm"
    )

    val ASK_SQUARE_METERS_EXTRA_BUILDING_THREE = NumberInputMessage(
        "message.house.square.meters.building.three",
        "Hur stor är extra byggnad tre?",
        "kvm"
    )

    val ASK_SQUARE_METERS_EXTRA_BUILDING_FOUR = NumberInputMessage(
        "message.house.square.meters.building.four",
        "Hur stor är extra byggnad fyra?",
        "kvm"
    )

    val SELECT_EXTRA_BUILDING_HAS_WATER_YES = SingleSelectOption("message.house.extra.building.has.water.yes", "Ja")
    val SELECT_EXTRA_BUILDING_HAS_WATER_NO = SingleSelectOption("message.house.extra.building.has.water.no", "Nej")
    val ASK_HAS_WATER_EXTRA_BUILDING_ONE = SingleSelectMessage(
        "message.house.has.water.building.one",
        "Finns det indraget vatten till byggnad ett?",
        listOf(
            SELECT_EXTRA_BUILDING_HAS_WATER_YES,
            SELECT_EXTRA_BUILDING_HAS_WATER_NO
        )
    )

    val ASK_HAS_WATER_EXTRA_BUILDING_TWO = SingleSelectMessage(
        "message.house.has.water.building.two",
        "Finns det indraget vatten till byggnad två?",
        listOf(
            SELECT_EXTRA_BUILDING_HAS_WATER_YES,
            SELECT_EXTRA_BUILDING_HAS_WATER_NO
        )
    )

    val ASK_HAS_WATER_EXTRA_BUILDING_THREE = SingleSelectMessage(
        "message.house.has.water.building.three",
        "Finns det indraget vatten till byggnad tree?",
        listOf(
            SELECT_EXTRA_BUILDING_HAS_WATER_YES,
            SELECT_EXTRA_BUILDING_HAS_WATER_NO
        )
    )

    val ASK_HAS_WATER_EXTRA_BUILDING_FOUR = SingleSelectMessage(
        "message.house.has.water.building.four",
        "Finns det indraget vatten till byggnad fyra?",
        listOf(
            SELECT_EXTRA_BUILDING_HAS_WATER_YES,
            SELECT_EXTRA_BUILDING_HAS_WATER_NO
        )
    )

    val SELECT_SUBLETTING_HOUSE_YES = SingleSelectOption("message.house.sublet.yes", "yes")
    val SELECT_SUBLETTING_HOUSE_NO = SingleSelectOption("message.house.sublet.no", "no")
    val ASK_SUBLETTING_HOUSE = SingleSelectMessage(
        "message.house.supletting.house",
        "Super! Ett par frågor till \uD83D\uDE0A${SPLIT}Hyr du ut någon del av ditt hus till någon?",
        listOf(
            SELECT_SUBLETTING_HOUSE_YES,
            SELECT_SUBLETTING_HOUSE_NO
        )
    )
}

/*
val  = NumberInputMessage(
        "message.house",
        "",
        ""
    )

    val  = TextInputMessage(
        "message.house",
        "",
        ""
    )

val  = SingleSelectMessage(
        "message.house",
        "",
        listOf()
    )

    val SELECT_ = SingleSelectOption("message.house", "")
 */

data class SingleSelectMessage(
    val id: String,
    val text: String,
    val selectOptions: List<SingleSelectOption>
)

data class NumberInputMessage(
    val id: String,
    val text: String,
    val placeholder: String
)

data class TextInputMessage(
    val id: String,
    val text: String,
    val textContentType: TextContentType,
    val keyboardType: KeyboardType,
    val placeholder: String
)

data class SingleSelectOption(
    val value: String,
    val text: String
)
