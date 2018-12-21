package com.hedvig.botService.chat

import com.google.common.collect.Lists
import com.hedvig.botService.dataTypes.*
import com.hedvig.botService.enteties.SignupCode
import com.hedvig.botService.enteties.SignupCodeRepository
import com.hedvig.botService.enteties.UserContext
import com.hedvig.botService.enteties.message.*
import com.hedvig.botService.enteties.userContextHelpers.UserData
import com.hedvig.botService.enteties.userContextHelpers.UserData.LOGIN
import com.hedvig.botService.serviceIntegration.memberService.MemberService
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignResponse
import com.hedvig.botService.serviceIntegration.memberService.exceptions.ErrorType
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService
import com.hedvig.botService.services.events.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class OnboardingConversationDevi
constructor(
    private val memberService: MemberService,
    private val productPricingService: ProductPricingService,
    private val signupRepo: SignupCodeRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val conversationFactory: ConversationFactory
) : Conversation(), BankIdChat {

    var queuePos: Int? = null

    enum class ProductTypes {
        BRF,
        RENT,
        RENT_BRF,
        SUBLET_RENTAL,
        SUBLET_BRF,
        STUDENT_BRF,
        STUDENT_RENT,
        LODGER
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
                    "Hej! Jag heter Hedvig 👋\u000CVad heter du?", TextContentType.GIVEN_NAME, KeyboardType.DEFAULT, "Förnamn"
                )
            )
            { body, u, message ->
                u.onBoardingData.firstName = body.text.trim().toLowerCase().capitalize()
                addToChat(message, u)
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
                body.text = "Min email är $trimmedEmail"
                addToChat(message, userContext)
                MESSAGE_FORSLAGSTART
            }
        )
        this.setExpectedReturnType(MESSAGE_ONBOARDINGSTART_ASK_EMAIL, EmailAdress())


        this.createChatMessage(
            "message.membernotfound",
            MessageBodySingleSelect(
                "Hmm, det verkar som att du inte är medlem här hos mig ännu" + "\u000CMen jag tar gärna fram ett försäkringsförslag till dig, det är precis som allt annat med mig superenkelt",
                Lists.newArrayList<SelectItem>(SelectOption("Låter bra!", MESSAGE_FORSLAGSTART))
            )
        )

        this.createMessage(
            MESSAGE_SIGNUP_TO_WAITLIST,
            MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
            MessageBodyText("Det ordnar jag! Vad är din mailadress?", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS)
        )
        this.setExpectedReturnType(MESSAGE_SIGNUP_TO_WAITLIST, EmailAdress())

        this.createMessage(
            "message.signup.email",
            MessageHeader(MessageHeader.HEDVIG_USER_ID, -1, true),
            MessageBodyText("Det ordnar jag! Vad är din mailadress?", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS)
        )
        this.setExpectedReturnType("message.signup.email", EmailAdress())

        this.createChatMessage(
            "message.signup.checkposition",
            MessageBodySingleSelect(
                "Tack!" + "\u000CJag hör av mig till dig snart, ha det fint så länge! ✌️",
                listOf(
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

        this.createChatMessage(
            MESSAGE_SIGNUP_NOT_ACTIVATED_YET,
            MessageBodySingleSelect(
                "Hmm, det verkar inte som att du är aktiverad än 👀"
                        + "\u000CTitta in igen när du fått aktiveringsmailet"
                        + "\u000CJag hör av mig snart!",
                listOf(
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

        this.createChatMessage(
            MESSAGE_SIGNUP_NOT_REGISTERED_YET,
            MessageBodySingleSelect(
                "Det ser inte ut som att du har skrivit upp dig på väntelistan än"
                        + "\u000CMen nu har jag din mailadress, så jag lägger till den!"
                        + "\u000CVi hörs snart!",
                listOf(
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

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
        this.addRelay("message.notmember.start", MESSAGE_FORSLAGSTART)

        // Deprecated
        this.createChatMessage(
            "message.waitlist.user.alreadyactive",
            MessageBodyText(
                "Grattis! "
                        + emoji_tada
                        + " Nu kan du bli medlem hos Hedvig\u000CKolla din mail, där ska du ha fått en aktiveringkod som du ska ange här\u000CVi ses snart! "
                        + emoji_smile
            )
        )

        // Deprecated
        this.createChatMessage(
            "message.activate.code.used",
            MessageBodySingleSelect(
                "Det verkar som koden redan är använd... \u000CHar du aktiverat koden på en annan enhet så kan du logga in direkt med bankId.",
                listOf(SelectOption("Jag är redan medlem och vill logga in", "message.medlem"))
            )
        )

        // Deprecated
        this.createMessage(
            "message.signup.flerval",
            MessageBodySingleSelect(
                "",
                listOf(
                    SelectOption(
                        "Kolla min plats på väntelistan", "message.signup.checkposition"
                    ),
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

        this.createMessage(
            MESSAGE_WAITLIST_NOT_ACTIVATED,
            MessageBodySingleSelect(
                "Du verkar redan stå på väntelistan. Din plats är {SIGNUP_POSITION}!",
                listOf(
                    SelectOption(
                        "Kolla min plats på väntelistan", "message.signup.checkposition"
                    ),
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

        // Deprecated
        this.createMessage(
            "message.activate.nocode",
            MessageBodySingleSelect(
                "Jag känner inte igen den koden tyvärr $emoji_thinking",
                listOf(
                    SelectOption(
                        "Kolla min plats på väntelistan", "message.signup.checkposition"
                    ),
                    SelectOption(
                        "Jag har fått ett aktiveringsmail", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST
                    )
                )
            )
        )

        this.createMessage(
            MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST,
            MessageBodyText("Kul! Skriv in din mailadress här")
        )
        this.setExpectedReturnType(MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST, EmailAdress())

        this.createMessage(MESSAGE_ACTIVATE_OK_A, MessageBodyParagraph("Välkommen!"), 1000)
        this.addRelay(MESSAGE_ACTIVATE_OK_A, MESSAGE_ACTIVATE_OK_B)

        this.createMessage(
            MESSAGE_ACTIVATE_OK_B,
            MessageBodyParagraph("Nu ska jag ta fram ett försäkringsförslag åt dig"),
            2000
        )
        this.addRelay(MESSAGE_ACTIVATE_OK_B, MESSAGE_FORSLAGSTART)

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
                    "ååååmmddxxxx"
                )
            ) { body, uc, m ->

                val trimmedSSN = body.text.trim()
                body.text = "${trimmedSSN.dropLast(4)}-****"
                addToChat(m, uc)

                uc.onBoardingData.ssn = trimmedSSN
                val response = memberService.lookupAddressSWE(trimmedSSN, uc.memberId)

                if (response != null) {
                    uc.onBoardingData.let {
                        it.familyName = response.lastName
                        it.firstName = response.firstName
                        it.birthDate = LocalDate.parse(
                            "${trimmedSSN.substring(0, 4)}-${trimmedSSN.substring(
                                4,
                                6
                            )}-${trimmedSSN.substring(6, 8)}"
                        )

                        if (response.address != null) {
                            it.addressCity = response.address.city
                            it.addressStreet = response.address.street
                            it.addressZipCode = response.address.zipCode
                        }
                    }
                }

                if (response?.address != null) {
                    MESSAGE_BANKIDJA
                } else {
                    "message.lagenhet.addressnotfound"
                }
            }
        )
        this.setExpectedReturnType(MESSAGE_LAGENHET_NO_PERSONNUMMER, SSNSweden())


        this.createChatMessage(
            "message.lagenhet.addressnotfound",
            WrappedMessage(
                MessageBodyText(
                    "Konstigt, just nu kan jag inte hitta din adress. Så jag behöver ställa några extra frågor 😊\u000C"
                            + "Vad heter du i efternamn?"
                , TextContentType.FAMILY_NAME, KeyboardType.DEFAULT)
            ) { b, uc, m ->
                uc.onBoardingData.familyName = b.text.trim().toLowerCase().capitalize()
                addToChat(m, uc)

                "message.varborduadress"
            })

        this.createChatMessage(
            MESSAGE_LAGENHET,
            WrappedMessage(
                MessageBodySingleSelect(
                    "Har du BankID? I så fall kan vi hoppa över några frågor så du får se ditt prisförslag snabbare!",
                    listOf(
                        SelectLink(
                            "Fortsätt med BankID",
                            "message.bankid.autostart.respond", null,
                            "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                            false
                        ),
                        SelectOption("Fortsätt utan", "message.manuellnamn")
                    )
                )
            )
            { m, uc, _ ->
                val obd = uc.onBoardingData
                if (m.selectedItem.value == "message.bankid.autostart.respond") {
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
            MESSAGE_START_LOGIN, MessageBodyParagraph("Välkommen tillbaka! $emoji_hug"), 1500
        )
        this.addRelay(MESSAGE_START_LOGIN, "message.bankid.start")

        this.createChatMessage(
            "message.bankid.start",
            WrappedMessage(
                MessageBodySingleSelect(
                    "Bara att logga in så ser du din försäkring",
                    SelectLink(
                        "Logga in med BankID",
                        "message.bankid.autostart.respond", null,
                        "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                        false
                    ),
                    SelectOption("Jag är inte medlem", MESSAGE_NOTMEMBER)
                )
            ) { m, uc, _ ->
                val obd = uc.onBoardingData
                if (m.selectedItem.value == "message.bankid.autostart.respond") {
                    obd.bankIdMessage = "message.bankid.start"
                } else if (m.selectedItem.value == MESSAGE_NOTMEMBER) {
                    uc.putUserData(LOGIN, "false")
                }

                m.selectedItem.value
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

        this.createChatMessage(
            MESSAGE_HUS,
            MessageBodySingleSelect(
                "Åh, typiskt! Just nu försäkrar jag bara lägenheter\u000C" + "Om du vill ge mig din mailadress så kan jag höra av mig när jag försäkrar annat också",
                SelectOption("Okej!", MESSAGE_NYHETSBREV),
                SelectOption("Tack, men nej tack", "message.avslutok")
            )
        )

        this.createMessage(MESSAGE_NYHETSBREV, MessageBodyText("Topp! Vad är mailadressen?", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS))
        this.setExpectedReturnType(MESSAGE_NYHETSBREV, EmailAdress())
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
                addToChat(m, uc)
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
            MessageBodyText("Tack {NAME}! Nu skulle jag behöva veta vilken gatuadress bor du på?",
                TextContentType.STREET_ADDRESS_LINE1, KeyboardType.DEFAULT)
        )

        this.createMessage(
            MESSAGE_VARBORDUFELADRESS,
            MessageBodyText("Inga problem! Vad är gatuadressen till lägenheten du vill försäkra?",
                TextContentType.STREET_ADDRESS_LINE1, KeyboardType.DEFAULT, "Kungsgatan 1")
        )
        this.createMessage(
            "message.varbordufelpostnr", MessageBodyNumber("Och vad har du för postnummer?", TextContentType.POSTAL_CODE, "123 45")
        )
        this.setExpectedReturnType("message.varbordufelpostnr", ZipCodeSweden())

        this.createMessage(MESSAGE_KVADRAT, MessageBodyNumber("Hur många kvadratmeter är lägenheten?"))
        this.setExpectedReturnType(MESSAGE_KVADRAT, LivingSpaceSquareMeters())

        this.createChatMessage(
            "message.manuellnamn",
            MessageBodyText(
                "Inga problem! Då ställer jag bara några extra frågor nu\u000CMen om du vill bli medlem sen så måste du signera med BankID, bara så du vet!\u000CVad heter du i förnamn?"
            ,TextContentType.GIVEN_NAME, KeyboardType.DEFAULT
            )
        )

        this.createMessage(
            "message.manuellfamilyname",
            MessageBodyText("Kul att ha dig här {NAME}! Vad heter du i efternamn?", TextContentType.FAMILY_NAME, KeyboardType.DEFAULT)
        )

        this.createMessage(
            "message.manuellpersonnr",
            MessageBodyNumber("Tack! Vad är ditt personnummer? (12 siffror)")
        )
        this.setExpectedReturnType("message.manuellpersonnr", SSNSweden())
        this.createMessage("message.varborduadress", MessageBodyText("Vilken gatuadress bor du på?", TextContentType.STREET_ADDRESS_LINE1, KeyboardType.DEFAULT, "Kungsgatan 1"))
        this.createMessage("message.varbordupostnr", MessageBodyNumber("Vad är ditt postnummer?", TextContentType.POSTAL_CODE, "123 45"))
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

        this.createMessage("message.pers", MessageBodyNumber("Okej! Hur många bor där?"))
        this.setExpectedReturnType("message.pers", HouseholdMemberNumber())

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
                "Nu behöver jag bara din mailadress så att jag kan skicka en bekräftelse", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS
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
                SelectOption("Folksam", "Folksam"),
                SelectOption("Trygg-Hansa", "Trygg-Hansa"),
                SelectOption("Länsförsäkringar", "Länsförsäkringar"),
                SelectOption("Annat bolag", "message.bolag.annat.expand"),
                SelectOption("Ingen aning", "message.bolag.vetej")
            )
        )

        this.createMessage(
            "message.bolag.annat.expand",
            MessageBodySingleSelect(
                "Okej! Är det något av dessa kanske?",
                SelectOption("Moderna", "Moderna"),
                SelectOption("ICA", "ICA"),
                SelectOption("Gjensidige", "Gjensidige"),
                SelectOption("Vardia", "Vardia"),
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
            MESSAGE_BYTESINFO,
            MessageBodySingleSelect(
                "👀\u000C" +
                        "Okej, om du blir medlem hos mig sköter jag bytet åt dig\u000CSå när din gamla försäkring går ut, flyttas du automatiskt till Hedvig",
                listOf(
                    SelectOption("Jag förstår", MESSAGE_FORSLAG2), // Create product
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
                        add(SelectOption("Okej!", MESSAGE_FORSLAG2)) // Create product
                    }
                })
        )

        this.createChatMessage(
            MESSAGE_50K_LIMIT,
            MessageBodySingleSelect(
                "Toppen!\u000CÄger du något som du tar med dig utanför hemmet som är värt över 50 000 kr som du vill försäkra? 💍",

                SelectOption("Ja, berätta om objektsförsäkring", MESSAGE_50K_LIMIT_YES),
                SelectOption("Nej, gå vidare utan", MESSAGE_50K_LIMIT_NO)
            )
        )

        this.createChatMessage(
            MESSAGE_50K_LIMIT_YES,
            MessageBodySingleSelect(
                "Om du har något som är värt mer än 50 000 kr och som du har med dig på stan, så behöver du lägga till ett extra skydd för den saken !\u000CDet kallas objektsförsäkring, och du lägger enkelt till det i efterhand om du skaffar Hedvig",
                SelectOption("Jag förstår!", MESSAGE_50K_LIMIT_YES_YES)
            )
        )

        this.createMessage(MESSAGE_50K_LIMIT_YES_YES, MessageBodyParagraph("Det fixar jag!"), 1500)
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

        this.createMessage(
            MESSAGE_FORSLAG2,
            MessageBodySingleSelect(
                "Sådärja, tack {NAME}! Det var alla frågor jag hade!",
                Lists.newArrayList<SelectItem>(
                    SelectLink.toOffer("Gå vidare för att se ditt förslag 👏", "message.forslag.dashboard")
                )
            )
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
            "message.mail",
            MessageBodyText(
                "Tackar.\nOch din mailadress så jag kan skicka en bekräftelse när vi skrivit på?",
                TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS
            )
        )

        // (FUNKTION: FYLL I MAILADRESS) = FÄLT
        this.setExpectedReturnType("message.mail", EmailAdress())

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

        this.createMessage("message.kontrakt.great", MessageBodyParagraph("Härligt!"), 1000)
        this.addRelay("message.kontrakt.great", "message.kontrakt")

        this.createMessage(
            "message.kontrakt.signError",
            MessageBodyParagraph("Hmm nu blev något fel! Vi försöker igen $emoji_flushed_face"),
            1000
        )
        this.addRelay("message.kontrakt.signError", "message.kontrakt")

        this.createMessage(
            "message.kontrakt.signProcessError",
            MessageBodyParagraph("Vi försöker igen $emoji_flushed_face"),
            1000
        )
        this.addRelay("message.kontrakt.signProcessError", "message.kontrakt")

        this.createChatMessage(
            "message.kontrakt",
            WrappedMessage(
                MessageBodySingleSelect(
                    "Då är det bara att signera, sen är vi klara",
                    listOf(SelectOption("Okej!", "message.kontraktpop.startBankId"))
                )
            ) { m, userContext, _ ->
                if (m.selectedItem.value == "message.kontrakt") {
                    m.text = m.selectedItem.text
                } else {
                    val ud = userContext.onBoardingData

                    val signData: Optional<BankIdSignResponse>

                    val signText: String
                    signText = if (ud.currentInsurer != null) {
                        "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag vill byta till Hedvig när min gamla försäkring går ut. Jag ger också Hedvig fullmakt att byta försäkringen åt mig."
                    } else {
                        "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag skaffar en försäkring hos Hedvig."
                    }

                    signData = memberService.sign(ud.ssn, signText, userContext.memberId)

                    if (signData.isPresent) {
                        userContext.startBankIdSign(signData.get())
                    } else {
                        log.error("Could not start signing process.")
                        return@WrappedMessage "message.kontrakt.signError"
                    }

                }
                m.selectedItem.value
            }
        )

        this.createMessage(
            "message.kontraktpop.bankid.collect", MessageBodyBankIdCollect("{REFERENCE_TOKEN}")
        )

        this.createChatMessage(
            "message.kontraktpop.startBankId",
            WrappedMessage(
                MessageBodySingleSelect(
                    "För signeringen använder vi BankID",
                    listOf(
                        SelectLink(
                            "Öppna BankID",
                            "message.kontraktpop.bankid.collect", null,
                            "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                            false
                        )
                    )
                )
            ) { m, uc, _ ->
                val obd = uc.onBoardingData
                if (m.selectedItem.value == "message.kontraktpop.bankid.collect") {
                    obd.bankIdMessage = "message.kontraktpop.startBankId"
                }

                m.selectedItem.value
            }
        )

        setupBankidErrorHandlers("message.kontraktpop.startBankId", "message.kontrakt")

        //Deperecated 2018-12-17
        this.createMessage(
            "message.kontraktklar",
            MessageBodyParagraph("Hurra! 🎉 Välkommen som medlem {NAME}!")
        )

        this.createMessage(
            "message.kontraktklar.ss",
            MessageBodySingleSelect(
                "Hurra! 🎉 Välkommen som medlem {NAME}!",
                SelectLink.toDashboard("Börja utforska appen 💥", "message.noop")
            )
        )

        this.createMessage("message.kontrakt.email", MessageBodyText("OK! Vad är din mailadress?", TextContentType.EMAIL_ADDRESS, KeyboardType.EMAIL_ADDRESS))
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
            "message.avslutok",
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

        this.createMessage("message.bikedone", MessageBodyText("Nu har du sett hur det funkar..."))

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
                        add(SelectOption("Okej, toppen!", MESSAGE_KVADRAT))
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
                Lists.newArrayList<SelectItem>(SelectOption("Okej, jag förstår", "message.student.25klimit"))
            )
        )

        this.createMessage(
            MESSAGE_STUDENT_ELIGIBLE_BRF,
            MessageBodySingleSelect(
                "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 99 kr per månad! 🙌",
                Lists.newArrayList<SelectItem>(SelectOption("Okej, nice!", "message.student.25klimit"))
            )
        )

        this.createMessage(
            MESSAGE_STUDENT_ELIGIBLE_RENT,
            MessageBodySingleSelect(
                "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 79 kr per månad! 🙌",
                Lists.newArrayList<SelectItem>(SelectOption("Okej, nice!", "message.student.25klimit"))
            )
        )

        this.createChatMessage(
            MESSAGE_STUDENT_25K_LIMIT,
            MessageBodySingleSelect("Äger du något som du tar med dig utanför hemmet som är värt över 25 000 kr som du vill försäkra? 💍",
                Lists.newArrayList<SelectItem>(
                    SelectOption("Ja, berätta om objektsförsäkring", MESSAGE_STUDENT_25K_LIMIT_YES),
                    SelectOption("Nej, gå vidare utan", MESSAGE_50K_LIMIT_NO)
                )
            )
        )

        this.createChatMessage(
            MESSAGE_STUDENT_25K_LIMIT_YES,
            MessageBodySingleSelect(
                "Om du har något som är värt mer än 25 000 kr och som du har med dig på stan, så behöver du lägga till ett extra skydd för den saken !\u000CDet kallas objektsförsäkring, och du lägger enkelt till det i efterhand om du skaffar Hedvig",
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

    override fun init(userContext: UserContext) {
        log.info("Starting onboarding conversation")
        if (userContext.getDataEntry("{SIGNED_UP}") == null) {
            startConversation(userContext, MESSAGE_ONBOARDINGSTART_ASK_NAME) // Id of first message
        } else {
            startConversation(userContext, MESSAGE_ACTIVATE_OK_B) // Id of first message
        }
    }

    override fun init(userContext: UserContext, startMessage: String) {
        log.info("Starting onboarding conversation with message: $startMessage")
        if (startMessage == MESSAGE_START_LOGIN) {
            userContext.putUserData(LOGIN, "true")
        }
        startConversation(userContext, startMessage) // Id of first message
    }
    // --------------------------------------------------------------------------- //

    override fun getValue(body: MessageBodyNumber): Int {
        return Integer.parseInt(body.text)
    }

    override fun getValue(body: MessageBodySingleSelect): String {

        for (o in body.choices) {
            if (SelectOption::class.java.isInstance(o) && SelectOption::class.java.cast(o).selected) {
                return SelectOption::class.java.cast(o).value
            }
        }
        return ""
    }

    override fun getValue(body: MessageBodyMultipleSelect): ArrayList<String> {
        val selectedOptions = ArrayList<String>()
        for (o in body.choices) {
            if (SelectOption::class.java.isInstance(o) && SelectOption::class.java.cast(o).selected) {
                selectedOptions.add(SelectOption::class.java.cast(o).value)
            }
        }
        return selectedOptions
    }

    // ------------------------------------------------------------------------------- //
    override fun receiveEvent(e: Conversation.EventTypes, value: String, userContext: UserContext) {
        when (e) {
            // This is used to let Hedvig say multiple message after another
            Conversation.EventTypes.MESSAGE_FETCHED -> {
                log.info("Message fetched: $value")

                // New way of handeling relay messages
                val relay = getRelay(value)
                if (relay != null) {
                    completeRequest(relay, userContext)
                }
                if (value == MESSAGE_FORSLAG2) {
                    completeOnboarding(userContext)
                } else //Deprecated 2018-12-17
                    if (value == "message.kontraktklar") {
                        endConversation(userContext)
                    }
            }
            Conversation.EventTypes.ANIMATION_COMPLETE -> when (value) {
                "animation.bike" -> completeRequest("message.bikedone", userContext)
            }
            Conversation.EventTypes.MODAL_CLOSED -> when (value) {
                "quote" -> completeRequest("message.quote.close", userContext)
            }
            Conversation.EventTypes.MISSING_DATA -> when (value) {
                "bisnode" -> completeRequest("message.missing.bisnode.data", userContext)
            }
        }
    }

    private fun completeOnboarding(userContext: UserContext) {
        val productId = this.productPricingService.createProduct(
            userContext.memberId, userContext.onBoardingData
        )
        userContext.onBoardingData.productId = productId
        this.memberService.finalizeOnBoarding(
            userContext.memberId, userContext.onBoardingData
        )
    }

    override fun handleMessage(userContext: UserContext, m: Message) {
        var nxtMsg = ""

        if (!validateReturnType(m, userContext)) {
            return
        }

        // Lambda
        if (this.hasSelectItemCallback(m.id) && m.body.javaClass == MessageBodySingleSelect::class.java) {
            // MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
            nxtMsg = this.execSelectItemCallback(m.id, m.body as MessageBodySingleSelect, userContext)
            addToChat(m, userContext)
        }

        val onBoardingData = userContext.onBoardingData

        val selectedOption = if (m.body.javaClass == MessageBodySingleSelect::class.java)
            getValue(m.body as MessageBodySingleSelect)
        else
            null

        if (selectedOption != null) {
            // Check the selected option first...
            when (selectedOption) {
                "message.signup.checkposition" -> {
                    log.info("Checking position...")
                    // We do not have the users email
                    if (!(onBoardingData.email != null && onBoardingData.email != "")) {
                        nxtMsg = "message.signup.email"
                    } else { // Update position if there is a code
                        userContext.putUserData(
                            "{SIGNUP_POSITION}",
                            Objects.toString(getSignupQueuePosition(onBoardingData.email))
                        )
                    }
                }
            }
        }

        // ... and then the incomming message id
        when (m.baseMessageId) {
            MESSAGE_ONBOARDINGSTART -> {
                val email = userContext.getDataEntry(EMAIL)
                if (emailIsActivated(email)) {
                    flagCodeAsUsed(email)
                    userContext.putUserData(SIGNED_UP, "true")
                    nxtMsg = MESSAGE_ACTIVATE_OK_A
                }
            }
            MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE, "message.lghtyp" -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem

                // Additional question for sublet contracts
                m.body.text = item.text
                addToChat(m, userContext)
                if (item.value == "message.lghtyp.sublet") {
                    nxtMsg = "message.lghtyp.sublet"
                } else {
                    val obd = userContext.onBoardingData
                    obd.houseType = item.value
                    nxtMsg = "message.pers"
                }
            }
            "message.lghtyp.sublet" -> {
                val item = (m.body as MessageBodySingleSelect).selectedItem
                val obd = userContext.onBoardingData
                obd.houseType = item.value
                m.body.text = item.text
                nxtMsg = "message.pers"
            }

            "message.student" -> {
                val sitem2 = (m.body as MessageBodySingleSelect).selectedItem
                if (sitem2.value == "message.studentja") {
                    m.body.text = "Yes"
                    addToChat(m, userContext)
                    userContext.putUserData("{STUDENT}", "1")
                } else {
                    m.body.text = "Nix"
                }
            }

            MESSAGE_NYHETSBREV -> {
                onBoardingData.newsLetterEmail = m.body.text
                addToChat(m, userContext)
                nxtMsg = MESSAGE_NAGOTMER
            }
            "message.signup.email", MESSAGE_SIGNUP_TO_WAITLIST -> {
                // Logic goes here
                val userEmail = m.body.text.toLowerCase().trim { it <= ' ' }
                onBoardingData.email = userEmail
                m.body.text = userEmail
                addToChat(m, userContext)

                // --------- Logic for user state ------------- //
                val existingSignupCode = findSignupCodeByEmail(userEmail)

                // User already has a signup code
                nxtMsg = if (existingSignupCode.isPresent) {
                    val esc = existingSignupCode.get()
                    if (esc.getActive()!!) { // User should have got an activation code
                        flagCodeAsUsed(userEmail)
                        userContext.putUserData(SIGNED_UP, "true")
                        MESSAGE_ACTIVATE_OK_A
                    } else {
                        "message.signup.checkposition"
                    }
                } else {
                    val sc = createSignupCode(userEmail)
                    userContext.putUserData(EMAIL, userEmail)
                    userContext.putUserData("{SIGNUP_CODE}", sc.code)
                    "message.signup.checkposition"
                }
                userContext.putUserData(
                    "{SIGNUP_POSITION}", Objects.toString(getSignupQueuePosition(userEmail))
                )
            }
            "message.signup.flerval" -> userContext.putUserData(
                "{SIGNUP_POSITION}",
                Objects.toString(getSignupQueuePosition(onBoardingData.email))
            )
            "message.waitlist.user.alreadyactive", "message.activate.nocode.tryagain", MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST -> {
                // Logic goes here
                val email = m.body.text.trim { it <= ' ' }.toLowerCase()
                when {
                    emailIsActivated(email) -> {
                        flagCodeAsUsed(email)
                        userContext.putUserData(SIGNED_UP, "true")
                        userContext.putUserData(EMAIL, email)
                        nxtMsg = MESSAGE_ACTIVATE_OK_A
                        addToChat(m, userContext)

                    }
                    emailIsRegistered(email) == false -> {
                        onBoardingData.email = email
                        val signupCode = createSignupCode(m.body.text)
                        userContext.putUserData("{SIGNUP_CODE}", signupCode.code)
                        userContext.putUserData(EMAIL, email)
                        userContext.putUserData(
                            "{SIGNUP_POSITION}", Objects.toString(getSignupQueuePosition(email))
                        )
                        nxtMsg = MESSAGE_SIGNUP_NOT_REGISTERED_YET

                    }
                    else -> {
                        nxtMsg = MESSAGE_SIGNUP_NOT_ACTIVATED_YET
                        addToChat(m, userContext)
                    }
                }
            }
            MESSAGE_SIGNUP_NOT_REGISTERED_YET, MESSAGE_SIGNUP_NOT_ACTIVATED_YET, "message.signup.checkposition" -> {
                m.body.text = (m.body as MessageBodySingleSelect).selectedItem.text
                addToChat(m, userContext)
                val email = userContext.getDataEntry(EMAIL)
                if (email != null) {
                    nxtMsg = if (emailIsActivated(email)) {
                        MESSAGE_ACTIVATE_OK_A
                    } else {
                        MESSAGE_SIGNUP_NOT_ACTIVATED_YET
                    }
                }
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
            "message.pers" -> {
                val nrPersons = getValue(m.body as MessageBodyNumber)
                onBoardingData.setPersonInHouseHold(nrPersons)
                m.body.text = if (nrPersons == 1) {
                    "Jag bor själv"
                } else {
                    "Vi är $nrPersons"
                }
                addToChat(m, userContext)

                nxtMsg = if (nrPersons > 6) {
                    "message.uwlimit.householdsize"
                } else {
                    handleStudentPolicyPersonLimit(MESSAGE_50K_LIMIT, userContext)
                }
            }
            MESSAGE_KVADRAT -> {
                val kvm = m.body.text
                onBoardingData.livingSpace = java.lang.Float.parseFloat(kvm)
                m.body.text = "$kvm kvm"
                addToChat(m, userContext)
                nxtMsg = if (Integer.parseInt(kvm) > 250) {
                    "message.uwlimit.housingsize"
                } else {
                    handleStudentPolicyLivingSpace("message.lghtyp", userContext)
                }
            }
            "message.manuellnamn" -> {
                onBoardingData.firstName = m.body.text
                addToChat(m, userContext)
                nxtMsg = "message.manuellfamilyname"
            }
            "message.manuellfamilyname" -> {
                onBoardingData.familyName = m.body.text
                addToChat(m, userContext)
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

                addToChat(m, userContext)
                nxtMsg = "message.varborduadress"
            }
            "message.bankidja.noaddress", MESSAGE_VARBORDUFELADRESS, "message.varborduadress" -> {
                onBoardingData.addressStreet = m.body.text
                addToChat(m, userContext)
                nxtMsg = "message.varbordupostnr"
            }
            "message.varbordupostnr" -> {
                onBoardingData.addressZipCode = m.body.text
                addToChat(m, userContext)
                nxtMsg = handleStudentEntrypoint(MESSAGE_KVADRAT, userContext)
            }
            "message.varbordu" -> {
                onBoardingData.addressStreet = m.body.text
                addToChat(m, userContext)
                nxtMsg = MESSAGE_KVADRAT
            }
            "message.mail" -> {
                onBoardingData.email = m.body.text
                addToChat(m, userContext)
                nxtMsg = "message.kontrakt"
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
                addToChat(m, userContext)
                val userData = userContext.onBoardingData
                nxtMsg = if (userData.studentPolicyEligibility == true) {
                    "message.student.25klimit"

                } else {
                    MESSAGE_50K_LIMIT
                }
            }
            MESSAGE_PHONENUMBER -> {
                val trim = m.body.text.trim { it <= ' ' }
                userContext.putUserData("{PHONE_NUMBER}", trim)
                m.body.text = "Mitt telefonnummer är $trim"
                addToChat(m, userContext)
                nxtMsg = MESSAGE_FORSAKRINGIDAG
            }
            MESSAGE_EMAIL -> {
                val trim2 = m.body.text.trim { it <= ' ' }
                userContext.putUserData("{EMAIL}", trim2)
                m.body.text = "Min email är $trim2"
                memberService.updateEmail(userContext.memberId, trim2)
                addToChat(m, userContext)
                endConversation(userContext)
                return
            }
            // nxtMsg = MESSAGE_FORSAKRINGIDAG;
            MESSAGE_50K_LIMIT_YES -> {
                val body1 = m.body as MessageBodySingleSelect
                for (o in body1.choices) {
                    if (o.selected) {
                        m.body.text = o.text
                        addToChat(m, userContext)
                    }
                }
                nxtMsg = handle50KLimitAnswer(userContext, m.body as MessageBodySingleSelect)
            }
            // case "message.bytesinfo":
            "message.bytesinfo2", MESSAGE_FORSAKRINGIDAG, "message.missingvalue", MESSAGE_FORSLAG2 -> {
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
                    addToChat(m, userContext)
                    addToChat(getMessage("message.missingvalue"), userContext)
                } else if (m.id == "message.missingvalue" || item.value == MESSAGE_FORSLAG2) {
                    completeOnboarding(userContext)
                }
            }
            MESSAGE_ANNATBOLAG -> {
                val comp = m.body.text
                userContext.onBoardingData.currentInsurer = comp
                m.body.text = comp
                nxtMsg = MESSAGE_BYTESINFO
                addToChat(m, userContext)
            }
            MESSAGE_FORSAKRINGIDAGJA, "message.bolag.annat.expand" -> {
                val comp = getValue(m.body as MessageBodySingleSelect)
                if (!comp.startsWith("message.")) {
                    userContext.onBoardingData.currentInsurer = comp
                    m.body.text = comp
                    nxtMsg = MESSAGE_BYTESINFO
                    addToChat(m, userContext)
                }
            }
            "message.forslagstart3" -> addToChat(m, userContext)

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

                addToChat(m, userContext)
            }

            "message.kontrakt" -> completeOnboarding(userContext)
        }

        /*
     * In a Single select, there is only one trigger event. Set default here to be a link to a new
     * message
     */
        if (nxtMsg == "" && m.body.javaClass == MessageBodySingleSelect::class.java) {

            val body1 = m.body as MessageBodySingleSelect
            for (o in body1.choices) {
                if (o.selected) {
                    m.body.text = o.text
                    addToChat(m, userContext)
                    nxtMsg = o.value
                }
            }
        }

        completeRequest(nxtMsg, userContext)
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

    private fun handle50KLimitAnswer(
        userContext: UserContext, body: MessageBodySingleSelect
    ): String {
        if (body.selectedItem.value.equals(MESSAGE_50K_LIMIT_YES_YES, ignoreCase = true)) {
            val userData = userContext.onBoardingData
            val studentPolicyEligibility = userData.studentPolicyEligibility
            if (studentPolicyEligibility != null && studentPolicyEligibility == true) {
                userContext.putUserData(UserData.TWENTYFIVE_THOUSAND_LIMIT, "true")
            } else {
                userContext.putUserData("{50K_LIMIT}", "true")
            }
        }
        return MESSAGE_FORSAKRINGIDAG
    }

    private fun handleFriFraga(userContext: UserContext, m: Message) {
        userContext.putUserData(
            "{ONBOARDING_QUESTION_" + LocalDateTime.now().toString() + "}", m.body.text
        )
        eventPublisher.publishEvent(
            OnboardingQuestionAskedEvent(userContext.memberId, m.body.text)
        )
        addToChat(m, userContext)
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

        addToChat(m, userContext)
        return "message.uwlimit.tack"
    }

    private fun endConversation(userContext: UserContext) {
        userContext.completeConversation(this)
    }

    /*
   * Generate next chat message or ends conversation
   */
    public override fun completeRequest(nxtMsg: String, userContext: UserContext) {
        var nxtMsg = nxtMsg
        when (nxtMsg) {
            "message.medlem", "message.bankid.start", MESSAGE_LAGENHET -> {
                val authResponse = memberService.auth(userContext.memberId)

                if (!authResponse.isPresent) {
                    log.error("Could not start bankIdAuthentication!")

                    nxtMsg = MESSAGE_ONBOARDINGSTART_SHORT
                } else {
                    val bankIdAuthResponse = authResponse.get()
                    userContext.startBankIdAuth(bankIdAuthResponse)
                }
            }
            "onboarding.done" -> {
            }
            "" -> {
                log.error("I dont know where to go next...")
                nxtMsg = "error"
            }
        }

        super.completeRequest(nxtMsg, userContext)
    }

    override fun getSelectItemsForAnswer(uc: UserContext): List<SelectItem> {

        val items = Lists.newArrayList<SelectItem>()

        val questionId: String
        if (uc.onBoardingData.houseType == MESSAGE_HUS) {
            questionId = MESSAGE_FRIONBOARDINGFRAGA

        } else {
            questionId = MESSAGE_FRIFRAGA
            items.add(SelectLink.toOffer("Visa mig förslaget", "message.forslag.dashboard"))
        }

        items.add(SelectOption("Jag har en till fråga", questionId))

        return items
    }

    override fun canAcceptAnswerToQuestion(uc: UserContext): Boolean {
        return uc.onBoardingData.houseType != null
    }

    override fun bankIdAuthComplete(userContext: UserContext) {

        when {
            userContext.onBoardingData.userHasSigned!! -> {
                userContext.completeConversation(this)
                val mc = conversationFactory.createConversation(MainConversation::class.java)
                userContext.startConversation(mc)
            }
            userContext.getDataEntry(LOGIN) != null -> {
                userContext.removeDataEntry(LOGIN)
                addToChat(getMessage("message.membernotfound"), userContext)
            }
            else -> addToChat(getMessage(MESSAGE_BANKIDJA), userContext)
        }
    }

    override fun bankIdAuthCompleteNoAddress(uc: UserContext) {
        addToChat(getMessage("message.bankidja.noaddress"), uc)
    }

    override fun bankIdAuthGeneralCollectError(userContext: UserContext) {
        addToChat(getMessage("message.bankid.error"), userContext)
        val bankIdStartMessage = userContext.onBoardingData.bankIdMessage
        addToChat(getMessage(bankIdStartMessage), userContext)
    }

    override fun memberSigned(referenceId: String, userContext: UserContext) {
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

            addToChat(getMessage("message.kontraktklar.ss"), userContext)
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

    override fun bankIdSignError(uc: UserContext) {
        addToChat(getMessage("message.kontrakt.signError"), uc)
    }

    override fun oustandingTransaction(uc: UserContext) {}

    override fun noClient(uc: UserContext) {}

    override fun started(uc: UserContext) {}

    override fun userSign(uc: UserContext) {}

    override fun couldNotLoadMemberProfile(uc: UserContext) {
        addToChat(getMessage("message.missing.bisnode.data"), uc)
    }

    override fun signalSignFailure(errorType: ErrorType, detail: String, uc: UserContext) {
        addBankIdErrorMessage(errorType, "message.kontraktpop.startBankId", uc)
    }

    override fun signalAuthFailiure(errorType: ErrorType, detail: String, uc: UserContext) {
        addBankIdErrorMessage(errorType, uc.onBoardingData.bankIdMessage, uc)
    }

    private fun addBankIdErrorMessage(errorType: ErrorType, baseMessage: String, uc: UserContext) {
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
        addToChat(getMessage(messageID), uc)
    }

    private fun createSignupCode(email: String): SignupCode {
        log.debug("Generate signup code for email: $email")
        val sc = signupRepo
            .findByEmail(email)
            .orElseGet {
                val newCode = SignupCode(email)
                signupRepo.save(newCode)
                newCode
            }
        signupRepo.saveAndFlush(sc)

        eventPublisher.publishEvent(SignedOnWaitlistEvent(email))

        return sc
    }

    private fun findSignupCodeByEmail(email: String): Optional<SignupCode> {
        return signupRepo.findByEmail(email)
    }

    private fun getSignupQueuePosition(email: String): Int {
        val scList = signupRepo.findAllByOrderByDateAsc() as ArrayList<SignupCode>
        var pos = 1
        for (sc in scList) {
            if (!sc.used) {
                log.debug(sc.code + "|" + sc.email + "(" + sc.date + "): " + pos)
                if (sc.email == email) {
                    return queuePos!! + pos
                }
                pos++
            }
        }
        return -1
    }

    private fun emailIsActivated(email: String?): Boolean {
        if (email == null) {
            return false
        }

        val maybeSignupCode = signupRepo.findByEmail(email)
        if (maybeSignupCode.isPresent == false) {
            return false
        }

        val signupCode = maybeSignupCode.get()
        return signupCode.getActive()!!
    }

    private fun emailIsRegistered(email: String): Boolean {
        val maybeSignupCode = signupRepo.findByEmail(email)

        return maybeSignupCode.isPresent
    }

    private fun flagCodeAsUsed(email: String) {
        val maybeSignupCode = signupRepo.findByEmail(email)
        if (maybeSignupCode.isPresent == false) {
            log.error("Attempted to flag nonexistent code as used with email: {}", email)
            return
        }
        val signupCode = maybeSignupCode.get()
        signupCode.setUsed(true)
        signupRepo.save(signupCode)
    }

    companion object {

        const val EMAIL = "{EMAIL}"
        const val SIGNED_UP = "{SIGNED_UP}"
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
        const val MESSAGE_ACTIVATE_OK_A = "message.activate.ok.a"
        const val MESSAGE_ACTIVATE_OK_B = "message.activate.ok.b"
        const val MESSAGE_SIGNUP_TO_WAITLIST = "message.waitlist"
        const val MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST = "message.activate"
        const val MESSAGE_WAITLIST_NOT_ACTIVATED = "message.activate.notactive"
        const val MESSAGE_SIGNUP_NOT_ACTIVATED_YET = "message.signup.notactivatedyet"
        const val MESSAGE_SIGNUP_NOT_REGISTERED_YET = "message.signup.notregisteredyet"
        const val MESSAGE_FORSLAG = "message.forslag"
        const val MESSAGE_FORSLAG2 = "message.forslag2"
        const val MESSAGE_50K_LIMIT = "message.50k.limit"
        const val MESSAGE_50K_LIMIT_YES_NO = "message.50k.limit.yes.no"
        @JvmField
        val MESSAGE_50K_LIMIT_YES_YES = "message.50k.limit.yes.yes"
        const val MESSAGE_50K_LIMIT_YES = "message.50k.limit.yes"
        const val MESSAGE_50K_LIMIT_NO = "message.50k.limit.no"
        const val MESSAGE_50K_LIMIT_NO_1 = "message.50k.limit.no.1"
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

        const val MESSAGE_STUDENT_LIMIT_PERSONS = "message.student.limit.persons"
        const val MESSAGE_STUDENT_LIMIT_LIVING_SPACE = "message.student.limit.livingspace"
        const val MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE = "message.student.limit.livingspace.lghtyp"
        const val MESSAGE_STUDENT_ELIGIBLE_BRF = "message.student.eligible.brf"
        const val MESSAGE_STUDENT_ELIGIBLE_RENT = "message.student.eligible.rent"
        const val MESSAGE_STUDENT_25K_LIMIT = "message.student.25klimit"
        const val MESSAGE_STUDENT_25K_LIMIT_YES = "message.student.25klimit.yes"

        @JvmField
        val IN_OFFER = "{IN_OFFER}"
        @JvmField
        val MESSAGE_BANKIDJA = "message.bankidja"
        private const val MESSAGE_KVADRAT = "message.kvadrat"
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
        val emoji_tada = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x8E.toByte(), 0x89.toByte()),
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
        val emoji_thinking = String(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0xA4.toByte(), 0x94.toByte()),
            Charset.forName("UTF-8")
        )
    }
}
