package com.hedvig.botService.chat

import com.google.common.collect.Lists
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hedvig.botService.chat.MainConversation.Companion.MESSAGE_HEDVIG_COM_POST_LOGIN
import com.hedvig.botService.chat.house.HouseConversationConstants
import com.hedvig.botService.chat.house.HouseOnboardingConversation
import com.hedvig.botService.config.SwitchableInsurers
import com.hedvig.botService.dataTypes.EmailAdress
import com.hedvig.botService.dataTypes.HouseholdMemberNumber
import com.hedvig.botService.dataTypes.LivingSpaceSquareMeters
import com.hedvig.botService.dataTypes.SSNSweden
import com.hedvig.botService.dataTypes.TextInput
import com.hedvig.botService.dataTypes.ZipCodeSweden
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.KeyboardType
import com.hedvig.botService.enteties.message.Message
import com.hedvig.botService.enteties.message.MessageBody
import com.hedvig.botService.enteties.message.MessageBodyBankIdCollect
import com.hedvig.botService.enteties.message.MessageBodyMultipleSelect
import com.hedvig.botService.enteties.message.MessageBodyNumber
import com.hedvig.botService.enteties.message.MessageBodyParagraph
import com.hedvig.botService.enteties.message.MessageBodySingleSelect
import com.hedvig.botService.enteties.message.MessageBodyText
import com.hedvig.botService.enteties.message.MessageHeader
import com.hedvig.botService.enteties.message.SelectItem
import com.hedvig.botService.enteties.message.SelectLink
import com.hedvig.botService.enteties.message.SelectOption
import com.hedvig.botService.enteties.message.TextContentType
import com.hedvig.botService.enteties.userContextHelpers.UserData
import com.hedvig.botService.enteties.userContextHelpers.UserData.IS_STUDENT
import com.hedvig.botService.enteties.userContextHelpers.UserData.LOGIN
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.Flag
import com.hedvig.botService.serviceIntegration.memberService.dto.Nationality
import com.hedvig.botService.serviceIntegration.memberService.exceptions.ErrorType
import com.hedvig.botService.serviceIntegration.underwriter.Underwriter
import com.hedvig.botService.services.events.MemberSignedEvent
import com.hedvig.botService.services.events.OnboardingCallForQuoteEvent
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent
import com.hedvig.botService.services.events.RequestObjectInsuranceEvent
import com.hedvig.botService.services.events.RequestStudentObjectInsuranceEvent
import com.hedvig.botService.services.events.UnderwritingLimitExcededEvent
import com.hedvig.botService.utils.ConversationUtils
import com.hedvig.botService.utils.ssnLookupAndStore
import com.hedvig.botService.utils.storeAndTrimAndAddSSNToChat
import com.hedvig.libs.translations.Translations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.ArrayList

