package com.hedvig.botService.chat.house

import com.hedvig.botService.Utils.ssnLookupAndStore
import com.hedvig.botService.Utils.storeAndTrimAndAddSSNToChat
import com.hedvig.botService.Utils.storeFamilyName
import com.hedvig.botService.chat.*
import com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_ONBOARDING_START
import com.hedvig.botService.chat.OnboardingConversationDevi.ProductTypes
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ANCILLARY_AREA
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_EXTRA_BUILDING_TYPE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HAS_EXTRA_BUILDINGS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_LAST_NAME
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_LOOK_UP_SUCCESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_BATHROOMS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_EXTRA_BUILDINGS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_RESIDENTS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SSN
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_STREET_ADDRESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SUBLETTING_HOUSE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_YEAR_OF_CONSTRUCTION
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ZIP_CODE
import com.hedvig.botService.chat.house.HouseConversationConstants.CONVERSATION_RENT_DONE
import com.hedvig.botService.chat.house.HouseConversationConstants.HOUSE_CONVERSATION_DONE
import com.hedvig.botService.chat.house.HouseConversationConstants.HUS_FIRST
import com.hedvig.botService.chat.house.HouseConversationConstants.IN_LOOP_ASK_EXTRA_BUILDING_TYPE
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_ATTEFALS
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_FRIGGEBO
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_GARAGE
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_NO
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_LOOK_UP_SUCCESS_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_RENT
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_SUBLETTING_HOUSE_YES
import com.hedvig.botService.dataTypes.*
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.productPricing.dto.ExtraBuildingType
import com.hedvig.botService.services.LocalizationService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher

