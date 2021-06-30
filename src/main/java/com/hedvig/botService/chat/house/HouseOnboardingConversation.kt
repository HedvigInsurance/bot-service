package com.hedvig.botService.chat.house

import com.hedvig.botService.utils.ssnLookupAndStore
import com.hedvig.botService.utils.storeAndTrimAndAddSSNToChat
import com.hedvig.botService.utils.storeFamilyName
import com.hedvig.botService.chat.*
import com.hedvig.botService.chat.FreeChatConversation.FREE_CHAT_ONBOARDING_START
import com.hedvig.botService.chat.OnboardingConversationDevi.ProductTypes
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ANCILLARY_AREA
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HAS_EXTRA_BUILDINGS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HAS_WATER_EXTRA_BUILDING
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_LAST_NAME
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ADDRESS_LOOK_UP_SUCCESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_EXTRA_BUILDING_TYPE_MORE_THAN_ONE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_EXTRA_BUILDING_TYPE_ONE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_BATHROOMS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_EXTRA_BUILDINGS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HOUSE_HOUSEHOLD_MEMBERS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_NO
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_HOUSE_OR_APARTMENT
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_MORE_EXTRA_BUILDING_TYPE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_REAL_ESTATE_LOOKUP_CORRECT
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS_EXTRA_BUILDING
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SQUARE_METERS_FAILED_LOOKUP
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SSN
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SSN_UNDER_EIGHTEEN
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_STREET_ADDRESS
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_SUBLETTING_HOUSE
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_YEAR_OF_CONSTRUCTION
import com.hedvig.botService.chat.house.HouseConversationConstants.ASK_ZIP_CODE
import com.hedvig.botService.chat.house.HouseConversationConstants.CONVERSATION_APARTMENT_DONE
import com.hedvig.botService.chat.house.HouseConversationConstants.CONVERSATION_RENT_DONE
import com.hedvig.botService.chat.house.HouseConversationConstants.HOUSE_CONVERSATION_DONE
import com.hedvig.botService.chat.house.HouseConversationConstants.HOUSE_FIRST
import com.hedvig.botService.chat.house.HouseConversationConstants.IN_LOOP_ASK_EXTRA_BUILDING_TYPE
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_BATHROOMS_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_EXTRA_BUILDINGS_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_EXTRA_BUILDING_SQM_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_FLOORS_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_HOUSEHOLD_MEMBERS_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_SQM_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_TOTAL_SQM_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.MORE_YEAR_OF_CONSTRUCTION_QUESTIONS_CALL
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_MORE_THAN_FOUR_FLOORS
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_ATTEFALL
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_FRIGGEBO
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_GARAGE
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_NO
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_HAS_WATER_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_ADDRESS_LOOK_UP_SUCCESS_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_APARTMENT
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_BOATHOUSE
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_CARPORT
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_GUESTHOUSE
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_EXTRA_BUILDING_SAUNA
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_RENT
import com.hedvig.botService.chat.house.HouseConversationConstants.SELECT_SUBLETTING_HOUSE_YES
import com.hedvig.botService.dataTypes.*
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.serviceIntegration.lookupService.LookupService
import com.hedvig.botService.serviceIntegration.lookupService.dto.RealEstateDto
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.Nationality
import com.hedvig.botService.serviceIntegration.productPricing.dto.ExtraBuildingType
import com.hedvig.botService.services.events.HouseUnderwritingLimitCallMeExceedsEvent
import com.hedvig.botService.utils.ConversationUtils.isYoungerThan18
import com.hedvig.botService.utils.MessageUtil
import com.hedvig.libs.translations.Translations
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher

class HouseOnboardingConversation
constructor(
    private val memberService: MemberService,
    private val lookupService: LookupService,
    override var eventPublisher: ApplicationEventPublisher,
    private val conversationFactory: ConversationFactory,
    translations: Translations,
    userContext: UserContext
) : Conversation(eventPublisher, translations, userContext) {

    var queuePos: Int? = null

    init {
        createInputMessage(
            HOUSE_FIRST
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
            handleSsnResponse(body, message)
        }
        this.setExpectedReturnType(ASK_SSN.id, SSNSweden())

        createInputMessage(
            ASK_SSN_UNDER_EIGHTEEN
        ) { body, userContext, message ->
            handleSsnResponse(body, message)
        }
        this.setExpectedReturnType(ASK_SSN_UNDER_EIGHTEEN.id, SSNSweden())

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
            if (userContext.hasHouseProduct()) {
                realEstateLookup()
            } else {
                ASK_SQUARE_METERS.id
            }
        }
        this.setExpectedReturnType(ASK_ZIP_CODE.id, ZipCodeSweden())

        createInputMessage(
            ASK_SQUARE_METERS
        ) { body, userContext, message ->
            handleSquareMetersResponse(message)
        }
        this.setExpectedReturnType(ASK_SQUARE_METERS.id, HouseLivingSpaceSquareMeters())

        createInputMessage(
            ASK_SQUARE_METERS_FAILED_LOOKUP
        ) { body, userContext, message ->
            handleSquareMetersResponse(message)
        }
        this.setExpectedReturnType(ASK_SQUARE_METERS_FAILED_LOOKUP.id, HouseLivingSpaceSquareMeters())

        createInputMessage(
            ASK_ANCILLARY_AREA
        ) { body, userContext, message ->
            val ancillaryArea = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.houseAncillaryArea = ancillaryArea
            addToChat(message)
            if (ancillaryArea + userContext.onBoardingData.livingSpace > MAX_LIVING_SPACE_INCLUDING_ANCILLARY_AREA_SQM) {
                MORE_TOTAL_SQM_QUESTIONS_CALL.id
            } else {
                ASK_YEAR_OF_CONSTRUCTION.id
            }
        }
        this.setExpectedReturnType(ASK_ANCILLARY_AREA.id, AncillaryAreaSquareMeters())

        createInputMessage(
            ASK_YEAR_OF_CONSTRUCTION
        ) { body, userContext, message ->
            val yearOfConstruction = (message.body as MessageBodyNumber).value
            userContext.onBoardingData.yearOfConstruction = yearOfConstruction
            addToChat(message)
            if (yearOfConstruction < MIN_YEAR_OF_CONSTRUCTION) {
                MORE_YEAR_OF_CONSTRUCTION_QUESTIONS_CALL.id
            } else {
                ASK_NUMBER_OF_BATHROOMS.id
            }
        }
        this.setExpectedReturnType(ASK_YEAR_OF_CONSTRUCTION.id, HouseYearOfConstruction())

        createInputMessage(
            ASK_NUMBER_OF_BATHROOMS
        ) { body, userContext, message ->
            handleNumberOfBathroomsResponse(message)
        }
        this.setExpectedReturnType(ASK_NUMBER_OF_BATHROOMS.id, HouseBathrooms())

        createInputMessage(
            ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP
        ) { body, userContext, message ->
            handleNumberOfBathroomsResponse(message)
        }
        this.setExpectedReturnType(ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP.id, HouseBathrooms())

        createInputMessage(
            ASK_HOUSE_HOUSEHOLD_MEMBERS
        ) { body, userContext, message ->
            handleHouseholdMembersResponse(message)
        }
        this.setExpectedReturnType(ASK_HOUSE_HOUSEHOLD_MEMBERS.id, HouseholdMemberNumber())

        createInputMessage(
            ASK_SUBLETTING_HOUSE
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_SUBLETTING_HOUSE_YES.value -> {
                    userContext.onBoardingData.isSubLetting = true
                    ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_YES.id
                }
                else -> {
                    userContext.onBoardingData.isSubLetting = false
                    ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_NO.id
                }
            }
        }

        createInputMessage(
            ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_YES
        ) { body, userContext, message ->
            handleFloorsResponse(body, message)
        }

        createInputMessage(
            ASK_HOUSE_HAS_MORE_THAN_FOUR_FLOORS_FROM_NO
        ) { body, userContext, message ->
            handleFloorsResponse(body, message)
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
                    userContext.onBoardingData.nrExtraBuildings = 0
                    HOUSE_CONVERSATION_DONE
                }
            }
        }

        createInputMessage(
            ASK_NUMBER_OF_EXTRA_BUILDINGS
        ) { body, userContext, message ->
            addToChat(message)
            userContext.onBoardingData.nrExtraBuildings = body.value
            when {
                body.value == 0 -> {
                    HOUSE_CONVERSATION_DONE
                }
                body.value > MAX_NUMBER_OF_EXTRA_BUILDING -> {
                    MORE_EXTRA_BUILDINGS_QUESTIONS_CALL.id
                }
                body.value == 1 -> {
                    ASK_EXTRA_BUILDING_TYPE_ONE.id
                }
                else -> {
                    ASK_EXTRA_BUILDING_TYPE_MORE_THAN_ONE.id
                }
            }
        }
        this.setExpectedReturnType(ASK_NUMBER_OF_EXTRA_BUILDINGS.id, HouseExtraBuildings())

        createInputMessage(
            ASK_EXTRA_BUILDING_TYPE_ONE
        ) { body, userContext, message ->
            handleExtraBuildingTypeResponse(body, userContext, message, 1)
        }

        createInputMessage(
            ASK_EXTRA_BUILDING_TYPE_MORE_THAN_ONE
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
                ASK_MORE_EXTRA_BUILDING_TYPE,
                buildingNumber
            ) { body, userContext, message ->
                handleMoreExtraBuildingTypeResponse(body, userContext, message, buildingNumber)
            }

            createInputMessage(
                ASK_SQUARE_METERS_EXTRA_BUILDING,
                buildingNumber
            ) { body, userContext, message ->
                val extraBuildingSQM = (message.body as MessageBodyNumber).value
                userContext.onBoardingData.setHouseExtraBuildingSQM(
                    extraBuildingSQM,
                    buildingNumber
                )
                addToChat(message)
                if (extraBuildingSQM > MAX_EXTRA_BUILDING_SQM) {
                    MORE_EXTRA_BUILDING_SQM_QUESTIONS_CALL.id
                } else {
                    ASK_HAS_WATER_EXTRA_BUILDING.id + buildingNumber
                }
            }
            this.setExpectedReturnType(ASK_SQUARE_METERS_EXTRA_BUILDING.id + buildingNumber, HouseExtraBuildingSQM())

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
                    HOUSE_CONVERSATION_DONE
                } else {
                    IN_LOOP_ASK_EXTRA_BUILDING_TYPE.id + (1 + buildingNumber)
                }
            }
        }

        createInputMessage(
            ASK_ADDRESS_LOOK_UP_SUCCESS
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_ADDRESS_LOOK_UP_SUCCESS_YES.value -> {
                    if (userContext.hasHouseProduct()) {
                        realEstateLookup()
                    } else {
                        ASK_SQUARE_METERS.id
                    }
                }
                else -> ASK_STREET_ADDRESS.id
            }
        }

        createInputMessage(
            ASK_REAL_ESTATE_LOOKUP_CORRECT
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_REAL_ESTATE_LOOKUP_CORRECT_YES.value -> {
                    when {
                        userContext.onBoardingData.livingSpace > MAX_LIVING_SPACE_SQM ->
                            MORE_SQM_QUESTIONS_CALL.id
                        (userContext.onBoardingData.houseAncillaryArea +
                                userContext.onBoardingData.livingSpace) > MAX_LIVING_SPACE_INCLUDING_ANCILLARY_AREA_SQM ->
                            MORE_TOTAL_SQM_QUESTIONS_CALL.id
                        userContext.onBoardingData.yearOfConstruction < MIN_YEAR_OF_CONSTRUCTION ->
                            MORE_YEAR_OF_CONSTRUCTION_QUESTIONS_CALL.id
                        else ->
                            ASK_NUMBER_OF_BATHROOMS_FROM_SUCCESS_LOOKUP.id
                    }
                }
                else -> ASK_SQUARE_METERS.id
            }
        }

        addAskMoreQuestionsMessage(MORE_SQM_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_HOUSEHOLD_MEMBERS_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_TOTAL_SQM_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_YEAR_OF_CONSTRUCTION_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_FLOORS_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_BATHROOMS_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_EXTRA_BUILDINGS_QUESTIONS_CALL)
        addAskMoreQuestionsMessage(MORE_EXTRA_BUILDING_SQM_QUESTIONS_CALL)

        //To be able edit house/apartment answer
        createInputMessage(
            ASK_HOUSE_OR_APARTMENT
        ) { body, userContext, message ->
            message.body.text = body.selectedItem.text
            addToChat(message)
            when (body.selectedItem.value) {
                SELECT_APARTMENT.value -> {
                    userContext.completeConversation(this)
                    val conversation =
                        conversationFactory.createConversation(OnboardingConversationDevi::class.java, userContext)
                    userContext.startConversation(
                        conversation,
                        OnboardingConversationDevi.MESSAGE_LAGENHET_NO_PERSONNUMMER
                    )
                    CONVERSATION_APARTMENT_DONE
                }
                else -> ASK_SSN.id
            }
        }
    }

    private fun realEstateLookup(): String =
        lookupService.realEstateLookup(
            userContext.memberId,
            RealEstateDto(
                userContext.onBoardingData.addressStreet,
                userContext.onBoardingData.addressZipCode.replace(" ", "")
            )
        )?.let { realEstate ->
            userContext.onBoardingData.apply {
                houseAncillaryArea = realEstate.ancillaryArea
                yearOfConstruction = realEstate.yearOfConstruction
                livingSpace = realEstate.livingSpace.toFloat()
            }

            ASK_REAL_ESTATE_LOOKUP_CORRECT.id
        } ?: ASK_SQUARE_METERS.id

    private fun handleSsnResponse(body: MessageBodyNumber, message: Message): String {
        val (trimmedSSN, memberBirthDate) = userContext.storeAndTrimAndAddSSNToChat(body) {
            message.body.text = it
            addToChat(message)
            it
        }

        if (isYoungerThan18(memberBirthDate)) {
            return ASK_SSN_UNDER_EIGHTEEN.id
        }

        val hasAddress = memberService.ssnLookupAndStore(userContext, trimmedSSN, Nationality.SWEDEN)

        return if (hasAddress) {
            ASK_ADDRESS_LOOK_UP_SUCCESS.id
        } else {
            ASK_LAST_NAME.id
        }
    }

    private fun addAskMoreQuestionsMessage(message: NumberInputMessage) {
        createInputMessage(
            message
        ) { body, userContext, message ->
            addToChat(message)
            userContext.completeConversation(this)

            val phoneNumber = message.body.text
            userContext.onBoardingData.phoneNumber = phoneNumber

            val reason = MessageUtil.getBaseMessageId(message.id)
                .replace("message.house.more.questions.call.", "")
                .replace(".", " ")

            eventPublisher.publishEvent(
                HouseUnderwritingLimitCallMeExceedsEvent(
                    userContext.memberId,
                    userContext.onBoardingData.firstName,
                    userContext.onBoardingData.familyName,
                    phoneNumber,
                    reason
                )
            )
            val conversation = conversationFactory.createConversation(FreeChatConversation::class.java, userContext)
            userContext.startConversation(conversation, FREE_CHAT_ONBOARDING_START)
            FREE_CHAT_ONBOARDING_START
        }
    }

    private fun handleFloorsResponse(body: MessageBodySingleSelect, message: Message): String {
        message.body.text = body.selectedItem.text
        addToChat(message)
        return when (body.selectedItem.value) {
            SELECT_MORE_THAN_FOUR_FLOORS.value -> {
                MORE_FLOORS_QUESTIONS_CALL.id
            }
            else -> {
                ASK_HAS_EXTRA_BUILDINGS.id
            }
        }
    }

    private fun handleSquareMetersResponse(message: Message): String {
        val livingSpace = (message.body as MessageBodyNumber).value.toFloat()
        userContext.onBoardingData.livingSpace = livingSpace
        addToChat(message)
        return if (userContext.hasHouseProduct()) {
            if (livingSpace > MAX_LIVING_SPACE_SQM) {
                MORE_SQM_QUESTIONS_CALL.id
            } else {
                ASK_ANCILLARY_AREA.id
            }
        } else {
            if (livingSpace > OnboardingConversationDevi.MAX_LIVING_SPACE_RENT_SQM) {
                MORE_SQM_QUESTIONS_CALL.id
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
    }

    private fun handleNumberOfBathroomsResponse(message: Message): String {
        val bathrooms = (message.body as MessageBodyNumber).value
        userContext.onBoardingData.numberOfBathrooms = bathrooms
        addToChat(message)
        return if (bathrooms > MAX_NUMBER_OF_BATHROOMS) {
            MORE_BATHROOMS_QUESTIONS_CALL.id
        } else {
            ASK_HOUSE_HOUSEHOLD_MEMBERS.id
        }
    }

    private fun handleHouseholdMembersResponse(message: Message): String {
        val nrPersons = (message.body as MessageBodyNumber).value
        userContext.onBoardingData.setPersonInHouseHold(nrPersons)
        addToChat(message)
        return if (nrPersons > MAX_NUMBER_OF_HOUSE_HOLD_MEMBERS) {
            MORE_HOUSEHOLD_MEMBERS_QUESTIONS_CALL.id
        } else {
            ASK_SUBLETTING_HOUSE.id
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
        return when (body.selectedItem.value) {
            SELECT_EXTRA_BUILDING_GARAGE.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.GARAGE,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
                ASK_SQUARE_METERS_EXTRA_BUILDING.id + buildingNumber
            }
            SELECT_EXTRA_BUILDING_FRIGGEBO.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.FRIGGEBOD,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
                ASK_SQUARE_METERS_EXTRA_BUILDING.id + buildingNumber
            }
            SELECT_EXTRA_BUILDING_ATTEFALL.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.ATTEFALL,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
                ASK_SQUARE_METERS_EXTRA_BUILDING.id + buildingNumber
            }
            else -> {
                ASK_MORE_EXTRA_BUILDING_TYPE.id + buildingNumber
            }
        }
    }

    private fun handleMoreExtraBuildingTypeResponse(
        body: MessageBodySingleSelect,
        userContext: UserContext,
        message: Message,
        buildingNumber: Int
    ): String {
        message.body.text = body.selectedItem.text
        addToChat(message)
        when (body.selectedItem.value) {

            SELECT_EXTRA_BUILDING_GUESTHOUSE.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.GUESTHOUSE,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
            }
            SELECT_EXTRA_BUILDING_CARPORT.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.CARPORT,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
            }
            SELECT_EXTRA_BUILDING_SAUNA.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.SAUNA,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
            }
            SELECT_EXTRA_BUILDING_BOATHOUSE.value -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.BOATHOUSE,
                    buildingNumber,
                    this.userContext.locale,
                    translations
                )
            }
            else -> {
                userContext.onBoardingData.setHouseExtraBuildingType(
                    ExtraBuildingType.OTHER,
                    buildingNumber,
                    this.userContext.locale,
                    translations
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
        startConversation(HOUSE_FIRST.id)
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

    private fun UserContext.hasHouseProduct() =
        this.onBoardingData.houseType == ProductTypes.HOUSE.toString()

    companion object {
        private val log = LoggerFactory.getLogger(HouseOnboardingConversation::class.java)

        private const val MAX_LIVING_SPACE_SQM = 250
        private const val MAX_LIVING_SPACE_INCLUDING_ANCILLARY_AREA_SQM = 300

        private const val MIN_YEAR_OF_CONSTRUCTION = 1925

        private const val MAX_NUMBER_OF_HOUSE_HOLD_MEMBERS = 6

        private const val MAX_NUMBER_OF_BATHROOMS = 2

        private const val MAX_NUMBER_OF_EXTRA_BUILDING = 4
        private const val MAX_EXTRA_BUILDING_SQM = 75
    }
}