class OnboardingConversationDevi(
    private val memberService: MemberService,
    private val underwriter: Underwriter,
    eventPublisher: ApplicationEventPublisher,
    private val conversationFactory: ConversationFactory,
    translations: Translations,
    @Value("\${hedvig.appleUser.email}")
    private val appleUserEmail: String,
    @Value("\${hedvig.appleUser.password}")
    private val appleUserPassword: String,
    private val phoneNumberUtil: PhoneNumberUtil,
    userContext: UserContext
) : Conversation(eventPublisher, translations, userContext), BankIdChat {

    var queuePos: Int? = null

    enum class ProductTypes {
        BRF,
        RENT,
        RENT_BRF,
        SUBLET_RENTAL,
        SUBLET_BRF,
        STUDENT_BRF,
        STUDENT_RENT,
        LODGER,
        HOUSE
    }

    init {

        //Not in use
        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART,
            MessageBodySingleSelect(
                "Hej! Jag heter Hedvig 👋"
                        + "\u000CJag behöver ställa några frågor till dig, för att kunna ge dig ett prisförslag på en hemförsäkring"
                        + "\u000CDu signar inte upp dig på något genom att fortsätta!",
                Lists.newArrayList<SelectItem>(
                    SelectOption("Låter bra!", MESSAGE_FORSLAGSTART),
                    SelectOption("Jag är redan medlem", "message.bankid.start")
                )
            )
        )

        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART_SHORT,
            MessageBodyParagraph(
                "Hej! Jag heter Hedvig 👋"
            )
        )
        this.addRelayToChatMessage(MESSAGE_ONBOARDINGSTART_SHORT, MESSAGE_FORSLAGSTART)

        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART_ASK_NAME,
            WrappedMessage(
                MessageBodyText(
                    "Hej! Jag heter Hedvig 👋\u000CVad heter du?",
                    TextContentType.GIVEN_NAME,
                    KeyboardType.DEFAULT,
                    "Förnamn"
                )
            )
            { body, u, message ->
                val name = body.text.trim().replace(Regex("[!.,]"), "")
                    .replace(Regex("Hej jag heter", RegexOption.IGNORE_CASE), "").trim().capitalizeAll()

                u.onBoardingData.firstName = name
                addToChat(message)
                MESSAGE_ONBOARDINGSTART_REPLY_NAME
            })

        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART_REPLY_NAME,
            MessageBodySingleSelect(
                "Trevligt att träffas {NAME}!\u000CFör att kunna ge dig ett prisförslag"
                        + " behöver jag ställa några snabba frågor"
                // + "\u000C"
                , SelectOption("Okej!", MESSAGE_ONBOARDINGSTART_ASK_EMAIL),
                SelectOption("Jag är redan medlem", "message.bankid.start")
            )
        )

        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART_ASK_EMAIL,
            WrappedMessage(
                MessageBodyText(
                    "Först, vad är din mailadress?",
                    TextContentType.EMAIL_ADDRESS,
                    KeyboardType.EMAIL_ADDRESS
                )
            ) { body, userContext, message ->
                val trimmedEmail = body.text.trim()
                userContext.onBoardingData.email = trimmedEmail
                memberService.updateEmail(userContext.memberId, trimmedEmail)
                body.text = "Min email är {EMAIL}"
                addToChat(message)
                MESSAGE_FORSLAGSTART
            }
        )
        this.setExpectedReturnType(MESSAGE_ONBOARDINGSTART_ASK_EMAIL, EmailAdress())


        this.createChatMessage(
            "message.membernotfound",
            MessageBodySingleSelect(
                "Hmm, det verkar som att du inte är medlem här hos mig ännu" + "\u000CMen jag tar gärna fram ett försäkringsförslag till dig, det är precis som allt annat med mig superenkelt",
                Lists.newArrayList<SelectItem>(SelectOption("Låter bra!", MESSAGE_ONBOARDINGSTART_ASK_EMAIL_ALT))
            )
        )
        this.createChatMessage(
            MESSAGE_ONBOARDINGSTART_ASK_EMAIL_ALT,
            WrappedMessage(
                MessageBodyText(
                    "Först, vad är din mailadress?",
                    TextContentType.EMAIL_ADDRESS,
                    KeyboardType.EMAIL_ADDRESS
                )
            ) { body, userContext, message ->
                val trimmedEmail = body.text.trim()
                userContext.onBoardingData.email = trimmedEmail
                memberService.updateEmail(userContext.memberId, trimmedEmail)
                body.text = "Min email är {EMAIL}"
                addToChat(message)
                MESSAGE_FORSLAGSTART
            }
        )
        this.setExpectedReturnType(MESSAGE_ONBOARDINGSTART_ASK_EMAIL_ALT, EmailAdress())

        this.createMessage(
            MESSAGE_NOTMEMBER,
            MessageBodyParagraph(
                "Okej! Då tar jag fram ett försäkringsförslag till dig på nolltid"
            )
        )
        this.addRelay(MESSAGE_NOTMEMBER, "message.notmember.start")

        this.createMessage(
            "message.notmember.start",
            MessageBodyParagraph(
                "Jag ställer några snabba frågor så att jag kan räkna ut ditt pris"
            )
        )
        this.addRelay("message.notmember.start", MESSAGE_ONBOARDINGSTART_ASK_EMAIL)

        this.createMessage(
            "message.uwlimit.tack",
            MessageBodySingleSelect(
                "Tack! Jag hör av mig så fort jag kan",
                listOf(SelectOption("Jag vill starta om chatten", "message.activate.ok.a"))
            )
        )


        this.createChatMessage(
            "message.medlem",
            WrappedMessage(
                MessageBodySingleSelect(
                    "Välkommen tillbaka "
                            + emoji_hug
                            + "\n\n Logga in med BankID så är du inne i appen igen",
                    listOf(
                        SelectLink(
                            "Logga in",
                            "message.bankid.autostart.respond",
                            null,
                            "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                            false
                        )
                    )
                )
            ) { m: MessageBodySingleSelect, uc: UserContext, _ ->
                val obd = uc.onBoardingData
                if (m.selectedItem.value == "message.bankid.autostart.respond") {
                    uc.putUserData(LOGIN, "true")
                    obd.bankIdMessage = "message.medlem"
                }

                m.selectedItem.value
            })
        setupBankidErrorHandlers("message.medlem")

        // Deprecated
        this.createMessage(
            MESSAGE_PRE_FORSLAGSTART,
            MessageBodyParagraph(
                "Toppen! Då ställer jag några frågor så att jag kan räkna ut ditt pris"
            ),
            1500
        )
        this.addRelay(MESSAGE_PRE_FORSLAGSTART, MESSAGE_FORSLAGSTART)

        this.createMessage(
            MESSAGE_FORSLAGSTART,
            body = MessageBodySingleSelect(
                "Tack! Bor du i lägenhet eller eget hus?",
                SelectOption("Lägenhet", MESSAGE_LAGENHET_PRE),
                SelectOption("Hus", MESSAGE_HUS)
            )
        )

        this.createMessage(MESSAGE_LAGENHET_PRE, MessageBodyParagraph("👍"))
        this.addRelay(MESSAGE_LAGENHET_PRE, MESSAGE_LAGENHET_NO_PERSONNUMMER)

        this.createChatMessage(
            MESSAGE_LAGENHET_NO_PERSONNUMMER,
            WrappedMessage(
                MessageBodyNumber(
                    "Vad är ditt personnummer? Jag behöver det så att jag kan hämta din adress",
                    "ååmmddxxxx"
                )
            ) { body, uc, m ->

                val (trimmedSSN, memberBirthDate) = uc.storeAndTrimAndAddSSNToChat(body) {
                    m.body.text = it
                    addToChat(m)
                }

                if (ConversationUtils.isYoungerThan18(memberBirthDate)) {
                    return@WrappedMessage(MESSAGE_MEMBER_UNDER_EIGHTEEN)
                }

                val hasAddress = memberService.ssnLookupAndStore(uc, trimmedSSN, Nationality.SWEDEN)

                if (hasAddress) {
                    MESSAGE_BANKIDJA
                } else {
                    MESSAGE_LAGENHET_ADDRESSNOTFOUND
                }
            }
        )
        this.setExpectedReturnType(MESSAGE_LAGENHET_NO_PERSONNUMMER, SSNSweden())


        this.createChatMessage(
            MESSAGE_LAGENHET_ADDRESSNOTFOUND,
            WrappedMessage(
                MessageBodyText(
                    "Konstigt, just nu kan jag inte hitta din adress. Så jag behöver ställa några extra frågor 😊\u000C"
                            + "Vad heter du i efternamn?"
                    , TextContentType.FAMILY_NAME, KeyboardType.DEFAULT
                )
            ) { b, uc, m ->
                val familyName = b.text.trim().capitalizeAll()
                val firstName = uc.onBoardingData.firstName
                if (firstName != null) {
                    if (firstName.split(" ").size > 1 && firstName.endsWith(familyName, true) == true) {
                        val lastNameIndex = firstName.length - (familyName.length + 1)
                        if (lastNameIndex > 0) {
                            uc.onBoardingData.firstName = firstName.substring(0, lastNameIndex)
                        }
                    }
                }
                uc.onBoardingData.familyName = familyName
                addToChat(m)

                "message.varborduadress"
            })

        this.createChatMessage(
            "fel.telefonnummer.format",
            WrappedMessage(
                MessageBodyText(
                    "Det ser inte ut som ett korrekt svenskt telefonnummer... Prova igen tack!",
                    TextContentType.TELEPHONE_NUMBER,
                    KeyboardType.PHONE_PAD
                )

            ) { b, uc, m ->
                if (phoneNumberIsCorrectSwedishFormat(b, m)) {
                    "message.hedvig.ska.ringa.dig"
                } else {
                    "fel.telefonnummer.format"
                }
            }
        )

        this.createChatMessage(
            "message.vad.ar.ditt.telefonnummer",
            WrappedMessage(
                MessageBodyText(
                    "Tack! Jag behöver ställa några frågor på telefon till dig, innan jag kan ge dig ditt förslag 🙂\u000C"
                            + "Vilket telefonnummer kan jag nå dig på?",
                    TextContentType.TELEPHONE_NUMBER,
                    KeyboardType.PHONE_PAD
                )

            ) { b, uc, m ->
                if (phoneNumberIsCorrectSwedishFormat(b, m)) {
                    "message.hedvig.ska.ringa.dig"
                } else {
                    "fel.telefonnummer.format"
                }
            }
        )

        this.createChatMessage(
            "message.hedvig.ska.ringa.dig",
            MessageBodyText(
                "Tack så mycket. Jag hör av mig inom kort med ett förslag!"
            )
        )


        this.createChatMessage(
            "message.member.under.eighteen",
            WrappedMessage(
                MessageBodyParagraph(
                    "Hoppsan! \uD83D\uDE4A För att skaffa en försäkring hos mig behöver du tyvärr ha fyllt 18 år"
                            + "Om du råkade skriva fel personnummer så kan du testa att skriva igen \uD83D\uDE42"
                )
            ) { b, uc, m ->
                MESSAGE_LAGENHET_NO_PERSONNUMMER
            }

        )

        this.createChatMessage(
            MESSAGE_LAGENHET,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Har du BankID? I så fall kan vi hoppa över några frågor så du får se ditt prisförslag snabbare!",
                    listOf(
                        SelectLink(
                            "Fortsätt med BankID",
                            "message.bankid.autostart.respond.one", null,
                            "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                            false
                        ),
                        SelectOption("Fortsätt utan", "message.manuellnamn")
                    )
                )
            )
            { m, uc, _ ->
                val obd = uc.onBoardingData
                if (m.selectedItem.value == "message.bankid.autostart.respond.one") {
                    obd.bankIdMessage = MESSAGE_LAGENHET
                }
                m.selectedItem.value
            }
        )

        setupBankidErrorHandlers(MESSAGE_LAGENHET)

        this.createMessage(
            "message.missing.bisnode.data",
            MessageBodyParagraph(
                "Konstigt, just nu kan jag inte hitta din adress. Så jag behöver ställa några extra frågor 😊"
            )
        )
        this.addRelay("message.missing.bisnode.data", "message.manuellnamn")

        this.createMessage(
            MESSAGE_START_LOGIN, MessageBodyParagraph("Hej! $emoji_hug"), 1500
        )
        this.addRelay(MESSAGE_START_LOGIN, MESSAGE_LOGIN_WITH_EMAIL)

        this.createChatMessage(
            "message.bankid.start",
            WrappedMessage(
                MessageBodySingleSelect(
                    "Bara att logga in så ser du din försäkring",
                    SelectLink(
                        "Logga in med BankID",
                        "message.bankid.autostart.respond.two", null,
                        "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                        false
                    ),
                    SelectOption("Jag är inte medlem", MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER)
                )
            ) { body, uc, message ->
                body.text = body.selectedItem.text
                addToChat(message)
                val obd = uc.onBoardingData
                if (body.selectedItem.value == "message.bankid.autostart.respond.two") {
                    obd.bankIdMessage = "message.bankid.start"
                    uc.putUserData(LOGIN, "true")
                    body.selectedItem.value
                } else if (body.selectedItem.value == MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER) {
                    uc.putUserData(LOGIN, "false")
                    MESSAGE_ONBOARDINGSTART_ASK_EMAIL
                } else {
                    body.selectedItem.value
                }
            })
        setupBankidErrorHandlers("message.bankid.start")

        this.createMessage(
            "message.bankid.start.manual",
            MessageBodyNumber(
                "Om du anger ditt personnummer så får du använda bankId på din andra enhet$emoji_smile"
            )
        )

        this.createMessage(
            "message.bankid.error",
            MessageBodyParagraph("Hmm, det verkar inte som att ditt BankID svarar. Testa igen!"),
            1500
        )

        this.createMessage(
            "message.bankid.start.manual.error",
            MessageBodyParagraph("Hmm, det verkar inte som att ditt BankID svarar. Testa igen!")
        )
        this.addRelay("message.bankid.start.manual.error", "message.bankid.start.manual")

        this.createMessage(
            "message.bankid.autostart.respond", MessageBodyBankIdCollect("{REFERENCE_TOKEN}")
        )
        this.createMessage(
            "message.bankid.autostart.respond.one", MessageBodyBankIdCollect("{REFERENCE_TOKEN}")
        )
        this.createMessage(
            "message.bankid.autostart.respond.two", MessageBodyBankIdCollect("{REFERENCE_TOKEN}")
        )

        this.createChatMessage(
            MESSAGE_LOGIN_FAILED_WITH_EMAIL,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Ojdå, det ser ut som att du måste logga in med BankID!",
                    SelectLink(
                        "Logga in med BankID",
                        "message.bankid.autostart.respond.two", null,
                        "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                        false
                    ),
                    SelectOption("Jag är inte medlem", MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER)
                )
            ) { body, uc, message ->
                body.text = body.selectedItem.text
                addToChat(message)
                when (body.selectedItem.value) {
                    "message.bankid.autostart.respond.two" -> {
                        uc.onBoardingData.bankIdMessage = "message.bankid.start"
                        uc.putUserData(LOGIN, "true")
                        body.selectedItem.value
                    }
                    MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER -> {
                        uc.putUserData(LOGIN, "false")
                        MESSAGE_ONBOARDINGSTART_ASK_EMAIL
                    }
                    else -> {
                        body.selectedItem.value
                    }
                }
            })
        setupBankidErrorHandlers(MESSAGE_LOGIN_FAILED_WITH_EMAIL)

        this.createChatMessage(MESSAGE_LOGIN_WITH_EMAIL,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Bara att logga in så ser du din försäkring",
                    SelectLink(
                        "Logga in med BankID",
                        "message.bankid.autostart.respond.two", null,
                        "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                        false
                    ),
                    SelectOption("Logga in med email och lösenord", MESSAGE_LOGIN_ASK_EMAIL),
                    SelectOption("Jag är inte medlem", MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER)
                )
            ) { body, uc, message ->
                body.text = body.selectedItem.text
                addToChat(message)
                when (body.selectedItem.value) {
                    "message.bankid.autostart.respond.two" -> {
                        uc.onBoardingData.bankIdMessage = MESSAGE_LOGIN_WITH_EMAIL
                        uc.putUserData(LOGIN, "true")
                        body.selectedItem.value
                    }
                    MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER -> {
                        uc.putUserData(LOGIN, "false")
                        MESSAGE_ONBOARDINGSTART_ASK_EMAIL
                    }
                    MESSAGE_LOGIN_ASK_EMAIL -> {
                        uc.putUserData(LOGIN, "true")
                        body.selectedItem.value
                    }
                    else -> {
                        body.selectedItem.value
                    }
                }
            })
        setupBankidErrorHandlers(MESSAGE_LOGIN_WITH_EMAIL)

        this.createMessage(
            MESSAGE_LOGIN_WITH_EMAIL_ASK_PASSWORD,
            MessageBodyText(
                "Tack! Och vad är ditt lösenord?",
                TextContentType.PASSWORD,
                KeyboardType.DEFAULT
            )
        )

        this.createMessage(MESSAGE_LOGIN_WITH_EMAIL_PASSWORD_SUCCESS, MessageBodyText("Välkommen, Apple!"))

        this.createMessage(
            MESSAGE_LOGIN_ASK_EMAIL,
            MessageBodyText(
                "Vad är din email address?",
                TextContentType.EMAIL_ADDRESS,
                KeyboardType.EMAIL_ADDRESS
            )
        )
        this.setExpectedReturnType(MESSAGE_LOGIN_ASK_EMAIL, EmailAdress())

        this.createChatMessage(MESSAGE_LOGIN_WITH_EMAIL_TRY_AGAIN,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Om du är medlem hos Hedvig med denna email måste du logga in med BankID!",
                    SelectLink(
                        "Logga in med BankID",
                        "message.bankid.autostart.respond.two", null,
                        "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                        false
                    ),
                    SelectOption("Logga in med email och lösenord", MESSAGE_LOGIN_ASK_EMAIL),
                    SelectOption("Jag är inte medlem", MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER)
                )
            ) { body, uc, message ->
                body.text = body.selectedItem.text
                addToChat(message)
                val obd = uc.onBoardingData
                when (body.selectedItem.value) {
                    "message.bankid.autostart.respond.two" -> {
                        obd.bankIdMessage = MESSAGE_LOGIN_WITH_EMAIL_TRY_AGAIN
                        uc.putUserData(LOGIN, "true")
                        body.selectedItem.value
                    }
                    MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER -> {
                        uc.putUserData(LOGIN, "false")
                        MESSAGE_ONBOARDINGSTART_ASK_EMAIL
                    }
                    MESSAGE_LOGIN_ASK_EMAIL -> {
                        uc.putUserData(LOGIN, "true")
                        body.selectedItem.value
                    }
                    else -> {
                        body.selectedItem.value
                    }
                }
            })
        setupBankidErrorHandlers(MESSAGE_LOGIN_WITH_EMAIL_TRY_AGAIN)

        this.createMessage(
            MESSAGE_TIPSA,
            MessageBodyText(
                "Kanon! Fyll i mailadressen till den du vill att jag ska skicka ett tipsmail till",
                TextContentType.EMAIL_ADDRESS,
                KeyboardType.EMAIL_ADDRESS
            )
        )
        this.setExpectedReturnType(MESSAGE_TIPSA, EmailAdress())
        this.createMessage(
            MESSAGE_FRIFRAGA,
            MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
            MessageBodyText("Fråga på!")
        )

        this.createMessage(
            MESSAGE_FRIONBOARDINGFRAGA,
            MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
            MessageBodyText("Fråga på! ")
        )

        this.createMessage(
            MESSAGE_NAGOTMER,
            MessageBodySingleSelect(
                "Tack! Vill du hitta på något mer nu när vi har varandra på tråden?",
                SelectOption("Jag har en fråga", MESSAGE_FRIONBOARDINGFRAGA),
                SelectOption("Nej tack!", MESSAGE_AVSLUTOK)
            )
        )

        this.createChatMessage(
            MESSAGE_BANKIDJA,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Tack {NAME}! Är det lägenheten på {ADDRESS} jag ska ta fram ett förslag för?",
                    SelectOption("Yes, stämmer bra!", MESSAGE_KVADRAT),
                    SelectOption("Nix", MESSAGE_VARBORDUFELADRESS)
                )
            ) { body, uc, m ->
                val item = body.selectedItem
                body.text = if (item.value == MESSAGE_KVADRAT) "Yes, stämmer bra!" else "Nix"
                addToChat(m)
                when {
                    item.value == MESSAGE_KVADRAT -> handleStudentEntrypoint(MESSAGE_KVADRAT, uc)
                    item.value == MESSAGE_VARBORDUFELADRESS -> {
                        val obd = uc.onBoardingData
                        obd.clearAddress()
                        item.value
                    }
                    else -> item.value
                }
            }
        )

        this.createMessage(
            "message.bankidja.noaddress",
            MessageBodyText(
                "Tack {NAME}! Nu skulle jag behöva veta vilken gatuadress bor du på?",
                TextContentType.STREET_ADDRESS_LINE1, KeyboardType.DEFAULT
            )
        )

        this.createMessage(
            MESSAGE_VARBORDUFELADRESS,
            MessageBodyText(
                "Inga problem! Vad är gatuadressen till lägenheten du vill försäkra?",
                TextContentType.STREET_ADDRESS_LINE1, KeyboardType.DEFAULT, "Kungsgatan 1"
            )
        )
        this.createMessage(
            "message.varbordufelpostnr",
            MessageBodyNumber("Och vad har du för postnummer?", TextContentType.POSTAL_CODE, "123 45")
        )
        this.setExpectedReturnType("message.varbordufelpostnr", ZipCodeSweden())

        this.createMessage(MESSAGE_KVADRAT, MessageBodyNumber("Hur många kvadratmeter är lägenheten?"))
        this.createMessage(MESSAGE_KVADRAT_ALT, MessageBodyNumber("Hur många kvadratmeter är lägenheten?"))
        this.setExpectedReturnType(MESSAGE_KVADRAT, LivingSpaceSquareMeters())
        this.setExpectedReturnType(MESSAGE_KVADRAT_ALT, LivingSpaceSquareMeters())

        this.createChatMessage(
            "message.manuellnamn",
            MessageBodyText(
                "Inga problem! Då ställer jag bara några extra frågor nu\u000CMen om du vill bli medlem sen så måste du signera med BankID, bara så du vet!\u000CVad heter du i förnamn?"
                , TextContentType.GIVEN_NAME, KeyboardType.DEFAULT
            )
        )

        this.createMessage(
            "message.manuellfamilyname",
            MessageBodyText(
                "Kul att ha dig här {NAME}! Vad heter du i efternamn?",
                TextContentType.FAMILY_NAME,
                KeyboardType.DEFAULT
            )
        )

        this.createMessage(
            "message.manuellpersonnr",
            MessageBodyNumber("Tack! Vad är ditt personnummer? (12 siffror)")
        )
        this.setExpectedReturnType("message.manuellpersonnr", SSNSweden())
        this.createMessage(
            "message.varborduadress",
            MessageBodyText(
                "Vilken gatuadress bor du på?",
                TextContentType.STREET_ADDRESS_LINE1,
                KeyboardType.DEFAULT,
                "Kungsgatan 1"
            )
        )
        this.createMessage(
            "message.varbordupostnr",
            MessageBodyNumber("Vad är ditt postnummer?", TextContentType.POSTAL_CODE, "123 45")
        )
        this.setExpectedReturnType("message.varbordupostnr", ZipCodeSweden())

        this.createMessage(
            "message.lghtyp",
            MessageBodySingleSelect(
                "Perfekt! Hyr du eller äger du den?",
                SelectOption("Jag hyr den", ProductTypes.RENT.toString()),
                SelectOption("Jag äger den", ProductTypes.BRF.toString())
            )
        )


        this.createMessage(
            "message.lghtyp.sublet",
            MessageBodySingleSelect(
                "Okej! Är lägenheten du hyr i andra hand en hyresrätt eller bostadsrätt?",
                SelectOption("Hyresrätt", ProductTypes.SUBLET_RENTAL.toString()),
                SelectOption("Bostadsrätt", ProductTypes.SUBLET_BRF.toString())
            )
        )

        this.createMessage(MESSAGE_ASK_NR_RESIDENTS, MessageBodyNumber("Okej! Hur många bor där?"))
        this.setExpectedReturnType(MESSAGE_ASK_NR_RESIDENTS, HouseholdMemberNumber())

        this.createMessage(
            MESSAGE_SAKERHET,
            MessageBodyMultipleSelect(
                "Finns någon av de här säkerhetsgrejerna i lägenheten?",
                Lists.newArrayList(
                    SelectOption("Brandvarnare", "safety.alarm"),
                    SelectOption("Brandsläckare", "safety.extinguisher"),
                    SelectOption("Säkerhetsdörr", "safety.door"),
                    SelectOption("Gallergrind", "safety.gate"),
                    SelectOption("Inbrottslarm", "safety.burglaralarm"),
                    SelectOption("Inget av dessa", "safety.none", false, true)
                )
            )
        )

        this.createMessage(
            MESSAGE_PHONENUMBER,
            MessageBodyNumber("Nu är vi snart klara! Vad är ditt telefonnummer?")
        )
        this.setExpectedReturnType(MESSAGE_PHONENUMBER, TextInput())

        this.createMessage(
            MESSAGE_EMAIL,
            MessageBodyText(
                "Nu behöver jag bara din mailadress så att jag kan skicka en bekräftelse",
                TextContentType.EMAIL_ADDRESS,
                KeyboardType.EMAIL_ADDRESS
            )
        )
        this.setExpectedReturnType(MESSAGE_EMAIL, EmailAdress())

        this.createMessage(
            MESSAGE_FORSAKRINGIDAG,
            MessageBodySingleSelect(
                "Har du någon hemförsäkring idag?",
                SelectOption("Ja", MESSAGE_FORSAKRINGIDAGJA),
                SelectOption("Nej", MESSAGE_FORSLAG2)
            )
        )

        this.createMessage(
            MESSAGE_FORSAKRINGIDAGJA,
            MessageBodySingleSelect(
                "Okej! Vilket försäkringsbolag har du?",
                SelectOption("If", "if"),
                SelectOption("ICA", "ICA"),
                SelectOption("Folksam", "Folksam"),
                SelectOption("Trygg-Hansa", "Trygg-Hansa"),
                SelectOption("Länsförsäkringar", "Länsförsäkringar"),
                SelectOption("Länsförsäkringar Stockholm", "Länsförsäkringar Stockholm"),
                SelectOption("Annat bolag", "message.bolag.annat.expand"),
                SelectOption("Ingen aning", "message.bolag.vetej")
            )
        )

        this.createMessage(
            "message.bolag.annat.expand",
            MessageBodySingleSelect(
                "Okej! Är det något av dessa kanske?",
                SelectOption("Moderna", "Moderna"),
                SelectOption("Tre Kronor", "Tre Kronor"),
                SelectOption("Vardia", "Vardia"),
                SelectOption("Gjensidige", "Gjensidige"),
                SelectOption("Aktsam", "Aktsam"),
                SelectOption("Dina Försäkringar", "Dina Försäkringar"),
                SelectOption("Annat bolag", MESSAGE_ANNATBOLAG)
            )
        )

        this.createMessage(
            "message.bolag.vetej", MessageBodyParagraph("Inga problem, det kan vi ta senare")
        )
        this.addRelay("message.bolag.vetej", MESSAGE_FORSLAG2)

        this.createMessage(
            MESSAGE_ANNATBOLAG, MessageBodyText("Okej, vilket försäkringsbolag har du?"), 2000
        )

        this.createChatMessage(
            "message.bolag.not.switchable",
            MessageBodySingleSelect(
                "👀\u000C" +
                        "Okej! Om du blir medlem hos mig så aktiveras din försäkring här först när din nuvarande försäkring gått ut\u000C" +
                        "Du kommer behöva ringa ditt försäkringbolag och säga upp din försäkring. Men jag hjälper dig med det så gott jag kan 😊",
                listOf(
                    SelectOption("Jag förstår", MESSAGE_FORSLAG2_ALT_1), // Create product
                    SelectOption("Förklara mer", "message.forklara.mer.bolag.not.switchable")
                )
            )
        )

        this.createChatMessage(
            MESSAGE_BYTESINFO,
            MessageBodySingleSelect(
                "👀\u000C" +
                        "Okej, om du blir medlem hos mig sköter jag bytet åt dig\u000CSå när din gamla försäkring går ut, flyttas du automatiskt till Hedvig",
                listOf(
                    SelectOption("Jag förstår", MESSAGE_FORSLAG2_ALT_1), // Create product
                    SelectOption("Förklara mer", "message.bytesinfo3")
                )
            )
        )

        this.createChatMessage(
            "message.bytesinfo3",
            MessageBodySingleSelect(
                "Självklart!\u000C"
                        + "Oftast har du ett tag kvar på bindningstiden på din gamla försäkring\u000C"
                        + "Om du väljer att byta till Hedvig så hör jag av mig till ditt försäkringsbolag och meddelar att du vill byta försäkring så fort bindningstiden går ut\u000C"
                        + "Till det behöver jag en fullmakt från dig som du skriver under med mobilt BankID \u000C"
                        + "Sen börjar din nya försäkring gälla direkt när den gamla går ut\u000C"
                        + "Så du behöver aldrig vara orolig att gå utan försäkring efter att du skrivit på med mig",
                object : ArrayList<SelectItem>() {
                    init {
                        add(SelectOption("Okej!", MESSAGE_FORSLAG2_ALT_2)) // Create product
                    }
                })
        )

        this.createChatMessage(
            "message.forklara.mer.bolag.not.switchable",
            MessageBodySingleSelect(
                "Självklart! De flesta försäkringsbolagen har som policy att man måste säga upp sin försäkring över telefon, kanske för att göra det extra krångligt för dig att säga upp din försäkring 🙄 Jag kommer maila dig vilket nummer du behöver ringa och vad du behöver säga, det brukar gå rätt fort",
                object : ArrayList<SelectItem>() {
                    init {
                        add(SelectOption("Okej!", MESSAGE_FORSLAG2_ALT_2)) // Create product
                    }
                })
        )

        this.createChatMessage(
            "message.hedvig.uwlimit.askemail",
            MessageBodyText(
                "Tack! Tyvärr kan vi inte ta fram ett pris till dig här i appen. En av våra försäkringsexperter behöver kika på din ansökan. För att få ditt erbjudande, maila till oss på prisforslag@hedvig.com"
            )
        )

        this.createChatMessage(
            MESSAGE_50K_LIMIT, WrappedMessage(
                MessageBodySingleSelect(
                    "Toppen!\u000CÄger du något som du tar med dig utanför hemmet som är värt över 50 000 kr som du vill försäkra? 💍",

                    SelectOption("Ja, berätta om objektsförsäkring", MESSAGE_50K_LIMIT_YES),
                    SelectOption("Nej, gå vidare utan", MESSAGE_50K_LIMIT_NO)
                )
            ) { body, userContext, m ->
                val ssn = userContext.onBoardingData.ssn
                if (checkSSN(ssn) == Flag.RED) {
                    completeOnboarding()
                    return@WrappedMessage ("message.hedvig.uwlimit.askemail")
                }

                for (o in body.choices) {
                    if (o.selected) {
                        m.body.text = o.text
                        addToChat(m)
                    }
                }
                if (body.selectedItem.value.equals(MESSAGE_50K_LIMIT_YES, ignoreCase = true)) {

                    userContext.putUserData("{50K_LIMIT}", "true")
                }
                body.selectedItem.value
            }
        )

        this.createChatMessage(
            MESSAGE_50K_LIMIT_YES,
            MessageBodySingleSelect(
                "Om du har något som är värt mer än 50 000 kr och som du har med dig på stan, så behöver du lägga till ett extra skydd för den saken!\u000CDet kallas objektsförsäkring, och du lägger enkelt till det i efterhand om du skaffar Hedvig",
                SelectOption("Jag förstår!", MESSAGE_50K_LIMIT_YES_YES)
            )
        )

        //This message is used as the last message to the 25K LIMIT flow as well as to 50K LIMIT flow
        this.createMessage(
            MESSAGE_50K_LIMIT_YES_YES,
            MessageBodyParagraph("Toppen, så hör bara av dig i chatten så fixar jag det!"),
            1500
        )
        this.addRelay(MESSAGE_50K_LIMIT_YES_YES, MESSAGE_FORSAKRINGIDAG)

        this.createMessage(
            MESSAGE_50K_LIMIT_YES_NO,
            MessageBodyParagraph("Då skippar jag det $emoji_thumbs_up"),
            2000
        )
        this.addRelay(MESSAGE_50K_LIMIT_YES_NO, MESSAGE_FORSAKRINGIDAG)

        this.createMessage(
            MESSAGE_50K_LIMIT_NO,
            MessageBodyParagraph("Vad bra! Då täcks dina prylar av drulleförsäkringen när du är ute på äventyr"),
            2000
        )

        this.createMessage(
            MESSAGE_50K_LIMIT_NO_1,
            MessageBodyParagraph(
                "Köper du någon dyr pryl i framtiden så fixar jag så klart det också!"
            ),
            2000
        )

        this.addRelay(MESSAGE_50K_LIMIT_NO, MESSAGE_50K_LIMIT_NO_1)

        this.addRelay(MESSAGE_50K_LIMIT_NO_1, MESSAGE_FORSAKRINGIDAG)

        this.createMessage(
            MESSAGE_FORSLAG,
            MessageBodyParagraph("Sådär, det var all info jag behövde. Tack!"),
            2000
        )

        this.createChatMessage(
            MESSAGE_FORSLAG2,
            WrappedMessage(MessageBodySingleSelect(
                "Sådärja, tack {NAME}! Det var alla frågor jag hade!",
                Lists.newArrayList<SelectItem>(
                    SelectLink.toOffer("Gå vidare för att se ditt förslag 👏", "message.forslag.dashboard")
                )
            ),
                addMessageCallback = { uc -> this.completeOnboarding() },
                receiveMessageCallback = { _, _, _ -> MESSAGE_FORSLAG2 })
        )

        this.createChatMessage(
            MESSAGE_FORSLAG2_ALT_1,
            WrappedMessage(MessageBodySingleSelect(
                "Sådärja, tack {NAME}! Det var alla frågor jag hade!",
                Lists.newArrayList<SelectItem>(
                    SelectLink.toOffer("Gå vidare för att se ditt förslag 👏", "message.forslag.dashboard")
                )
            ),
                addMessageCallback = { uc -> this.completeOnboarding() },
                receiveMessageCallback = { _, _, _ -> MESSAGE_FORSLAG2 })
        )

        this.createChatMessage(
            MESSAGE_FORSLAG2_ALT_2,
            WrappedMessage(MessageBodySingleSelect(
                "Sådärja, tack {NAME}! Det var alla frågor jag hade!",
                Lists.newArrayList<SelectItem>(
                    SelectLink.toOffer("Gå vidare för att se ditt förslag 👏", "message.forslag.dashboard")
                )
            ),
                addMessageCallback = { uc -> this.completeOnboarding() },
                receiveMessageCallback = { _, _, _ -> MESSAGE_FORSLAG2 })
        )
        this.addRelay(MESSAGE_FORSLAG, MESSAGE_FORSLAG2)

        this.createChatMessage(
            "message.tryggt",
            MessageBodySingleSelect(
                ""
                        + "Självklart!\u000CHedvig är backat av en av världens största försäkringsbolag, så att du kan känna dig trygg i alla lägen\u000CDe är där för mig, så jag alltid kan vara där för dig\u000CJag är självklart också auktoriserad av Finansinspektionen "
                        + emoji_mag,
                object : ArrayList<SelectItem>() {
                    init {
                        add(
                            SelectLink(
                                "Visa förslaget igen",
                                "message.forslag.dashboard",
                                "Offer", null, null,
                                false
                            )
                        )
                        add(SelectOption("Jag har en annan fråga", "message.quote.close"))
                    }
                })
        )

        this.createChatMessage(
            "message.skydd",
            MessageBodySingleSelect(
                "" + "Såklart! Med mig har du samma grundskydd som en vanlig hemförsäkring\u000CUtöver det ingår alltid drulle, alltså till exempel om du tappar din telefon i golvet och den går sönder, och ett bra reseskydd",
                object : ArrayList<SelectItem>() {
                    init {
                        add(
                            SelectLink(
                                "Visa förslaget igen",
                                "message.forslag.dashboard",
                                "Offer", null, null,
                                false
                            )
                        )
                        add(SelectOption("Jag har en annan fråga", "message.quote.close"))
                        // add(new SelectOption("Jag vill bli medlem", "message.forslag"));
                    }
                })
        )

        this.createMessage(
            "message.frionboardingfragatack",
            MessageBodySingleSelect(
                "Tack! Jag hör av mig inom kort",
                object : ArrayList<SelectItem>() {
                    init {
                        add(SelectOption("Jag har fler frågor", MESSAGE_FRIONBOARDINGFRAGA))
                    }
                })
        )

        this.createMessage(
            "message.frifragatack",
            MessageBodySingleSelect(
                "Tack! Jag hör av mig inom kort",
                object : ArrayList<SelectItem>() {
                    init {
                        add(
                            SelectLink(
                                "Visa förslaget igen",
                                "message.forslag.dashboard",
                                "Offer", null, null,
                                false
                            )
                        )
                        add(SelectOption("Jag har fler frågor", MESSAGE_FRIFRAGA))
                    }
                })
        )

        this.createChatMessage(
            "message.uwlimit.housingsize",
            MessageBodyText(
                "Det var stort! För att kunna försäkra så stora lägenheter behöver vi ta några grejer över telefon\u000CVad är ditt nummer?",
                TextContentType.TELEPHONE_NUMBER, KeyboardType.DEFAULT
            )
        )

        this.createChatMessage(
            "message.uwlimit.householdsize",
            MessageBodyText(
                "Okej! För att kunna försäkra så många i samma lägenhet behöver vi ta några grejer över telefon\u000CVad är ditt nummer?",
                TextContentType.TELEPHONE_NUMBER, KeyboardType.DEFAULT
            )
        )

        this.createChatMessage(
            "message.pris",
            MessageBodySingleSelect(
                "Det är knepigt att jämföra försäkringspriser, för alla försäkringar är lite olika.\u000CMen grundskyddet jag ger är väldigt brett utan att du behöver betala för krångliga tillägg\u000CSom Hedvigmedlem gör du dessutom skillnad för världen runtomkring dig, vilket du garanterat inte gör genom din gamla försäkring!",
                object : ArrayList<SelectItem>() {
                    init {
                        add(
                            SelectLink(
                                "Visa förslaget igen",
                                "message.forslag.dashboard",
                                "Offer", null, null,
                                false
                            )
                        )
                        add(SelectOption("Jag har fler frågor", "message.quote.close"))
                    }
                })
        )

        this.createMessage(
            "message.bankid.error.expiredTransaction",
            MessageBodyParagraph(BankIDStrings.expiredTransactionError),
            1500
        )

        this.createMessage(
            "message.bankid.error.certificateError",
            MessageBodyParagraph(BankIDStrings.certificateError),
            1500
        )

        this.createMessage(
            "message.bankid.error.userCancel",
            MessageBodyParagraph(BankIDStrings.userCancel),
            1500
        )

        this.createMessage(
            "message.bankid.error.cancelled", MessageBodyParagraph(BankIDStrings.cancelled), 1500
        )

        this.createMessage(
            "message.bankid.error.startFailed",
            MessageBodyParagraph(BankIDStrings.startFailed),
            1500
        )

        //Deperecated 2018-12-17
        this.createMessage(
            "message.kontraktklar",
            MessageBodyParagraph("Hurra! 🎉 Välkommen som medlem {NAME}!")
        )

        this.createMessage(
            "message.kontraktklar.ss",
            MessageBodySingleSelect(
                "Hurra! 🎉 Välkommen som medlem {NAME}!",
                SelectLink.toDashboard("Kolla in appen och bjud in dina vänner till Hedvig! 🙌 💕", "message.noop")
            )
        )

        this.createMessage(
            "message.kontrakt.email",
            MessageBodyText("OK! Vad är din mailadress?", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS)
        )
        this.setExpectedReturnType("message.kontrakt.email", EmailAdress())

        this.createMessage(
            "message.avslutvalkommen",
            MessageBodySingleSelect(
                "Hej så länge och ännu en gång, varmt välkommen!",
                object : ArrayList<SelectItem>() {
                    init {
                        add(
                            SelectLink(
                                "Nu utforskar jag", "onboarding.done", "Dashboard", null, null, false
                            )
                        )
                    }
                })
        )

        this.createMessage(
            MESSAGE_AVSLUTOK,
            MessageBodySingleSelect(
                "Okej! Trevligt att chattas, ha det fint och hoppas vi hörs igen!",
                Lists.newArrayList<SelectItem>(
                    SelectOption("Jag vill starta om chatten", MESSAGE_ONBOARDINGSTART_SHORT)
                )
            )
        )

        this.createChatMessage(
            "message.quote.close",
            MessageBodySingleSelect(
                "Du kanske undrade över något" + "\u000CNågot av det här kanske?",
                object : ArrayList<SelectItem>() {
                    init {
                        add(SelectOption("Är Hedvig tryggt?", "message.tryggt"))
                        add(SelectOption("Ger Hedvig ett bra skydd?", "message.skydd"))
                        add(SelectOption("Är Hedvig prisvärt?", "message.pris"))
                        add(SelectOption("Jag har en annan fråga", MESSAGE_FRIFRAGA))
                        add(
                            SelectLink(
                                "Visa förslaget igen",
                                "message.forslag.dashboard",
                                "Offer", null, null,
                                false
                            )
                        )
                    }
                })
        )

        this.createMessage("error", MessageBodyText("Oj nu blev något fel..."))

        // Student policy-related messages
        this.createMessage(
            "message.student",
            MessageBodySingleSelect(
                "Okej! Jag ser att du är under 30. Är du kanske student? $emoji_school_satchel",
                SelectOption("Ja", "message.studentja"),
                SelectOption("Nej", "message.studentnej")
            )
        )

        this.createMessage("message.studentnej", MessageBodyParagraph("Okej, då vet jag"))
        this.addRelay("message.studentnej", MESSAGE_KVADRAT)

        this.createMessage(
            "message.studentja",
            MessageBodySingleSelect(
                "Vad kul! Då har jag ett erbjudande som är skräddarsytt för studenter som bor max två personer på max 50 kvm",
                object : ArrayList<SelectItem>() {
                    init {
                        add(SelectOption("Okej, toppen!", MESSAGE_KVADRAT_ALT))
                    }
                })
        )

        this.createChatMessage(
            MESSAGE_STUDENT_LIMIT_LIVING_SPACE,
            MessageBodySingleSelect(
                "Okej! För så stora lägenheter (över 50 kvm) gäller dessvärre inte studentförsäkringen\u000C" + "Men inga problem, du får den vanliga hemförsäkringen som ger ett bredare skydd och jag fixar ett grymt pris till dig ändå! 🙌",
                Lists.newArrayList<SelectItem>(
                    SelectOption(
                        "Okej, jag förstår", MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE
                    )
                )
            )
        )

        this.createMessage(
            MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE,
            MessageBodySingleSelect(
                "Hyr du eller äger du lägenheten?",
                SelectOption("Jag hyr den", ProductTypes.RENT.toString()),
                SelectOption("Jag äger den", ProductTypes.BRF.toString())
            )
        )

        this.createChatMessage(
            MESSAGE_STUDENT_LIMIT_PERSONS,
            MessageBodySingleSelect(
                "Okej! För så många personer (fler än 2) gäller dessvärre inte studentförsäkringen\u000C" + "Men inga problem, du får den vanliga hemförsäkringen som ger ett bredare skydd och jag fixar ett grymt pris till dig ändå! 🙌",
                Lists.newArrayList<SelectItem>(SelectOption("Okej, jag förstår", MESSAGE_STUDENT_25K_LIMIT))
            )
        )

        this.createMessage(
            MESSAGE_STUDENT_ELIGIBLE_BRF,
            MessageBodySingleSelect(
                "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 99 kr per månad! 🙌",
                Lists.newArrayList<SelectItem>(SelectOption("Okej, nice!", MESSAGE_STUDENT_25K_LIMIT))
            )
        )

        this.createMessage(
            MESSAGE_STUDENT_ELIGIBLE_RENT,
            MessageBodySingleSelect(
                "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 79 kr per månad! 🙌",
                Lists.newArrayList<SelectItem>(SelectOption("Okej, nice!", MESSAGE_STUDENT_25K_LIMIT))
            )
        )

        this.createChatMessage(
            MESSAGE_STUDENT_25K_LIMIT, WrappedMessage(
                MessageBodySingleSelect(
                    "Äger du något som du tar med dig utanför hemmet som är värt över 25 000 kr som du vill försäkra? 💍",
                    Lists.newArrayList<SelectItem>(
                        SelectOption("Ja, berätta om objektsförsäkring", MESSAGE_STUDENT_25K_LIMIT_YES),
                        SelectOption("Nej, gå vidare utan", MESSAGE_50K_LIMIT_NO)
                    )
                )
            ) { body, userContext, m ->
                for (o in body.choices) {
                    if (o.selected) {
                        m.body.text = o.text
                        addToChat(m)
                    }
                }
                if (body.selectedItem.value.equals(MESSAGE_STUDENT_25K_LIMIT_YES, ignoreCase = true)) {
                    userContext.putUserData(UserData.TWENTYFIVE_THOUSAND_LIMIT, "true")
                }
                body.selectedItem.value
            }
        )

        this.createChatMessage(
            MESSAGE_STUDENT_25K_LIMIT_YES,
            MessageBodySingleSelect(
                "Om du har något som är värt mer än 25 000 kr och som du har med dig på stan, så behöver du lägga till ett extra skydd för den saken!\u000CDet kallas objektsförsäkring, och du lägger enkelt till det i efterhand om du skaffar Hedvig",
                SelectOption("Jag förstår!", MESSAGE_50K_LIMIT_YES_YES)
            )
        )
    }

    private fun setupBankidErrorHandlers(messageId: String, optinalRelayId: String? = null) {
        val relayId = optinalRelayId ?: messageId


        this.createMessage(
            "$messageId.bankid.error.expiredTransaction",
            MessageBodyParagraph(BankIDStrings.expiredTransactionError),
            1500
        )
        this.addRelay("$messageId.bankid.error.expiredTransaction", relayId)

        this.createMessage(
            "$messageId.bankid.error.certificateError",
            MessageBodyParagraph(BankIDStrings.certificateError),
            1500
        )
        this.addRelay("$messageId.bankid.error.certificateError", relayId)

        this.createMessage(
            "$messageId.bankid.error.userCancel",
            MessageBodyParagraph(BankIDStrings.userCancel),
            1500
        )
        this.addRelay("$messageId.bankid.error.userCancel", relayId)

        this.createMessage(
            "$messageId.bankid.error.cancelled",
            MessageBodyParagraph(BankIDStrings.cancelled),
            1500
        )
        this.addRelay("$messageId.bankid.error.cancelled", relayId)

        this.createMessage(
            "$messageId.bankid.error.startFailed",
            MessageBodyParagraph(BankIDStrings.startFailed),
            1500
        )
        this.addRelay("$messageId.bankid.error.startFailed", relayId)

        this.createMessage(
            "$messageId.bankid.error.invalidParameters",
            MessageBodyParagraph(BankIDStrings.userCancel),
            1500
        )
        this.addRelay("$messageId.bankid.error.invalidParameters", relayId)
    }

    override fun init() {
        log.info("Starting onboarding conversation")
        startConversation(MESSAGE_ONBOARDINGSTART_ASK_NAME) // Id of first message
    }

    override fun init(startMessage: String) {
        log.info("Starting onboarding conversation with message: $startMessage")
        if (startMessage == MESSAGE_START_LOGIN) {
            userContext.putUserData(LOGIN, "true")
        }
        startConversation(startMessage) // Id of first message
    }
    // --------------------------------------------------------------------------- //

    // ------------------------------------------------------------------------------- //
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

                if (value == "message.kontraktklar") {
                    endConversation(userContext)
                }
            }
        }
    }

    private fun completeOnboarding() {
        underwriter.createQuote(userContext)
        this.memberService.finalizeOnBoarding(
            userContext.memberId, userContext.onBoardingData
        )
    }

    private fun phoneNumberIsCorrectSwedishFormat(b: MessageBody, m: Message): Boolean {
        val regex = Regex("[^0-9]")
        userContext.onBoardingData.phoneNumber = regex.replace(b.text, "")

        try {
            val swedishNumber = phoneNumberUtil.parse(userContext.onBoardingData.phoneNumber, "SE")
            if (phoneNumberUtil.isValidNumberForRegion(swedishNumber, "SE")) {
                sendPhoneNumberMessage(userContext)
                addToChat(m)
                return true
            } else {
                addToChat(m)
                return false
            }
        } catch (error: Exception) {
            "Error thrown when trying to validate phone number" + error.toString()
        }
        return false
    }

    private fun sendPhoneNumberMessage(uc: UserContext) {
        eventPublisher.publishEvent(
            OnboardingCallForQuoteEvent(
                uc.memberId,
                uc.onBoardingData.firstName,
                uc.onBoardingData.familyName,
                uc.onBoardingData.phoneNumber
            )
        )
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

        when (m.strippedBaseMessageId) {
            MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE, "message.lghtyp" -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem

                // Additional question for sublet contracts
                m.body.text = item.text
                addToChat(m)
                if (item.value == "message.lghtyp.sublet") {
                    nxtMsg = "message.lghtyp.sublet"
                } else {
                    val obd = userContext.onBoardingData
                    obd.houseType = item.value
                    nxtMsg = MESSAGE_ASK_NR_RESIDENTS
                }
            }
            "message.lghtyp.sublet" -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem
                val obd = userContext.onBoardingData
                obd.houseType = item.value
                m.body.text = item.text
                nxtMsg = MESSAGE_ASK_NR_RESIDENTS
            }

            "message.student" -> {
                val sitem2 = (m.body as MessageBodySingleSelect).selectedItem
                if (sitem2.value == "message.studentja") {
                    m.body.text = "Yes"
                    addToChat(m)
                    userContext.putUserData(IS_STUDENT, "1")
                } else {
                    m.body.text = "Nix"
                    userContext.putUserData(IS_STUDENT, "0")
                }
            }

            MESSAGE_NYHETSBREV -> {
                onBoardingData.newsLetterEmail = m.body.text
                addToChat(m)
                nxtMsg = MESSAGE_NAGOTMER
            }

            "message.uwlimit.housingsize", "message.uwlimit.householdsize" -> nxtMsg =
                handleUnderwritingLimitResponse(userContext, m, m.baseMessageId)
            MESSAGE_TIPSA -> {
                onBoardingData.setRecommendFriendEmail(m.body.text)
                nxtMsg = MESSAGE_NAGOTMER
            }
            MESSAGE_FRIFRAGA -> {
                handleFriFraga(userContext, m)
                nxtMsg = "message.frifragatack"
            }
            MESSAGE_FRIONBOARDINGFRAGA -> {
                handleFriFraga(userContext, m)
                nxtMsg = "message.frionboardingfragatack"
            }
            MESSAGE_ASK_NR_RESIDENTS -> {
                val nrPersons = (m.body as MessageBodyNumber).value
                onBoardingData.setPersonInHouseHold(nrPersons)
                m.body.text = if (nrPersons == 1) {
                    m.id = "message.pers.only.one"
                    "Jag bor själv"
                } else {
                    m.id = "message.pers.more.than.one"
                    "Vi är {NR_PERSONS}"
                }
                addToChat(m)

                nxtMsg = if (nrPersons > 6) {
                    "message.uwlimit.householdsize"
                } else {
                    handleStudentPolicyPersonLimit(MESSAGE_FORSAKRINGIDAG, userContext)
                }
            }
            MESSAGE_KVADRAT, MESSAGE_KVADRAT_ALT -> {
                val kvm = m.body.text
                onBoardingData.livingSpace = java.lang.Float.parseFloat(kvm)
                m.body.text = "{KVM} kvm"
                addToChat(m)
                nxtMsg = if (Integer.parseInt(kvm) > MAX_LIVING_SPACE_RENT_SQM) {
                    "message.uwlimit.housingsize"
                } else {
                    handleStudentPolicyLivingSpace("message.lghtyp", userContext)
                }
            }
            "message.manuellnamn" -> {
                onBoardingData.firstName = m.body.text
                addToChat(m)
                nxtMsg = "message.manuellfamilyname"
            }
            "message.manuellfamilyname" -> {
                onBoardingData.familyName = m.body.text
                addToChat(m)
                nxtMsg = "message.manuellpersonnr"
            }
            "message.manuellpersonnr" -> {
                onBoardingData.ssn = m.body.text

                // Member service is responsible for handling SSN->birth date conversion
                try {
                    memberService.startOnBoardingWithSSN(userContext.memberId, m.body.text)
                    val member = memberService.getProfile(userContext.memberId)
                    onBoardingData.birthDate = member.birthDate
                } catch (ex: Exception) {
                    log.error("Error loading memberProfile from memberService", ex)
                }

                addToChat(m)
                nxtMsg = "message.varborduadress"
            }
            "message.bankidja.noaddress", MESSAGE_VARBORDUFELADRESS, "message.varborduadress" -> {
                onBoardingData.addressStreet = m.body.text
                addToChat(m)
                nxtMsg = "message.varbordupostnr"
            }
            "message.varbordupostnr" -> {
                onBoardingData.addressZipCode = m.body.text
                addToChat(m)
                nxtMsg = handleStudentEntrypoint(MESSAGE_KVADRAT, userContext)
            }
            "message.varbordu" -> {
                onBoardingData.addressStreet = m.body.text
                addToChat(m)
                nxtMsg = MESSAGE_KVADRAT
            }
            MESSAGE_SAKERHET -> {
                val body = m.body as MessageBodyMultipleSelect

                if (body.noSelectedOptions == 0L) {
                    m.body.text = "Jag har inga säkerhetsgrejer"
                } else {
                    m.body.text = String.format("Jag har %s", body.selectedOptionsAsString())
                    for (o in body.selectedOptions()) {
                        onBoardingData.addSecurityItem(o.value)
                    }
                }
                addToChat(m)
                val userData = userContext.onBoardingData
                nxtMsg = if (userData.studentPolicyEligibility == true) {
                    MESSAGE_STUDENT_25K_LIMIT

                } else {
                    MESSAGE_FORSAKRINGIDAG
                }
            }
            MESSAGE_PHONENUMBER -> {
                val trim = m.body.text.trim { it <= ' ' }
                userContext.putUserData("{PHONE_NUMBER}", trim)
                m.body.text = "Mitt telefonnummer är {PHONE_NUMBER}"
                addToChat(m)
                nxtMsg = MESSAGE_FORSAKRINGIDAG
            }
            MESSAGE_EMAIL -> {
                val trim2 = m.body.text.trim { it <= ' ' }
                userContext.putUserData("{EMAIL}", trim2)
                m.body.text = "Min email är {EMAIL}"
                memberService.updateEmail(userContext.memberId, trim2)
                addToChat(m)
                endConversation(userContext)
                return
            }
            MESSAGE_LOGIN_ASK_EMAIL -> {
                val trimEmail = m.body.text.trim().toLowerCase()
                userContext.putUserData("{LOGIN_EMAIL}", trimEmail)
                m.body.text = trimEmail
                addToChat(m)
                nxtMsg = if (trimEmail == appleUserEmail.toLowerCase()) {
                    MESSAGE_LOGIN_WITH_EMAIL_ASK_PASSWORD
                } else {
                    MESSAGE_LOGIN_FAILED_WITH_EMAIL
                }
            }
            MESSAGE_LOGIN_WITH_EMAIL_ASK_PASSWORD -> {
                val pwd = m.body.text
                m.body.text = "*****"
                addToChat(m)
                if (pwd == appleUserPassword) {
                    nxtMsg = MESSAGE_LOGIN_WITH_EMAIL_PASSWORD_SUCCESS
                } else {
                    nxtMsg = MESSAGE_LOGIN_WITH_EMAIL_TRY_AGAIN
                }
            }

            MESSAGE_LOGIN_WITH_EMAIL_PASSWORD_SUCCESS -> {
                endConversation(userContext);
                return;
            }

            // nxtMsg = MESSAGE_FORSAKRINGIDAG;

            // case "message.bytesinfo":
            "message.bytesinfo2", MESSAGE_FORSAKRINGIDAG, "message.missingvalue", MESSAGE_FORSLAG2, MESSAGE_FORSLAG2_ALT_1, MESSAGE_FORSLAG2_ALT_2 -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem

                /*
         * Check if there is any data missing. Keep ask until Hedvig has got all info
         */
                val missingItems = userContext.missingDataItem
                if (missingItems != null) {

                    this.createMessage(
                        "message.missingvalue",
                        MessageBodyText(
                            "Oj, nu verkar det som om jag saknar lite viktig information.$missingItems"
                        )
                    )

                    m.body.text = item.text
                    nxtMsg = "message.missingvalue"
                    addToChat(m)
                    addToChat(getMessage("message.missingvalue"))
                } else if (m.id == "message.missingvalue" || item.value == MESSAGE_FORSLAG2 ||
                    item.value == MESSAGE_FORSLAG2_ALT_1 ||
                    item.value == MESSAGE_FORSLAG2_ALT_2
                ) {
                    completeOnboarding()
                }
            }
            MESSAGE_ANNATBOLAG -> {
                val comp = m.body.text
                userContext.onBoardingData.currentInsurer = comp
                m.body.text = comp
                nxtMsg = MESSAGE_INSURER_NOT_SWITCHABLE
                addToChat(m)
            }

            MESSAGE_FORSAKRINGIDAGJA, "message.bolag.annat.expand" -> {
                val comp = (m.body as MessageBodySingleSelect).selectedItem.value
                if (!comp.startsWith("message.")) {
                    userContext.onBoardingData.currentInsurer = comp
                    m.body.text = comp

                    if (comp != null && SwitchableInsurers.SWITCHABLE_INSURERS.contains(comp)) {
                        nxtMsg = MESSAGE_BYTESINFO
                    } else {
                        nxtMsg = MESSAGE_INSURER_NOT_SWITCHABLE
                    }

                    addToChat(m)
                }
            }
            "message.forslagstart3" -> addToChat(m)

            "message.bankid.start.manual" -> {
                val ssn = m.body.text

                val ssnResponse = memberService.auth(ssn)

                if (!ssnResponse.isPresent) {
                    log.error("Could not start bankIdAuthentication!")
                    nxtMsg = "message.bankid.start.manual.error"
                } else {
                    userContext.startBankIdAuth(ssnResponse.get())
                }

                if (nxtMsg == "") {
                    nxtMsg = "message.bankid.autostart.respond"
                }

                addToChat(m)
            }
        }

        val handledNxtMsg = handleSingleSelect(m, nxtMsg, listOf(MESSAGE_HUS))

        completeRequest(handledNxtMsg)
    }

    private fun handleStudentEntrypoint(defaultMessage: String, uc: UserContext): String {
        val onboardingData = uc.onBoardingData
        return if (onboardingData.age in 1..29) {
            "message.student"
        } else defaultMessage
    }

    private fun handleStudentPolicyLivingSpace(defaultMessage: String, uc: UserContext): String {
        val onboardingData = uc.onBoardingData
        val isStudent = onboardingData.isStudent

        if (!isStudent) {
            return defaultMessage
        }

        val livingSpace = onboardingData.livingSpace
        return if (livingSpace > 50) {
            MESSAGE_STUDENT_LIMIT_LIVING_SPACE
        } else defaultMessage

    }

    private fun handleStudentPolicyPersonLimit(defaultMessage: String, uc: UserContext): String {
        val onboardingData = uc.onBoardingData
        val isStudent = onboardingData.isStudent
        if (!isStudent) {
            return defaultMessage
        }

        val livingSpace = onboardingData.livingSpace
        if (livingSpace > 50) {
            return defaultMessage
        }

        val personsInHousehold = onboardingData.personsInHouseHold
        if (personsInHousehold > 2) {
            onboardingData.studentPolicyEligibility = false
            return MESSAGE_STUDENT_LIMIT_PERSONS
        }

        onboardingData.studentPolicyEligibility = true

        val houseType = onboardingData.houseType
        if (houseType == ProductTypes.BRF.toString()) {
            onboardingData.houseType = ProductTypes.STUDENT_BRF.toString()
            return MESSAGE_STUDENT_ELIGIBLE_BRF
        }

        if (houseType == ProductTypes.RENT.toString()) {
            onboardingData.houseType = ProductTypes.STUDENT_RENT.toString()
            return MESSAGE_STUDENT_ELIGIBLE_RENT
        }

        log.error("This state should be unreachable")
        return defaultMessage
    }

    private fun handleFriFraga(userContext: UserContext, m: Message) {
        userContext.putUserData(
            "{ONBOARDING_QUESTION_" + LocalDateTime.now().toString() + "}", m.body.text
        )
        eventPublisher.publishEvent(
            OnboardingQuestionAskedEvent(userContext.memberId, m.body.text)
        )
        addToChat(m)
    }

    private fun handleUnderwritingLimitResponse(
        userContext: UserContext, m: Message, messageId: String
    ): String {
        userContext.putUserData("{PHONE_NUMBER}", m.body.text)
        val type = if (messageId.endsWith("householdsize"))
            UnderwritingLimitExcededEvent.UnderwritingType.HouseholdSize
        else
            UnderwritingLimitExcededEvent.UnderwritingType.HouseingSize

        val onBoardingData = userContext.onBoardingData
        eventPublisher.publishEvent(
            UnderwritingLimitExcededEvent(
                userContext.memberId,
                m.body.text,
                onBoardingData.firstName,
                onBoardingData.familyName,
                type
            )
        )

        addToChat(m)
        return "message.uwlimit.tack"
    }

    private fun endConversation(userContext: UserContext) {
        userContext.completeConversation(this)
    }

    /*
   * Generate next chat message or ends conversation
   */
    override fun completeRequest(nxtMsg: String) {
        var nxtMsg = nxtMsg
        when (nxtMsg) {
            "message.medlem", "message.bankid.start", MESSAGE_LAGENHET, MESSAGE_LOGIN_WITH_EMAIL, MESSAGE_LOGIN_FAILED_WITH_EMAIL -> {
                val authResponse = memberService.auth(userContext.memberId)

                if (!authResponse.isPresent) {
                    log.error("Could not start bankIdAuthentication!")

                    nxtMsg = MESSAGE_ONBOARDINGSTART_SHORT
                } else {
                    val bankIdAuthResponse = authResponse.get()
                    userContext.startBankIdAuth(bankIdAuthResponse)
                }
            }
            MESSAGE_HUS -> {
                userContext.completeConversation(this)
                val conversation =
                    conversationFactory.createConversation(HouseOnboardingConversation::class.java, userContext)
                userContext.startConversation(conversation, HouseConversationConstants.HOUSE_FIRST.id)
                return
            }
            "onboarding.done" -> {
            }
            "" -> {
                log.error("I dont know where to go next...")
                nxtMsg = "error"
            }
        }

        super.completeRequest(nxtMsg)
    }

    override fun getSelectItemsForAnswer(): List<SelectItem> {

        val items = Lists.newArrayList<SelectItem>()

        val questionId: String
        if (userContext.onBoardingData.houseType == MESSAGE_HUS) {
            questionId = MESSAGE_FRIONBOARDINGFRAGA

        } else {
            questionId = MESSAGE_FRIFRAGA
            items.add(SelectLink.toOffer("Visa mig förslaget", "message.forslag.dashboard"))
        }

        items.add(SelectOption("Jag har en till fråga", questionId))

        return items
    }

    override fun canAcceptAnswerToQuestion(): Boolean {
        return true
    }

    override fun bankIdAuthComplete() {

        when {
            userContext.onBoardingData.userHasSigned!! -> {
                userContext.completeConversation(this)
                val mc = conversationFactory.createConversation(MainConversation::class.java, userContext)
                userContext.startConversation(mc, MESSAGE_HEDVIG_COM_POST_LOGIN)
            }
            userContext.getDataEntry(LOGIN) != null -> {
                userContext.removeDataEntry(LOGIN)
                addToChat(getMessage("message.membernotfound"))
            }
            else -> addToChat(getMessage(MESSAGE_BANKIDJA))
        }
    }

    fun emailLoginComplete() {
        when {
            userContext.onBoardingData.userHasSigned ?: false -> {
                userContext.completeConversation(this)
                val mc = conversationFactory.createConversation(MainConversation::class.java, userContext)
                userContext.startConversation(mc)
            }
        }
    }

    override fun bankIdAuthCompleteNoAddress() {
        addToChat(getMessage("message.bankidja.noaddress"))
    }

    override fun bankIdAuthGeneralCollectError() {
        addToChat(getMessage("message.bankid.error"))
        val bankIdStartMessage = userContext.onBoardingData.bankIdMessage
        addToChat(getMessage(bankIdStartMessage))
    }

    override fun memberSigned(referenceId: String) {
        val signed = userContext.onBoardingData.userHasSigned

        if (!signed) {
            val maybeActiveConversation = userContext.activeConversation
            if (maybeActiveConversation.isPresent) {
                val activeConversation = maybeActiveConversation.get()
                if (activeConversation.containsConversation(FreeChatConversation::class.java)) {
                    activeConversation.setConversationStatus(Conversation.conversationStatus.COMPLETE)
                    userContext.setActiveConversation(this)

                    // Duct tape to shift onboarding conversation back into the correct state
                    val onboardingConversation = userContext
                        .activeConversation
                        .orElseThrow {
                            RuntimeException(
                                "active conversation is for some reason not onboarding chat anymore"
                            )
                        }
                    onboardingConversation.conversationStatus = Conversation.conversationStatus.ONGOING
                }
            }

            addToChat(getMessage("message.kontraktklar.ss"))
            userContext.onBoardingData.userHasSigned = true
            userContext.setInOfferState(false)
            userContext.setOnboardingComplete()

            val productType = userContext.getDataEntry(UserData.HOUSE)
            val memberId = userContext.memberId
            val fiftyKLimit = userContext.getDataEntry("{50K_LIMIT}")
            val twentyFiveKLimit = userContext.getDataEntry(UserData.TWENTYFIVE_THOUSAND_LIMIT)
            when {
                fiftyKLimit == "true" -> eventPublisher.publishEvent(RequestObjectInsuranceEvent(memberId, productType))
                twentyFiveKLimit == "true" -> eventPublisher.publishEvent(
                    RequestStudentObjectInsuranceEvent(
                        memberId,
                        productType
                    )
                )
                else -> eventPublisher.publishEvent(MemberSignedEvent(memberId, productType))
            }
        }
    }

    override fun bankIdSignError() {
        addToChat(getMessage("message.kontrakt.signError"))
    }

    override fun oustandingTransaction() {}

    override fun noClient() {}

    override fun started() {}

    override fun userSign() {}

    override fun couldNotLoadMemberProfile() {
        addToChat(getMessage("message.missing.bisnode.data"))
    }

    override fun signalSignFailure(errorType: ErrorType, detail: String) {
        addBankIdErrorMessage(errorType, "message.kontraktpop.startBankId")
    }

    override fun signalAuthFailiure(errorType: ErrorType, detail: String) {
        addBankIdErrorMessage(errorType, userContext.onBoardingData.bankIdMessage)
    }

    private fun addBankIdErrorMessage(errorType: ErrorType, baseMessage: String) {
        val errorPostfix: String = when (errorType) {
            ErrorType.EXPIRED_TRANSACTION -> ".bankid.error.expiredTransaction"
            ErrorType.CERTIFICATE_ERR -> ".bankid.error.certificateError"
            ErrorType.USER_CANCEL -> ".bankid.error.userCancel"
            ErrorType.CANCELLED -> ".bankid.error.cancelled"
            ErrorType.START_FAILED -> ".bankid.error.startFailed"
            ErrorType.INVALID_PARAMETERS -> ".bankid.error.invalidParameters"
            else -> ""
        }
        val messageID = baseMessage + errorPostfix
        log.info("Adding bankIDerror message: {}", messageID)
        addToChat(getMessage(messageID))
    }

    private fun String.capitalizeAll(): String {
        return this.split(regex = Regex("\\s")).map { it.toLowerCase().capitalize() }.joinToString(" ")
    }

    private fun checkSSN(ssn: String): Flag {
        try {
            memberService.checkPersonDebt(ssn)
            val personStatus = memberService.getPersonStatus(ssn)
            if (personStatus.whitelisted) {
                return Flag.GREEN
            }
            return personStatus.flag

        } catch (ex: Exception) {
            log.error("Error getting debt status from member-service", ex)
            return Flag.GREEN
        }
    }

    companion object {
        const val MAX_LIVING_SPACE_RENT_SQM = 250

        const val MESSAGE_HUS = "message.hus"
        const val MESSAGE_NYHETSBREV = "message.nyhetsbrev"
        const val MESSAGE_FRIONBOARDINGFRAGA = "message.frionboardingfraga"
        const val MESSAGE_FRIFRAGA = "message.frifraga"
        const val MESSAGE_TIPSA = "message.tipsa"
        const val MESSAGE_AVSLUTOK = "message.avslutok"
        const val MESSAGE_NAGOTMER = "message.nagotmer"
        const val MESSAGE_ONBOARDINGSTART = "message.onboardingstart"
        const val MESSAGE_ONBOARDINGSTART_SHORT = "message.onboardingstart.short"
        const val MESSAGE_ONBOARDINGSTART_ASK_NAME = "message.onboardingstart.ask.name"
        const val MESSAGE_ONBOARDINGSTART_REPLY_NAME = "message.onboardingstart.reply.name"
        const val MESSAGE_ONBOARDINGSTART_ASK_EMAIL = "message.onboardingstart.ask.email"
        const val MESSAGE_ONBOARDINGSTART_ASK_EMAIL_ALT = "message.onboardingstart.ask.email.two"
        const val MESSAGE_ONBOARDINGSTART_ASK_EMAIL_NOT_MEMBER = "message.onboardingstart.ask.email.not.member"
        const val MESSAGE_LOGIN_ASK_EMAIL = "message.login.ask.email"
        const val MESSAGE_FORSLAG = "message.forslag"
        const val MESSAGE_FORSLAG2 = "message.forslag2"
        const val MESSAGE_FORSLAG2_ALT_1 = "message.forslag2.one"
        const val MESSAGE_FORSLAG2_ALT_2 = "message.forslag2.two"
        const val MESSAGE_50K_LIMIT = "message.fifty.k.limit"
        const val MESSAGE_50K_LIMIT_YES_NO = "message.fifty.k.limit.yes.no"
        @JvmField
        val MESSAGE_50K_LIMIT_YES_YES = "message.fifty.k.limit.yes.yes"
        const val MESSAGE_50K_LIMIT_YES = "message.fifty.k.limit.yes"
        const val MESSAGE_50K_LIMIT_NO = "message.fifty.k.limit.no"
        const val MESSAGE_50K_LIMIT_NO_1 = "message.fifty.k.limit.no.one"
        const val MESSAGE_PHONENUMBER = "message.phonenumber"
        const val MESSAGE_FORSAKRINGIDAG = "message.forsakringidag"
        const val MESSAGE_SAKERHET = "message.sakerhet"
        const val MESSAGE_FORSAKRINGIDAGJA = "message.forsakringidagja"
        const val MESSAGE_BYTESINFO = "message.bytesinfo"
        const val MESSAGE_ANNATBOLAG = "message.annatbolag"
        const val MESSAGE_FORSLAGSTART = "message.forslagstart"
        const val MESSAGE_EMAIL = "message.email"
        const val MESSAGE_PRE_FORSLAGSTART = "message.pre.forslagstart"
        @JvmField
        val MESSAGE_START_LOGIN = "message.start.login"
        const val MESSAGE_LAGENHET_PRE = "message.lagenhet.pre"
        const val MESSAGE_LAGENHET = "message.lagenhet"
        const val MESSAGE_LAGENHET_NO_PERSONNUMMER = "message.lagenhet.no.personnummer"
        const val MESSAGE_LAGENHET_ADDRESSNOTFOUND = "message.lagenhet.addressnotfound"

        const val MESSAGE_STUDENT_LIMIT_PERSONS = "message.student.limit.persons"
        const val MESSAGE_STUDENT_LIMIT_LIVING_SPACE = "message.student.limit.livingspace"
        const val MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE = "message.student.limit.livingspace.lghtyp"
        const val MESSAGE_STUDENT_ELIGIBLE_BRF = "message.student.eligible.brf"
        const val MESSAGE_STUDENT_ELIGIBLE_RENT = "message.student.eligible.rent"
        const val MESSAGE_STUDENT_25K_LIMIT = "message.student.twentyfive.k.limit"
        const val MESSAGE_STUDENT_25K_LIMIT_YES = "message.student.twentyfive.k.limit.yes"

        const val MESSAGE_LOGIN_WITH_EMAIL_ASK_PASSWORD = "message.login.with.mail.ask.password"
        const val MESSAGE_LOGIN_WITH_EMAIL = "message.login.with.mail"
        const val MESSAGE_LOGIN_WITH_EMAIL_TRY_AGAIN = "message.login.with.mail.try.again"
        const val MESSAGE_LOGIN_WITH_EMAIL_PASSWORD_SUCCESS = "message.login.with.mail.passwrod.success"
        const val MESSAGE_LOGIN_FAILED_WITH_EMAIL = "message.login.failed.with.mail"
        const val MESSAGE_INSURER_NOT_SWITCHABLE = "message.bolag.not.switchable"
        const val MESSAGE_MEMBER_UNDER_EIGHTEEN = "message.member.under.eighteen"

        const val MESSAGE_ASK_NR_RESIDENTS = "message.pers"

        @JvmField
        val IN_OFFER = "{IN_OFFER}"
        @JvmField
        val MESSAGE_BANKIDJA = "message.bankidja"
        private const val MESSAGE_KVADRAT = "message.kvadrat"
        private const val MESSAGE_KVADRAT_ALT = "message.kvadrat.one"
        @JvmField
        val MESSAGE_VARBORDUFELADRESS = "message.varbordufeladress"
        private const val MESSAGE_NOTMEMBER = "message.notmember"

        /*
    * Need to be stateless. I.e no data beyond response scope
    *
    * Also, message names cannot end with numbers!! Numbers are used for internal sectioning
    */
        private val log = LoggerFactory.getLogger(OnboardingConversationDevi::class.java)

        val emoji_smile = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x98.toByte(), 0x81.toByte()),
            Charset.forName("UTF-8")
        )
        val emoji_school_satchel = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x8E.toByte(), 0x92.toByte()),
            Charset.forName("UTF-8")
        )
        val emoji_mag = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x94.toByte(), 0x8D.toByte()),
            Charset.forName("UTF-8")
        )
        val emoji_thumbs_up = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x91.toByte(), 0x8D.toByte()),
            Charset.forName("UTF-8")
        )
        val emoji_hug = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0xA4.toByte(), 0x97.toByte()),
            Charset.forName("UTF-8")
        )
        val emoji_flushed_face = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x98.toByte(), 0xB3.toByte()),
            Charset.forName("UTF-8")
        )
    }
}