class HouseOnboardingConversation
constructor(
    private val memberService: MemberService,
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
                    userContext.onBoardingData.houseType = ProductTypes.RENT.toString()
                    ASK_SSN.id
                }
                else -> {
                    userContext.onBoardingData.houseType = ProductTypes.HOUSE.toString()
                    ASK_SSN.id
                }
            }
        }

        createInputMessage(
            ASK_SSN
        ) { body, userContext, message ->
            val trimmedSSN = userContext.storeAndTrimAndAddSSNToChat(body) {
                message.body.text = it
                addToChat(message)
                it
            }

            val hasAddress = memberService.ssnLookupAndStore(userContext, trimmedSSN)

            if (hasAddress) {
                ASK_LOOK_UP_SUCCESS.id
            } else {
                ASK_LAST_NAME.id
            }
        }
        this.setExpectedReturnType(ASK_SSN.id, SSNSweden())

        createInputMessage(
            ASK_LAST_NAME
        ) { body, userContext, message ->
            userContext.storeFamilyName(body)
            addToChat(message)
            ASK_STREET_ADDRESS.id
        }

        createInputMessage(
            ASK_STREET_ADDRESS
        ) { _, userContext, message ->
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
        // TODO Visma look up
        createInputMessage(
            ASK_SQUARE_METERS
        ) { body, userContext, message ->
            userContext.onBoardingData.livingSpace = (message.body as MessageBodyNumber).value.toFloat()
            addToChat(message)
            if (userContext.onBoardingData.houseType == ProductTypes.HOUSE.toString()) {
                ASK_ANCILLARY_AREA.id
            } else {
                userContext.completeConversation(this)
                val conversation =
                    conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)
                userContext.startConversation(
                    conversation,
                    OnboardingConversationDevi.MESSAGE_ASK_NR_RESIDENTS
                )
                CONVERSATION_RENT_DONE
            }
        }
        this.setExpectedReturnType(ASK_SQUARE_METERS.id, LivingSpaceSquareMeters())

        createInputMessage(
            ASK_ANCILLARY_AREA
        ) { body, userContext, message ->
            userContext.onBoardingData.houseAncillaryArea = (message.body as MessageBodyNumber).value
            addToChat(message)
            ASK_RESIDENTS.id
        }
        this.setExpectedReturnType(ASK_ANCILLARY_AREA.id, AncillaryAreaSquareMeters())

        createInputMessage(
            ASK_RESIDENTS
        ) { body, userContext, message ->
            val nrPersons = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.setPersonInHouseHold(nrPersons)
            addToChat(message)
            ASK_NUMBER_OF_BATHROOMS.id
        }
        //TODO check if same as apartment
        this.setExpectedReturnType(ASK_RESIDENTS.id, HouseholdMemberNumber())

        createInputMessage(
            ASK_NUMBER_OF_BATHROOMS
        ) { body, userContext, message ->
            val bathrooms = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.numberOfBathrooms = bathrooms
            addToChat(message)
            ASK_YEAR_OF_CONSTRUCTION.id
        }
        this.setExpectedReturnType(ASK_NUMBER_OF_BATHROOMS.id, HouseBathrooms())

        createInputMessage(
            ASK_YEAR_OF_CONSTRUCTION
        ) { body, userContext, message ->
            val bathrooms = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.yearOfConstruction = bathrooms
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
            ASK_SUBLETTING_HOUSE
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
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

        createInputMessage(
            ASK_NUMBER_OF_EXTRA_BUILDINGS
        ) { body, userContext, message ->
            addToChat(message)
            when {
                body.value == 0 -> {
                    ASK_SUBLETTING_HOUSE.id
                }
                body.value >= 5 -> {
                    userContext.onBoardingData.nrExtraBuildings = body.value
                    MORE_QUESTIONS_CALL.id
                }
                else -> {
                    userContext.onBoardingData.nrExtraBuildings = body.value
                    ASK_EXTRA_BUILDING_TYPE.id
                }
            }
        }
        this.setExpectedReturnType(ASK_RESIDENTS.id, HouseExtraBuildings())

        createInputMessage(
            ASK_EXTRA_BUILDING_TYPE
        ) { body, userContext, message ->
            handleExtraBuildingTypeResponse(body, userContext, message, 1)
        }

        for (buildingNumber in 1..4) {
            if (buildingNumber != 1) {
                createInputMessage(
                    IN_LOOP_ASK_EXTRA_BUILDING_TYPE,
                    buildingNumber
                ) { body, userContext, message ->
                    handleExtraBuildingTypeResponse(body, userContext, message, buildingNumber)
                }
            }

            createInputMessage(
                ASK_SQUARE_METERS_EXTRA_BUILDING,
                buildingNumber
            ) { body, userContext, message ->
                userContext.onBoardingData.setHouseExtraBuildingSQM(
                    (message.body as MessageBodyNumber).value,
                    buildingNumber
                )
                addToChat(message)
                ASK_HAS_WATER_EXTRA_BUILDING.id + buildingNumber
            }
            this.setExpectedReturnType(ASK_HAS_WATER_EXTRA_BUILDING.id + buildingNumber, HouseExtraBuildingSQM())

            createInputMessage(
                ASK_HAS_WATER_EXTRA_BUILDING,
                buildingNumber
            ) { body, userContext, message ->
                message.body.text = body.selectedItem.text
                addToChat(message)
                when (body.selectedItem.value) {
                    SELECT_EXTRA_BUILDING_HAS_WATER_YES.value -> {
                        userContext.onBoardingData.setHouseExtraBuildingHasWater(true, buildingNumber)
                    }
                    SELECT_EXTRA_BUILDING_HAS_WATER_NO.value -> {
                        userContext.onBoardingData.setHouseExtraBuildingHasWater(false, buildingNumber)
                    }
                }
                if (userContext.onBoardingData.nrExtraBuildings <= buildingNumber) {
                    ASK_SUBLETTING_HOUSE.id
                } else {
                    IN_LOOP_ASK_EXTRA_BUILDING_TYPE.id + (1 + buildingNumber)
                }
            }
        }

        createInputMessage(
            MORE_QUESTIONS_CALL
        ) { body, userContext, message ->
            userContext.completeConversation(this)
            val conversation = conversationFactory.createConversation(FreeChatConversation::class.java, userContext)
            userContext.startConversation(conversation, FREE_CHAT_ONBOARDING_START)
            FREE_CHAT_ONBOARDING_START
        }

        createInputMessage(
            ASK_LOOK_UP_SUCCESS
        ) { body, userContext, message ->
            when (body.selectedItem.value) {
                // TODO: Visma look up
                SELECT_LOOK_UP_SUCCESS_YES.value -> ASK_SQUARE_METERS.id
                else -> ASK_STREET_ADDRESS.id
            }
        }
    }

    private fun handleExtraBuildingTypeResponse(
        body: MessageBodySingleSelect,
        userContext: UserContext,
        message: Message,
        buildingNumber: Int
    ): String {
        message.body.text = body.selectedItem.text
        addToChat(message)
        when (body.selectedItem.value) {

            SELECT_EXTRA_BUILDING_GARAGE.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.GARAGE,
                    buildingNumber,
                    this.userContext.locale,
                    localizationService
                )
            }
            SELECT_EXTRA_BUILDING_FRIGGEBO.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.FRIGGEBOD,
                    buildingNumber,
                    this.userContext.locale,
                    localizationService
                )
            }
            SELECT_EXTRA_BUILDING_ATTEFALS.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.ATTEFALL,
                    buildingNumber,
                    this.userContext.locale,
                    localizationService
                )
            }
            else -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.OTHER,
                    buildingNumber,
                    this.userContext.locale,
                    localizationService
                )
            }
        }
        return ASK_SQUARE_METERS_EXTRA_BUILDING.id + buildingNumber
    }

    public override fun completeRequest(nxtMsg: String) {
        var nxtMsg = nxtMsg

        when (nxtMsg) {
            HOUSE_CONVERSATION_DONE -> {
                userContext.completeConversation(this)
                val conversation =
                    conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)
                userContext.startConversation(
                    conversation,
                    OnboardingConversationDevi.MESSAGE_50K_LIMIT
                )
            }

            "" -> {
                log.error("I dont know where to go next...")
                nxtMsg = "error"
            }
        }
        super.completeRequest(nxtMsg)
    }

    override fun init() {
        log.info("Starting house conversation")
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

    override fun receiveEvent(e: EventTypes, value: String) {
        when (e) {
            // This is used to let Hedvig say multiple message after another
            EventTypes.MESSAGE_FETCHED -> {
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
        ordinal: Int? = null,
        callback: (MessageBodySingleSelect, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id + (ordinal ?: ""),
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
        ordinal: Int? = null,
        callback: (MessageBodyNumber, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id + (ordinal ?: ""),
            WrappedMessage(
                MessageBodyNumber(
                    message.text,
                    message.placeholder
                ),
                receiveMessageCallback = callback
            )
        )
    }

    private fun createInputMessage(
        message: TextInputMessage,
        ordinal: Int? = null,
        callback: (MessageBodyText, UserContext, Message) -> String
    ) {
        this.createChatMessage(
            message.id + (ordinal ?: ""),
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
