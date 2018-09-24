package com.hedvig.botService.chat;

import com.google.common.collect.Lists;
import com.hedvig.botService.dataTypes.EmailAdress;
import com.hedvig.botService.dataTypes.HouseholdMemberNumber;
import com.hedvig.botService.dataTypes.LivingSpaceSquareMeters;
import com.hedvig.botService.dataTypes.SSNSweden;
import com.hedvig.botService.dataTypes.TextInput;
import com.hedvig.botService.dataTypes.ZipCodeSweden;
import com.hedvig.botService.enteties.SignupCode;
import com.hedvig.botService.enteties.SignupCodeRepository;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyAudio;
import com.hedvig.botService.enteties.message.MessageBodyBankIdCollect;
import com.hedvig.botService.enteties.message.MessageBodyMultipleSelect;
import com.hedvig.botService.enteties.message.MessageBodyNumber;
import com.hedvig.botService.enteties.message.MessageBodyParagraph;
import com.hedvig.botService.enteties.message.MessageBodyPhotoUpload;
import com.hedvig.botService.enteties.message.MessageBodySingleSelect;
import com.hedvig.botService.enteties.message.MessageBodyText;
import com.hedvig.botService.enteties.message.MessageHeader;
import com.hedvig.botService.enteties.message.SelectItem;
import com.hedvig.botService.enteties.message.SelectLink;
import com.hedvig.botService.enteties.message.SelectOption;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.serviceIntegration.memberService.MemberProfile;
import com.hedvig.botService.serviceIntegration.memberService.MemberService;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdAuthResponse;
import com.hedvig.botService.serviceIntegration.memberService.dto.BankIdSignResponse;
import com.hedvig.botService.serviceIntegration.memberService.exceptions.ErrorType;
import com.hedvig.botService.serviceIntegration.productPricing.ProductPricingService;
import com.hedvig.botService.services.events.MemberSignedEvent;
import com.hedvig.botService.services.events.OnboardingQuestionAskedEvent;
import com.hedvig.botService.services.events.RequestObjectInsuranceEvent;
import com.hedvig.botService.services.events.RequestStudentObjectInsuranceEvent;
import com.hedvig.botService.services.events.SignedOnWaitlistEvent;
import com.hedvig.botService.services.events.UnderwritingLimitExcededEvent;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OnboardingConversationDevi extends Conversation implements BankIdChat {

  public static final String LOGIN = "{LOGIN}";
  public static final String EMAIL = "{EMAIL}";
  public static final String SIGNED_UP = "{SIGNED_UP}";
  public static final String MESSAGE_HUS = "message.hus";
  public static final String MESSAGE_NYHETSBREV = "message.nyhetsbrev";
  public static final String MESSAGE_FRIONBOARDINGFRAGA = "message.frionboardingfraga";
  public static final String MESSAGE_FRIFRAGA = "message.frifraga";
  public static final String MESSAGE_TIPSA = "message.tipsa";
  public static final String MESSAGE_AVSLUTOK = "message.avslutok";
  public static final String MESSAGE_NAGOTMER = "message.nagotmer";
  public static final String MESSAGE_WAITLIST_START = "message.onboardingstart";
  public static final String MESSAGE_ONBOARDING_START = "message.activate.ok.a";
  public static final String MESSAGE_SIGNUP_TO_WAITLIST = "message.waitlist";
  public static final String MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST = "message.activate";
  public static final String MESSAGE_WAITLIST_NOT_ACTIVATED = "message.activate.notactive";
  public static final String MESSAGE_SIGNUP_NOT_ACTIVATED_YET = "message.signup.notactivatedyet";
  public static final String MESSAGE_SIGNUP_NOT_REGISTERED_YET = "message.signup.notregisteredyet";
  public static final String MESSAGE_FORSLAG = "message.forslag";
  public static final String MESSAGE_FORSLAG2 = "message.forslag2";
  public static final String MESSAGE_50K_LIMIT = "message.50k.limit";
  public static final String MESSAGE_50K_LIMIT_YES_NO = "message.50k.limit.yes.no";
  public static final String MESSAGE_50K_LIMIT_YES_YES = "message.50k.limit.yes.yes";
  public static final String MESSAGE_50K_LIMIT_YES = "message.50k.limit.yes";
  public static final String MESSAGE_50K_LIMIT_NO = "message.50k.limit.no";
  public static final String MESSAGE_50K_LIMIT_NO_1 = "message.50k.limit.no.1";
  public static final String MESSAGE_PHONENUMBER = "message.phonenumber";
  public static final String MESSAGE_FORSAKRINGIDAG = "message.forsakringidag";
  public static final String MESSAGE_SAKERHET = "message.sakerhet";
  public static final String MESSAGE_FORSAKRINGIDAGJA = "message.forsakringidagja";
  public static final String MESSAGE_BYTESINFO = "message.bytesinfo";
  public static final String MESSAGE_ANNATBOLAG = "message.annatbolag";
  public static final String MESSAGE_FORSLAGSTART = "message.forslagstart";
  public static final String MESSAGE_EMAIL = "message.email";
  public static final String MESSAGE_PRE_FORSLAGSTART = "message.pre.forslagstart";
  public static final String MESSAGE_START_LOGIN = "message.start.login";
  public static final String MESSAGE_LAGENHET_PRE = "message.lagenhet.pre";
  public static final String MESSAGE_LAGENHET = "message.lagenhet";

  public static final String MESSAGE_STUDENT_LIMIT_PERSONS = "message.student.limit.persons";
  public static final String MESSAGE_STUDENT_LIMIT_LIVING_SPACE =
      "message.student.limit.livingspace";
  public static final String MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE =
      "message.student.limit.livingspace.lghtyp";
  public static final String MESSAGE_STUDENT_ELIGIBLE_BRF = "message.student.eligible.brf";
  public static final String MESSAGE_STUDENT_ELIGIBLE_RENT = "message.student.eligible.rent";
  public static final String MESSAGE_STUDENT_25K_LIMIT = "message.student.25klimit";

  public static final String IN_OFFER = "{IN_OFFER}";
  private static final String MESSAGE_BANKIDJA = "message.bankidja";
  private static final String MESSAGE_KVADRAT = "message.kvadrat";
  private static final String MESSAGE_VARBORDUFELADRESS = "message.varbordufeladress";
  /*
   * Need to be stateless. I.e no data beyond response scope
   *
   * Also, message names cannot end with numbers!! Numbers are used for internal sectioning
   */
  private static Logger log = LoggerFactory.getLogger(OnboardingConversationDevi.class);
  private final ApplicationEventPublisher eventPublisher;
  private final MemberService memberService;
  private final ProductPricingService productPricingService;

  public static enum ProductTypes {
    BRF, RENT, RENT_BRF, SUBLET_RENTAL, SUBLET_BRF, STUDENT_BRF, STUDENT_RENT, LODGER
  };

  public static final String emoji_smile = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81}, Charset.forName("UTF-8"));
  public static final String emoji_hand_ok = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x91, (byte) 0x8C}, Charset.forName("UTF-8"));
  public static final String emoji_closed_lock_with_key = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x94, (byte) 0x90}, Charset.forName("UTF-8"));
  public static final String emoji_postal_horn = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0xAF}, Charset.forName("UTF-8"));
  public static final String emoji_school_satchel = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x8E, (byte) 0x92}, Charset.forName("UTF-8"));
  public static final String emoji_mag = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x94, (byte) 0x8D}, Charset.forName("UTF-8"));
  public static final String emoji_revlolving_hearts = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x92, (byte) 0x9E}, Charset.forName("UTF-8"));
  public static final String emoji_tada = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x8E, (byte) 0x89}, Charset.forName("UTF-8"));
  public static final String emoji_thumbs_up = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x91, (byte) 0x8D}, Charset.forName("UTF-8"));
  public static final String emoji_hug = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0xA4, (byte) 0x97}, Charset.forName("UTF-8"));
  public static final String emoji_waving_hand = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x91, (byte) 0x8B}, Charset.forName("UTF-8"));
  public static final String emoji_flushed_face = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0xB3}, Charset.forName("UTF-8"));
  public static final String emoji_thinking = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0xA4, (byte) 0x94}, Charset.forName("UTF-8"));
  public static final String emoji_sauropod = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0xA6, (byte) 0x95}, Charset.forName("UTF-8"));
  public static final String emoji_folded_hands = new String(
      new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x99, (byte) 0x8F}, Charset.forName("UTF-8"));

  private final SignupCodeRepository signupRepo;
  private final ConversationFactory conversationFactory;

  Integer queuePos;

  @Autowired
  public OnboardingConversationDevi(MemberService memberService,
      ProductPricingService productPricingClient, SignupCodeRepository signupRepo,
      ApplicationEventPublisher eventPublisher, ConversationFactory conversationFactory) {
    super();
    this.memberService = memberService;
    this.productPricingService = productPricingClient;
    this.signupRepo = signupRepo;
    this.eventPublisher = eventPublisher;
    this.conversationFactory = conversationFactory;

    createChatMessage(MESSAGE_WAITLIST_START, new MessageBodySingleSelect("Hej! Jag heter Hedvig "
        + emoji_waving_hand
        + "\fDet här går på nolltid! Jag ställer några frågor, sen ger jag dig ett förslag på en hemförsäkring",
        Lists.newArrayList(new SelectOption("Låter bra!", MESSAGE_FORSLAGSTART),
            new SelectOption("Jag är redan medlem", "message.bankid.start"))));

    createChatMessage("message.membernotfound",
        new MessageBodySingleSelect("Hmm, det verkar som att du inte är medlem här hos mig ännu"
            + "\fMen jag tar gärna fram ett försäkringsförslag till dig, det är precis som allt annat med mig superenkelt",
            Lists.newArrayList(new SelectOption("Låter bra!", MESSAGE_FORSLAGSTART))));

    createMessage(MESSAGE_SIGNUP_TO_WAITLIST,
        new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Det ordnar jag! Vad är din mailadress?"));
    setExpectedReturnType(MESSAGE_SIGNUP_TO_WAITLIST, new EmailAdress());

    createMessage("message.signup.email", new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Det ordnar jag! Vad är din mailadress?"));
    setExpectedReturnType("message.signup.email", new EmailAdress());

    createChatMessage("message.signup.checkposition",
        new MessageBodySingleSelect(
            "Tack!" + "\fJag hör av mig till dig snart, ha det fint så länge! ✌️",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Jag har fått ett aktiveringsmail",
                    MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
              }
            }));

    createChatMessage(MESSAGE_SIGNUP_NOT_ACTIVATED_YET,
        new MessageBodySingleSelect(
            "Hmm, det verkar inte som att du är aktiverad än 👀"
                + "\fTitta in igen när du fått aktiveringsmailet" + "\fJag hör av mig snart!",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Jag har fått ett aktiveringsmail",
                    MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
              }
            }));

    createChatMessage(MESSAGE_SIGNUP_NOT_REGISTERED_YET,
        new MessageBodySingleSelect(
            "Det ser inte ut som att du har skrivit upp dig på väntelistan än"
                + "\fMen nu har jag din mailadress, så jag lägger till den!" + "\fVi hörs snart!",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Jag har fått ett aktiveringsmail",
                    MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
              }
            }));

    createMessage("message.notmember", new MessageBodyParagraph(
        "Okej! Då tar jag fram ett försäkringsförslag till dig på nolltid"));
    addRelay("message.notmember", "message.notmember.start");

    createMessage("message.notmember.start", new MessageBodyParagraph(
        "Jag ställer några snabba frågor så att jag kan räkna ut ditt pris"));
    addRelay("message.notmember", MESSAGE_FORSLAGSTART);

    // Deprecated
    createChatMessage("message.waitlist.user.alreadyactive", new MessageBodyText("Grattis! "
        + emoji_tada
        + " Nu kan du bli medlem hos Hedvig\fKolla din mail, där ska du ha fått en aktiveringkod som du ska ange här\fVi ses snart! "
        + emoji_smile));

    // Deprecated
    createChatMessage("message.activate.code.used", new MessageBodySingleSelect(
        "Det verkar som koden redan är använd... \fHar du aktiverat koden på en annan enhet så kan du logga in direkt med bankId.",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag är redan medlem och vill logga in", "message.medlem"));
          }
        }));

    // Deprecated
    createMessage("message.signup.flerval",
        new MessageBodySingleSelect("", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Kolla min plats på väntelistan", "message.signup.checkposition"));
            add(new SelectOption("Jag har fått ett aktiveringsmail",
                MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
          }
        }));

    createMessage(MESSAGE_WAITLIST_NOT_ACTIVATED,
        new MessageBodySingleSelect(
            "Du verkar redan stå på väntelistan. Din plats är {SIGNUP_POSITION}!",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Kolla min plats på väntelistan",
                    "message.signup.checkposition"));
                add(new SelectOption("Jag har fått ett aktiveringsmail",
                    MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
              }
            }));

    // Deprecated
    createMessage("message.activate.nocode", new MessageBodySingleSelect(
        "Jag känner inte igen den koden tyvärr " + emoji_thinking, new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Kolla min plats på väntelistan", "message.signup.checkposition"));
            add(new SelectOption("Jag har fått ett aktiveringsmail",
                MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST));
          }
        }));

    createMessage(MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST,
        new MessageBodyText("Kul! Skriv in din mailadress här"));
    setExpectedReturnType(MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST, new EmailAdress());

    createMessage("message.activate.ok.a", new MessageBodyParagraph("Välkommen!"), 1000);
    addRelay("message.activate.ok.a", "message.activate.ok.b");

    createMessage("message.activate.ok.b",
        new MessageBodyParagraph("Nu ska jag ta fram ett försäkringsförslag åt dig"), 2000);
    addRelay("message.activate.ok.b", MESSAGE_FORSLAGSTART);

    createMessage("message.uwlimit.tack", new MessageBodySingleSelect(
        "Tack! Jag hör av mig så fort jag kan", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag vill starta om chatten", "message.activate.ok.a"));
          }
        }));

    createMessage("message.audiotest",
        new MessageBodyAudio("Här kan du testa audio", "/claims/fileupload"), 2000);
    createMessage("message.phototest",
        new MessageBodyPhotoUpload("Här kan du testa fotouppladdaren", "/asset/fileupload"), 2000);
    createMessage("message.fileupload.result",
        new MessageBodySingleSelect("Ok uppladdningen gick bra!", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Hem", MESSAGE_WAITLIST_START));
          }
        }));

    createMessage("message.medlem",
        new MessageBodySingleSelect(
            "Välkommen tillbaka " + emoji_hug
                + "\n\n Logga in med BankID så är du inne i appen igen",
            new ArrayList<SelectItem>() {
              {
                add(new SelectLink("Logga in", "message.bankid.autostart.respond", null,
                    "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null,
                    false));

                // add(new SelectOption("Logga in", "message.bankidja"));
              }
            }),
        (m, uc) -> {
          UserData obd = uc.getOnBoardingData();
          if (m.getSelectedItem().value.equals("message.bankid.autostart.respond")) {
            uc.putUserData(LOGIN, "true");
            obd.setBankIdMessage("message.medlem");
          }

          return "";
        });
    setupBankidErrorHandlers("message.medlem");

    // Deprecated
    createMessage(MESSAGE_PRE_FORSLAGSTART, new MessageBodyParagraph(
        "Toppen! Då ställer jag några frågor så att jag kan räkna ut ditt pris"), 1500);
    addRelay(MESSAGE_PRE_FORSLAGSTART, MESSAGE_FORSLAGSTART);

    createMessage(MESSAGE_FORSLAGSTART, new MessageBodySingleSelect(
        "Första frågan! Bor du i lägenhet eller eget hus?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Lägenhet", MESSAGE_LAGENHET_PRE));
            add(new SelectOption("Hus", MESSAGE_HUS));
          }
        }), "h_to_house");

    createMessage(MESSAGE_LAGENHET_PRE, new MessageBodyParagraph(emoji_hand_ok));
    addRelay(MESSAGE_LAGENHET_PRE, MESSAGE_LAGENHET);

    createMessage(MESSAGE_LAGENHET, new MessageBodySingleSelect(
        "Har du BankID? I så fall kan vi hoppa över några frågor så du får se ditt prisförslag snabbare!",
        new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Fortsätt med BankID", "message.bankid.autostart.respond", null,
                "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null, false));
            add(new SelectOption("Fortsätt utan", "message.manuellnamn"));
          }
        }), (m, uc) -> {
          UserData obd = uc.getOnBoardingData();
          if (m.getSelectedItem().value.equals("message.bankid.autostart.respond")) {
            obd.setBankIdMessage(MESSAGE_LAGENHET);
          }

          return "";
        });

    setupBankidErrorHandlers(MESSAGE_LAGENHET);

    createMessage("message.missing.bisnode.data",
        new MessageBodyParagraph("Jag hittade tyvärr inte dina uppgifter. Men..."));
    addRelay("message.missing.bisnode.data", "message.manuellnamn");

    createMessage(MESSAGE_START_LOGIN, new MessageBodyParagraph("Välkommen tillbaka! " + emoji_hug),
        1500);
    addRelay(MESSAGE_START_LOGIN, "message.bankid.start");

    createMessage("message.bankid.start", new MessageBodySingleSelect(
        "Bara att logga in så ser du din försäkring", new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Logga in med BankID", "message.bankid.autostart.respond", null,
                "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null, false));
            add(new SelectOption("Jag är inte medlem", "message.notmember"));
          }
        }), (m, uc) -> {
          UserData obd = uc.getOnBoardingData();
          if (m.getSelectedItem().value.equals("message.bankid.autostart.respond")) {
            obd.setBankIdMessage("message.bankid.start");
          } else if (m.getSelectedItem().value.equals(MESSAGE_WAITLIST_START)) {
            uc.putUserData(LOGIN, "false");
          }

          return "";
        });

    setupBankidErrorHandlers("message.bankid.start");

    createMessage("message.bankid.start.manual", new MessageBodyNumber(
        "Om du anger ditt personnumer så får du använda bankId på din andra enhet" + emoji_smile));

    createMessage("message.bankid.error",
        new MessageBodyParagraph("Hmm, det verkar inte som att ditt BankID svarar. Testa igen!"),
        1500);

    createMessage("message.bankid.start.manual.error",
        new MessageBodyParagraph("Hmm, det verkar inte som att ditt BankID svarar. Testa igen!"));
    addRelay("message.bankid.start.manual.error", "message.bankid.start.manual");

    createMessage("message.bankid.autostart.respond",
        new MessageBodyBankIdCollect("{REFERENCE_TOKEN}"));

    createChatMessage(MESSAGE_HUS,
        new MessageBodySingleSelect("Åh, typiskt! Just nu försäkrar jag bara lägenheter\f"
            + "Om du vill ge mig din mailadress så kan jag höra av mig när jag försäkrar annat också",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Okej!", MESSAGE_NYHETSBREV));
                add(new SelectOption("Tack, men nej tack", "message.avslutok"));
              }
            }));

    createMessage(MESSAGE_NYHETSBREV, new MessageBodyText("Topp! Vad är mailadressen?"));
    setExpectedReturnType(MESSAGE_NYHETSBREV, new EmailAdress());
    createMessage(MESSAGE_TIPSA, new MessageBodyText(
        "Kanon! Fyll i mailadressen till den du vill att jag ska skicka ett tipsmail till"));
    setExpectedReturnType(MESSAGE_TIPSA, new EmailAdress());
    createMessage(MESSAGE_FRIFRAGA, new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Fråga på!"));

    createMessage(MESSAGE_FRIONBOARDINGFRAGA,
        new MessageHeader(Conversation.HEDVIG_USER_ID, -1, true),
        new MessageBodyText("Fråga på! "));

    createMessage(MESSAGE_NAGOTMER,
        new MessageBodySingleSelect(
            "Tack! Vill du hitta på något mer nu när vi har varandra på tråden?",
            new ArrayList<SelectItem>() {
              {
                // add(new SelectOption("Jag vill tipsa någon om dig",
                // MESSAGE_TIPSA));
                add(new SelectOption("Jag har en fråga", MESSAGE_FRIONBOARDINGFRAGA));
                add(new SelectOption("Nej tack!", MESSAGE_AVSLUTOK));
              }
            }));

    createMessage(MESSAGE_BANKIDJA, new MessageBodySingleSelect(
        "Tack {NAME}! Stämmer det att du bor på {ADDRESS}?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Ja", MESSAGE_KVADRAT));
            add(new SelectOption("Nej", MESSAGE_VARBORDUFELADRESS));
          }
        }));

    createMessage("message.bankidja.noaddress",
        new MessageBodyText("Tack {NAME}! Nu skulle jag behöva veta vilken gatuadress bor du på?"));

    createMessage(MESSAGE_VARBORDUFELADRESS,
        new MessageBodyText("Inga problem! Vilken gatuadress bor du på?"));
    createMessage("message.varbordufelpostnr",
        new MessageBodyNumber("Och vad har du för postnummer?"));
    setExpectedReturnType("message.varbordufelpostnr", new ZipCodeSweden());

    createMessage(MESSAGE_KVADRAT,
        new MessageBodyNumber("Hur många kvadratmeter är lägenheten?"));
    setExpectedReturnType(MESSAGE_KVADRAT, new LivingSpaceSquareMeters());

    createChatMessage("message.manuellnamn", new MessageBodyText(
        "Inga problem! Då ställer jag bara några extra frågor nu\fMen om du vill bli medlem sen så måste du signera med BankID, bara så du vet!\fVad heter du i förnamn?"));

    createMessage("message.manuellfamilyname",
        new MessageBodyText("Kul att ha dig här {NAME}! Vad heter du i efternamn?"));

    createMessage("message.manuellpersonnr",
        new MessageBodyNumber("Tack! Vad är ditt personnummer? (12 siffror)"));
    setExpectedReturnType("message.manuellpersonnr", new SSNSweden());
    createMessage("message.varborduadress", new MessageBodyText("Vilken gatuadress bor du på?"));
    createMessage("message.varbordupostnr", new MessageBodyNumber("Vad är ditt postnummer?"));
    setExpectedReturnType("message.varbordupostnr", new ZipCodeSweden());


    createMessage("message.lghtyp", new MessageBodySingleSelect(
        "Hoppas du trivs! Hyr du eller äger du den?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag hyr den", ProductTypes.RENT.toString()));
            add(new SelectOption("Jag äger den", ProductTypes.BRF.toString()));
          }
        }));

    createMessage("message.lghtyp.sublet",
        new MessageBodySingleSelect(
            "Okej! Är lägenheten du hyr i andra hand en hyresrätt eller bostadsrätt?",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Hyresrätt", ProductTypes.SUBLET_RENTAL.toString()));
                add(new SelectOption("Bostadsrätt", ProductTypes.SUBLET_BRF.toString()));
              }
            }));

    createMessage("message.pers", new MessageBodyNumber("Okej! Hur många bor i lägenheten?"));
    setExpectedReturnType("message.pers", new HouseholdMemberNumber());

    createMessage(MESSAGE_SAKERHET,
        new MessageBodyMultipleSelect("Finns någon av de här säkerhetsgrejerna i lägenheten?",
            Lists.newArrayList(new SelectOption("Brandvarnare", "safety.alarm"),
                new SelectOption("Brandsläckare", "safety.extinguisher"),
                new SelectOption("Säkerhetsdörr", "safety.door"),
                new SelectOption("Gallergrind", "safety.gate"),
                new SelectOption("Inbrottslarm", "safety.burglaralarm"),
                new SelectOption("Inget av dessa", "safety.none", false, true))));

    createMessage(MESSAGE_PHONENUMBER,
        new MessageBodyNumber("Nu är vi snart klara! Vad är ditt telefonnummer?"));
    setExpectedReturnType(MESSAGE_PHONENUMBER, new TextInput());

    // ---------- Move to after sign.
    createMessage(MESSAGE_EMAIL, new MessageBodyText(
        "Nu behöver jag bara din mailadress så att jag kan skicka en bekräftelse"));
    setExpectedReturnType(MESSAGE_EMAIL, new EmailAdress());

    createMessage(MESSAGE_FORSAKRINGIDAG, new MessageBodySingleSelect(
        "Tackar! Har du någon hemförsäkring idag?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Ja", MESSAGE_FORSAKRINGIDAGJA));
            add(new SelectOption("Nej", MESSAGE_FORSLAG));
          }
        }));

    createMessage(MESSAGE_FORSAKRINGIDAGJA, new MessageBodySingleSelect(
        "Okej! Vilket försäkringsbolag har du?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("If", "If"));
            add(new SelectOption("Folksam", "Folksam"));
            add(new SelectOption("Trygg-Hansa", "Trygg-Hansa"));
            add(new SelectOption("Länsförsäkringar", "Länsförsäkringar"));
            // add(new SelectOption("Moderna", "Moderna"));
            add(new SelectOption("Annat bolag", "message.bolag.annat.expand"));
            add(new SelectOption("Ingen aning", "message.bolag.vetej"));
          }
        }));

    createMessage("message.bolag.annat.expand", new MessageBodySingleSelect(
        "Okej! Är det något av dessa kanske?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Moderna", "Moderna"));
            add(new SelectOption("ICA", "ICA"));
            add(new SelectOption("Gjensidige", "Gjensidige"));
            add(new SelectOption("Vardia", "Vardia"));
            add(new SelectOption("Annat bolag", MESSAGE_ANNATBOLAG));
          }
        }));

    createMessage("message.bolag.vetej",
        new MessageBodyParagraph("Inga problem, det kan vi ta senare"));
    addRelay("message.bolag.vetej", MESSAGE_FORSLAG);

    createMessage(MESSAGE_ANNATBOLAG, new MessageBodyText("Okej, vilket försäkringsbolag har du?"),
        2000);

    createChatMessage(MESSAGE_BYTESINFO, new MessageBodySingleSelect(
        "Ja, ibland är det dags att prova något nytt. De kommer nog förstå\f"
            + "Om du blir medlem hos mig sköter jag bytet åt dig. Så när din gamla försäkring går ut, flyttas du automatiskt till din nya hos mig",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag förstår", MESSAGE_FORSLAG)); // Create product
            add(new SelectOption("Förklara mer", "message.bytesinfo3"));
          }
        }));

    createChatMessage("message.bytesinfo3", new MessageBodySingleSelect("Självklart!\f"
        + "Oftast har du ett tag kvar på bindningstiden på din gamla försäkring\f"
        + "Om du väljer att byta till Hedvig så hör jag av mig till ditt försäkringsbolag och meddelar att du vill byta försäkring så fort bindningstiden går ut\f"
        + "Till det behöver jag en fullmakt från dig som du skriver under med mobilt BankID \f"
        + "Sen börjar din nya försäkring gälla direkt när den gamla går ut\f"
        + "Så du behöver aldrig vara orolig att gå utan försäkring efter att du skrivit på med mig",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Okej!", MESSAGE_FORSLAG)); // Create product
          }
        }));

    createChatMessage(MESSAGE_50K_LIMIT, new MessageBodySingleSelect(
        "Okej! Dina prylar som du har hemma skyddas upp till en miljon kr 🏺🖼️\f"
            + "Men äger du något som du tar med dig utanför hemmet som är värt över 50 000 kr? 💍⌚",
        Lists.newArrayList(new SelectOption("Ja", MESSAGE_50K_LIMIT_YES),
            new SelectOption("Nej", MESSAGE_50K_LIMIT_NO))));

    createChatMessage(MESSAGE_50K_LIMIT_YES, new MessageBodySingleSelect(
        "Okej!\fOm du skaffar Hedvig är det enkelt att lägga till en separat objektsförsäkring efteråt",
        Lists.newArrayList(new SelectOption("Jag förstår!", MESSAGE_50K_LIMIT_YES_YES))));

    createMessage(MESSAGE_50K_LIMIT_YES_YES, new MessageBodyParagraph("Det fixar jag!"), 1500);
    addRelay(MESSAGE_50K_LIMIT_YES_YES, MESSAGE_PHONENUMBER);

    createMessage(MESSAGE_50K_LIMIT_YES_NO,
        new MessageBodyParagraph("Då skippar jag det " + emoji_thumbs_up), 2000);
    addRelay(MESSAGE_50K_LIMIT_YES_NO, MESSAGE_PHONENUMBER);

    createMessage(MESSAGE_50K_LIMIT_NO,
        new MessageBodyParagraph("Vad bra! Då täcks dina prylar av drulleförsäkringen"), 2000);

    createMessage(MESSAGE_50K_LIMIT_NO_1, new MessageBodyParagraph(
        "Köper du någon dyr pryl i framtiden så fixar jag så klart det också!"), 2000);

    addRelay(MESSAGE_50K_LIMIT_NO, MESSAGE_50K_LIMIT_NO_1);

    addRelay(MESSAGE_50K_LIMIT_NO_1, MESSAGE_PHONENUMBER);

    createMessage(MESSAGE_FORSLAG,
        new MessageBodyParagraph("Sådär, det var all info jag behövde. Tack!"), 2000);

    createMessage(MESSAGE_FORSLAG2,
        new MessageBodySingleSelect("Nu går vi igenom ditt förslag!", Lists.newArrayList(
            SelectLink.toOffer("Okej " + emoji_thumbs_up, "message.forslag.dashboard"))));
    addRelay(MESSAGE_FORSLAG, MESSAGE_FORSLAG2);

    createChatMessage("message.tryggt", new MessageBodySingleSelect(""
        + "Självklart!\fHedvig är backat av en av världens största försäkringsbolag, så att du kan känna dig trygg i alla lägen\fDe är där för mig, så jag alltid kan vara där för dig\fJag är självklart också auktoriserad av Finansinspektionen "
        + emoji_mag, new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Visa förslaget igen", "message.forslag.dashboard", "Offer", null,
                null, false));
            add(new SelectOption("Jag har en annan fråga", "message.quote.close"));
          }
        }));

    createChatMessage("message.skydd", new MessageBodySingleSelect(""
        + "Såklart! Med mig har du samma grundskydd som en vanlig hemförsäkring\fUtöver det ingår alltid drulle, alltså till exempel om du tappar din telefon i golvet och den går sönder, och ett bra reseskydd",
        new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Visa förslaget igen", "message.forslag.dashboard", "Offer", null,
                null, false));
            add(new SelectOption("Jag har en annan fråga", "message.quote.close"));
            // add(new SelectOption("Jag vill bli medlem", "message.forslag"));
          }
        }));

    createMessage("message.frionboardingfragatack",
        new MessageBodySingleSelect("Tack! Jag hör av mig inom kort", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag har fler frågor", MESSAGE_FRIONBOARDINGFRAGA));
          }
        }));

    createMessage("message.frifragatack",
        new MessageBodySingleSelect("Tack! Jag hör av mig inom kort", new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Visa förslaget igen", "message.forslag.dashboard", "Offer", null,
                null, false));
            add(new SelectOption("Jag har fler frågor", MESSAGE_FRIFRAGA));
          }
        }));

    createChatMessage("message.uwlimit.housingsize", new MessageBodyText(
        "Det var stort! För att kunna försäkra så stora lägenheter behöver vi ta några grejer över telefon\fVad är ditt nummer?"));

    createChatMessage("message.uwlimit.householdsize", new MessageBodyText(
        "Okej! För att kunna försäkra så många i samma lägenhet behöver vi ta några grejer över telefon\fVad är ditt nummer?"));

    createChatMessage("message.pris", new MessageBodySingleSelect(
        "Det är knepigt att jämföra försäkringspriser, för alla försäkringar är lite olika.\fMen grundskyddet jag ger är väldigt brett utan att du behöver betala för krångliga tillägg\fSom Hedvigmedlem gör du dessutom skillnad för världen runtomkring dig, vilket du garanterat inte gör genom din gamla försäkring!",
        new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Visa förslaget igen", "message.forslag.dashboard", "Offer", null,
                null, false));
            add(new SelectOption("Jag har fler frågor", "message.quote.close"));
          }
        }));

    createMessage("message.mail", new MessageBodyText(
        "Tackar.\nOch din mailadress så jag kan skicka en bekräftelse när vi skrivit på?"));

    // (FUNKTION: FYLL I MAILADRESS) = FÄLT
    setExpectedReturnType("message.mail", new EmailAdress());

    createMessage("message.bankid.error.expiredTransaction",
        new MessageBodyParagraph(BankIDStrings.expiredTransactionError), 1500);

    createMessage("message.bankid.error.certificateError",
        new MessageBodyParagraph(BankIDStrings.certificateError), 1500);

    createMessage("message.bankid.error.userCancel",
        new MessageBodyParagraph(BankIDStrings.userCancel), 1500);

    createMessage("message.bankid.error.cancelled",
        new MessageBodyParagraph(BankIDStrings.cancelled), 1500);

    createMessage("message.bankid.error.startFailed",
        new MessageBodyParagraph(BankIDStrings.startFailed), 1500);

    createMessage("message.kontrakt.great", new MessageBodyParagraph("Härligt!"), 1000);
    addRelay("message.kontrakt.great", "message.kontrakt");

    createMessage("message.kontrakt.signError",
        new MessageBodyParagraph("Hmm nu blev något fel! Vi försöker igen " + emoji_flushed_face),
        1000);
    addRelay("message.kontrakt.signError", "message.kontrakt");

    createMessage("message.kontrakt.signProcessError",
        new MessageBodyParagraph("Vi försöker igen " + emoji_flushed_face), 1000);
    addRelay("message.kontrakt.signProcessError", "message.kontrakt");

    createMessage("message.kontrakt", new MessageBodySingleSelect(
        "Då är det bara att signera, sen är vi klara", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Okej!", "message.kontraktpop.startBankId"));
            // add(new SelectLink("Läs igenom", "message.kontrakt", null, null,
            // gatewayUrl + "/insurance/contract/{PRODUCT_ID}", false));
          }
        }), (m, userContext) -> {
          if (m.getSelectedItem().value.equals("message.kontrakt")) {
            m.text = m.getSelectedItem().text;
            return m.getSelectedItem().value;
          } else {
            UserData ud = userContext.getOnBoardingData();

            Optional<BankIdSignResponse> signData;

            String signText;
            if (ud.getCurrentInsurer() != null) {
              signText =
                  "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag vill byta till Hedvig när min gamla försäkring går ut. Jag ger också Hedvig fullmakt att byta försäkringen åt mig.";
            } else {
              signText =
                  "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag skaffar en försäkring hos Hedvig.";
            }

            signData = memberService.sign(ud.getSSN(), signText, userContext.getMemberId());

            if (signData.isPresent()) {
              userContext.startBankIdSign(signData.get());
            } else {
              log.error("Could not start signing process.");
              return "message.kontrakt.signError";
            }
            return "";
          }
        });

    createMessage("message.kontraktpop.bankid.collect",
        new MessageBodyBankIdCollect("{REFERENCE_TOKEN}"));

    createMessage("message.kontraktpop.startBankId", new MessageBodySingleSelect(
        "För signeringen använder vi BankID", new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Öppna BankID", "message.kontraktpop.bankid.collect", null,
                "bankid:///?autostarttoken={AUTOSTART_TOKEN}&redirect={LINK_URI}", null, false));
          }
        }), (m, uc) -> {
          UserData obd = uc.getOnBoardingData();
          if (m.getSelectedItem().value.equals("message.kontraktpop.bankid.collect")) {
            obd.setBankIdMessage("message.kontraktpop.startBankId");
          }

          return "";
        });

    setupBankidErrorHandlers("message.kontraktpop.startBankId", "message.kontrakt");

    createMessage("message.kontraktklar",
        new MessageBodyParagraph("Hurra! " + emoji_tada + " Välkommen som medlem!"));
    addRelay("message.kontraktklar", MESSAGE_EMAIL);

    /*
     * createChatMessage("message.kontraktklar", new MessageBodySingleSelect("Hurra! "+ emoji_tada +
     * " Välkommen som medlem!"+
     * "\fJag skickar en bekräftelse till din mail. Visst stämmer det att du har " + EMAIL + "?",
     * new ArrayList<SelectItem>() {{ add(new SelectOption("Ja", "message.onboarding.end")); add(new
     * SelectOption("Nej", "message.kontrakt.email")); }} ));
     */

    createMessage("message.kontrakt.email", new MessageBodyText("OK! Vad är din mailadress?"));
    setExpectedReturnType("message.kontrakt.email", new EmailAdress());

    createMessage("message.avslutvalkommen", new MessageBodySingleSelect(
        "Hej så länge och ännu en gång, varmt välkommen!", new ArrayList<SelectItem>() {
          {
            add(new SelectLink("Nu utforskar jag", "onboarding.done", "Dashboard", null, null,
                false));
          }
        }));

    createMessage("message.avslutok",
        new MessageBodySingleSelect(
            "Okej! Trevligt att chattas, ha det fint och hoppas vi hörs igen!",
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Jag vill starta om chatten", MESSAGE_WAITLIST_START));
              }
            }));

    createChatMessage("message.quote.close", new MessageBodySingleSelect(
        "Du kanske undrade över något" + "\fNågot av det här kanske?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Är Hedvig tryggt?", "message.tryggt"));
            add(new SelectOption("Ger Hedvig ett bra skydd?", "message.skydd"));
            add(new SelectOption("Är Hedvig prisvärt?", "message.pris"));
            add(new SelectOption("Jag har en annan fråga", MESSAGE_FRIFRAGA));
            add(new SelectLink("Visa förslaget igen", "message.forslag.dashboard", "Offer", null,
                null, false));
          }
        }));

    createMessage("message.bikedone", new MessageBodyText("Nu har du sett hur det funkar..."));

    createMessage("error", new MessageBodyText("Oj nu blev något fel..."));

    // Student policy-related messages
    createMessage("message.student",
        new MessageBodySingleSelect(
            "Okej! Jag ser att du är under 30. Är du kanske student? " + emoji_school_satchel,
            new ArrayList<SelectItem>() {
              {
                add(new SelectOption("Ja", "message.studentja"));
                add(new SelectOption("Nej", "message.studentnej"));
              }
            }));

    createMessage("message.studentnej", new MessageBodyParagraph("Okej, då vet jag"));
    addRelay("message.studentnej", MESSAGE_KVADRAT);

    createMessage("message.studentja", new MessageBodySingleSelect(
        "Vad kul! Jag har tagit fram ett extra grymt erbjudande som är skräddarsytt för studenter som bor max två personer på max 50 kvm ‍🎓",
        new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Okej, toppen!", MESSAGE_KVADRAT));
          }
        }));

    createChatMessage(MESSAGE_STUDENT_LIMIT_LIVING_SPACE, new MessageBodySingleSelect(
        "Okej! För så stora lägenheter (över 50 kvm) gäller dessvärre inte studentförsäkringen\f"
            + "Men inga problem, du får den vanliga hemförsäkringen som ger ett bredare skydd och jag fixar ett grymt pris till dig ändå! 🙌",
        Lists.newArrayList(
            new SelectOption("Okej, jag förstår", MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE))));

    createMessage(MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE, new MessageBodySingleSelect(
        "Hyr du eller äger du lägenheten?", new ArrayList<SelectItem>() {
          {
            add(new SelectOption("Jag hyr den", ProductTypes.RENT.toString()));
            add(new SelectOption("Jag äger den", ProductTypes.BRF.toString()));
          }
        }));

    createChatMessage(MESSAGE_STUDENT_LIMIT_PERSONS, new MessageBodySingleSelect(
        "Okej! För så många personer (fler än 2) gäller dessvärre inte studentförsäkringen\f"
            + "Men inga problem, du får den vanliga hemförsäkringen som ger ett bredare skydd och jag fixar ett grymt pris till dig ändå! 🙌",
        Lists.newArrayList(new SelectOption("Okej, jag förstår", MESSAGE_SAKERHET))));

    createMessage(MESSAGE_STUDENT_ELIGIBLE_BRF, new MessageBodySingleSelect(
        "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 99 kr per månad! 🙌",
        Lists.newArrayList(new SelectOption("Okej, nice!", MESSAGE_SAKERHET))));

    createMessage(MESSAGE_STUDENT_ELIGIBLE_RENT, new MessageBodySingleSelect(
        "Grymt! Då får du vår fantastiska studentförsäkring där drulle ingår och betalar bara 79 kr per månad! 🙌",
        Lists.newArrayList(new SelectOption("Okej, nice!", MESSAGE_SAKERHET))));

    createChatMessage(MESSAGE_STUDENT_25K_LIMIT, new MessageBodySingleSelect(
        "Okej! Dina prylar som du har hemma skyddas upp till 200 000 kr 🏺🖼️\f"
            + "Men äger du något som du tar med dig utanför hemmet som är värt över 25 000 kr? 💍⌚",
        Lists.newArrayList(new SelectOption("Ja", MESSAGE_50K_LIMIT_YES),
            new SelectOption("Nej", MESSAGE_50K_LIMIT_NO))));
  }

  private void setupBankidErrorHandlers(String messageId) {
    setupBankidErrorHandlers(messageId, null);
  }

  private void setupBankidErrorHandlers(String messageId, String relayId) {

    if (relayId == null) {
      relayId = messageId;
    }

    createMessage(messageId + ".bankid.error.expiredTransaction",
        new MessageBodyParagraph(BankIDStrings.expiredTransactionError), 1500);
    addRelay(messageId + ".bankid.error.expiredTransaction", relayId);

    createMessage(messageId + ".bankid.error.certificateError",
        new MessageBodyParagraph(BankIDStrings.certificateError), 1500);
    addRelay(messageId + ".bankid.error.certificateError", relayId);

    createMessage(messageId + ".bankid.error.userCancel",
        new MessageBodyParagraph(BankIDStrings.userCancel), 1500);
    addRelay(messageId + ".bankid.error.userCancel", relayId);

    createMessage(messageId + ".bankid.error.cancelled",
        new MessageBodyParagraph(BankIDStrings.cancelled), 1500);
    addRelay(messageId + ".bankid.error.cancelled", relayId);

    createMessage(messageId + ".bankid.error.startFailed",
        new MessageBodyParagraph(BankIDStrings.startFailed), 1500);
    addRelay(messageId + ".bankid.error.startFailed", relayId);

    createMessage(messageId + ".bankid.error.invalidParameters",
        new MessageBodyParagraph(BankIDStrings.userCancel), 1500);
    addRelay(messageId + ".bankid.error.invalidParameters", relayId);
  }

  @Override
  public void init(UserContext userContext) {
    log.info("Starting onboarding conversation");
    if (userContext.getDataEntry("{SIGNED_UP}") == null) {
      startConversation(userContext, MESSAGE_WAITLIST_START); // Id of first message
    } else {
      startConversation(userContext, "message.activate.ok.b"); // Id of first message
    }
  }

  @Override
  public void init(UserContext userContext, String startMessage) {
    log.info("Starting onboarding conversation with message: " + startMessage);
    if (startMessage.equals(MESSAGE_START_LOGIN)) {
      userContext.putUserData(LOGIN, "true");
    }
    startConversation(userContext, startMessage); // Id of first message
  }
  // --------------------------------------------------------------------------- //

  public int getValue(MessageBodyNumber body) {
    return Integer.parseInt(body.text);
  }

  public String getValue(MessageBodySingleSelect body) {

    for (SelectItem o : body.choices) {
      if (SelectOption.class.isInstance(o) && SelectOption.class.cast(o).selected) {
        return SelectOption.class.cast(o).value;
      }
    }
    return "";
  }

  public ArrayList<String> getValue(MessageBodyMultipleSelect body) {
    ArrayList<String> selectedOptions = new ArrayList<>();
    for (SelectItem o : body.choices) {
      if (SelectOption.class.isInstance(o) && SelectOption.class.cast(o).selected) {
        selectedOptions.add(SelectOption.class.cast(o).value);
      }
    }
    return selectedOptions;
  }

  // ------------------------------------------------------------------------------- //
  @Override
  public void receiveEvent(EventTypes e, String value, UserContext userContext) {
    switch (e) {
      // This is used to let Hedvig say multiple message after another
      case MESSAGE_FETCHED:
        log.info("Message fetched: " + value);

        // New way of handeling relay messages
        String relay = getRelay(value);
        if (relay != null) {
          completeRequest(relay, userContext);
        }
        if (value.equals(MESSAGE_FORSLAG)) {
          completeOnboarding(userContext);
        }
        break;
      case ANIMATION_COMPLETE:
        switch (value) {
          case "animation.bike":
            completeRequest("message.bikedone", userContext);
            break;
        }
        break;
      case MODAL_CLOSED:
        switch (value) {
          case "quote":
            completeRequest("message.quote.close", userContext);
            break;
        }
        break;
      case MISSING_DATA:
        switch (value) {
          case "bisnode":
            completeRequest("message.missing.bisnode.data", userContext);
            break;
        }
        break;
    }
  }

  private void completeOnboarding(UserContext userContext) {
    String productId = this.productPricingService.createProduct(userContext.getMemberId(),
        userContext.getOnBoardingData());
    userContext.getOnBoardingData().setProductId(productId);
    this.memberService.finalizeOnBoarding(userContext.getMemberId(),
        userContext.getOnBoardingData());
  }

  @Override
  public void receiveMessage(UserContext userContext, Message m) {

    String nxtMsg = "";

    if (!validateReturnType(m, userContext)) {
      return;
    }

    // Lambda
    if (this.hasSelectItemCallback(m.id)
        && m.body.getClass().equals(MessageBodySingleSelect.class)) {
      // MessageBodySingleSelect body = (MessageBodySingleSelect) m.body;
      nxtMsg = this.execSelectItemCallback(m.id, (MessageBodySingleSelect) m.body, userContext);
      addToChat(m, userContext);
    }

    UserData onBoardingData = userContext.getOnBoardingData();

    String selectedOption = (m.body.getClass().equals(MessageBodySingleSelect.class))
        ? getValue((MessageBodySingleSelect) m.body)
        : null;

    if (selectedOption != null) { // TODO: Think this over
      // Check the selected option first...
      switch (selectedOption) {
        case "message.signup.checkposition":
          log.info("Checking position...");
          // We do not have the users email
          if (!(onBoardingData.getEmail() != null && !onBoardingData.getEmail().equals(""))) {
            nxtMsg = "message.signup.email";
          } else { // Update position if there is a code
            userContext.putUserData("{SIGNUP_POSITION}",
                Objects.toString(getSignupQueuePosition(onBoardingData.getEmail())));
          }
          break;
      }
    }

    // ... and then the incomming message id
    switch (m.getBaseMessageId()) {
      case MESSAGE_WAITLIST_START: {
        val email = userContext.getDataEntry(EMAIL);
        if (emailIsActivated(email)) {
          flagCodeAsUsed(email);
          userContext.putUserData(SIGNED_UP, "true");
          nxtMsg = MESSAGE_ONBOARDING_START;
        }
        break;
      }
      case MESSAGE_STUDENT_LIMIT_LIVING_SPACE_HOUSE_TYPE:
      case "message.lghtyp": {
        SelectItem item = ((MessageBodySingleSelect) m.body).getSelectedItem();

        // Additional question for sublet contracts
        m.body.text = item.text;
        addToChat(m, userContext);
        if (item.value.equals("message.lghtyp.sublet")) {
          nxtMsg = "message.lghtyp.sublet";
          break;
        } else {
          UserData obd = userContext.getOnBoardingData();
          obd.setHouseType(item.value);
          nxtMsg = "message.pers";
        }
        break;
      }
      case "message.lghtyp.sublet": {
        SelectItem item = ((MessageBodySingleSelect) m.body).getSelectedItem();
        UserData obd = userContext.getOnBoardingData();
        obd.setHouseType(item.value);
        m.body.text = item.text;
        nxtMsg = "message.pers";
        break;
      }
      case MESSAGE_BANKIDJA: {
        val item = ((MessageBodySingleSelect) m.body).getSelectedItem();
        m.body.text = item.text;
        addToChat(m, userContext);
        if (Objects.equals(item.value, MESSAGE_KVADRAT)) {
          nxtMsg = handleStudentEntrypoint(MESSAGE_KVADRAT, userContext);
        }else if(Objects.equals(item.value, MESSAGE_VARBORDUFELADRESS)) {
          val obd = userContext.getOnBoardingData();
          obd.clearAddress();
        }
        break;
      }

      case "message.student": {
        SelectItem sitem2 = ((MessageBodySingleSelect) m.body).getSelectedItem();
        if (sitem2.value.equals("message.studentja")) {
          m.body.text = sitem2.text;
          addToChat(m, userContext);
          userContext.putUserData("{STUDENT}", "1");
        }
        break;
      }

      case "message.audiotest":
      case "message.phototest":
        nxtMsg = "message.fileupload.result";
        break;
      case MESSAGE_FORSLAGSTART:
        onBoardingData.setHouseType(((MessageBodySingleSelect) m.body).getSelectedItem().value);
        break;
      case "message.kontrakt.email":
        onBoardingData.setEmail(m.body.text);
        memberService.updateEmail(userContext.getMemberId(), m.body.text.trim());
        m.body.text = m.body.text;
        addToChat(m, userContext);
        endConversation(userContext);
        return;

      case MESSAGE_NYHETSBREV:
        onBoardingData.setNewsLetterEmail(m.body.text);
        nxtMsg = MESSAGE_NAGOTMER;
        break;
      case "message.signup.email":
      case MESSAGE_SIGNUP_TO_WAITLIST:
        // Logic goes here
        String userEmail = m.body.text.toLowerCase().trim();
        onBoardingData.setEmail(userEmail);
        m.body.text = userEmail;
        addToChat(m, userContext);

        // --------- Logic for user state ------------- //
        Optional<SignupCode> existingSignupCode = findSignupCodeByEmail(userEmail);

        // User already has a signup code
        if (existingSignupCode.isPresent()) {
          SignupCode esc = existingSignupCode.get();
          if (esc.getActive()) { // User should have got an activation code
            flagCodeAsUsed(userEmail);
            userContext.putUserData(SIGNED_UP, "true");
            nxtMsg = MESSAGE_ONBOARDING_START;
          } else {
            nxtMsg = "message.signup.checkposition";
          }
        } else {
          SignupCode sc = createSignupCode(userEmail);
          userContext.putUserData(EMAIL, userEmail);
          userContext.putUserData("{SIGNUP_CODE}", sc.code);
          nxtMsg = "message.signup.checkposition";
        }
        userContext.putUserData("{SIGNUP_POSITION}",
            Objects.toString(getSignupQueuePosition(userEmail)));

        break;
      case "message.signup.flerval":
        userContext.putUserData("{SIGNUP_POSITION}",
            Objects.toString(getSignupQueuePosition(onBoardingData.getEmail())));
        break;
      case "message.waitlist.user.alreadyactive":
      case "message.activate.nocode.tryagain":
      case MESSAGE_CHECK_IF_ACTIVE_ON_WAITLIST: {
        // Logic goes here
        val email = m.body.text.trim().toLowerCase();
        if (emailIsActivated(email)) {
          flagCodeAsUsed(email);
          userContext.putUserData(SIGNED_UP, "true");
          userContext.putUserData(EMAIL, email);
          nxtMsg = MESSAGE_ONBOARDING_START;
          addToChat(m, userContext);
          break;
        }

        if (emailIsRegistered(email) == false) {
          onBoardingData.setEmail(email);
          val signupCode = createSignupCode(m.body.text);
          userContext.putUserData("{SIGNUP_CODE}", signupCode.code);
          userContext.putUserData(EMAIL, email);
          userContext.putUserData("{SIGNUP_POSITION}",
              Objects.toString(getSignupQueuePosition(email)));
          nxtMsg = MESSAGE_SIGNUP_NOT_REGISTERED_YET;
          break;
        }
        nxtMsg = MESSAGE_SIGNUP_NOT_ACTIVATED_YET;
        addToChat(m, userContext);
        break;
      }
      case MESSAGE_SIGNUP_NOT_REGISTERED_YET:
      case MESSAGE_SIGNUP_NOT_ACTIVATED_YET:
      case "message.signup.checkposition": {
        m.body.text = ((MessageBodySingleSelect) m.body).getSelectedItem().text;
        addToChat(m, userContext);
        val email = userContext.getDataEntry(EMAIL);
        if (email == null) {
          break;
        }
        if (emailIsActivated(email)) {
          nxtMsg = MESSAGE_ONBOARDING_START;
        } else {
          nxtMsg = MESSAGE_SIGNUP_NOT_ACTIVATED_YET;
        }
        break;
      }
      case "message.uwlimit.housingsize":
      case "message.uwlimit.householdsize":
        nxtMsg = handleUnderwritingLimitResponse(userContext, m, m.getBaseMessageId());
        break;
      case MESSAGE_TIPSA:
        onBoardingData.setRecommendFriendEmail(m.body.text);
        nxtMsg = MESSAGE_NAGOTMER;
        break;
      case MESSAGE_FRIFRAGA:
        handleFriFraga(userContext, m);
        nxtMsg = "message.frifragatack";
        break;
      case MESSAGE_FRIONBOARDINGFRAGA:
        handleFriFraga(userContext, m);
        nxtMsg = "message.frionboardingfragatack";
        break;
      case "message.pers":
        int nr_persons = getValue((MessageBodyNumber) m.body);
        onBoardingData.setPersonInHouseHold(nr_persons);
        if (nr_persons == 1) {
          m.body.text = "Jag bor själv";
        } else {
          m.body.text = "Vi är " + nr_persons;
        }
        addToChat(m, userContext);

        if (nr_persons > 6) {
          nxtMsg = "message.uwlimit.householdsize";
          break;
        }

        nxtMsg = handleStudentPolicyPersonLimit(MESSAGE_SAKERHET, userContext);
        break;
      case MESSAGE_KVADRAT: {
        String kvm = m.body.text;
        onBoardingData.setLivingSpace(Float.parseFloat(kvm));
        m.body.text = kvm + " kvm";
        addToChat(m, userContext);
        if (Integer.parseInt(kvm) > 250) {
          nxtMsg = "message.uwlimit.housingsize";
          break;
        }
        nxtMsg = handleStudentPolicyLivingSpace("message.lghtyp", userContext);
        break;
      }
      case "message.manuellnamn":
        onBoardingData.setFirstName(m.body.text);
        addToChat(m, userContext);
        nxtMsg = "message.manuellfamilyname";
        break;
      case "message.manuellfamilyname":
        onBoardingData.setFamilyName(m.body.text);
        addToChat(m, userContext);
        nxtMsg = "message.manuellpersonnr";
        break;
      case "message.manuellpersonnr":
        onBoardingData.setSSN(m.body.text);

        // Member service is responsible for handling SSN->birth date conversion
        try {
          memberService.startOnBoardingWithSSN(userContext.getMemberId(), m.body.text);
          MemberProfile member = memberService.getProfile(userContext.getMemberId());
          onBoardingData.setBirthDate(member.getBirthDate());
        } catch (Exception ex) {
          log.error("Error loading memberProfile from memberService", ex);
        }

        addToChat(m, userContext);
        nxtMsg = "message.varborduadress";
        break;
      case "message.bankidja.noaddress":
      case MESSAGE_VARBORDUFELADRESS:
      case "message.varborduadress":
        onBoardingData.setAddressStreet(m.body.text);
        addToChat(m, userContext);
        nxtMsg = "message.varbordupostnr";
        break;
      case "message.varbordupostnr":
        onBoardingData.setAddressZipCode(m.body.text);
        addToChat(m, userContext);
        nxtMsg = handleStudentEntrypoint(MESSAGE_KVADRAT, userContext);
        break;
      case "message.varbordu":
        onBoardingData.setAddressStreet(m.body.text);
        addToChat(m, userContext);
        nxtMsg = MESSAGE_KVADRAT;
        break;
      case "message.mail":
        onBoardingData.setEmail(m.body.text);
        addToChat(m, userContext);
        nxtMsg = "message.kontrakt";
        break;
      case MESSAGE_SAKERHET: {
        MessageBodyMultipleSelect body = (MessageBodyMultipleSelect) m.body;

        if (body.getNoSelectedOptions() == 0) {
          m.body.text = "Jag har inga säkerhetsgrejer";
        } else {
          m.body.text = String.format("Jag har %s", body.selectedOptionsAsString());
          for (SelectOption o : body.selectedOptions()) {
            onBoardingData.addSecurityItem(o.value);
          }
        }
        addToChat(m, userContext);
        UserData userData = userContext.getOnBoardingData();
        if (userData.getStudentPolicyEligibility() == true) {
          nxtMsg = "message.student.25klimit";
          break;
        }
        nxtMsg = MESSAGE_50K_LIMIT;
        break;
      }
      case MESSAGE_PHONENUMBER:
        String trim = m.body.text.trim();
        userContext.putUserData("{PHONE_NUMBER}", trim);
        m.body.text = "Mitt telefonnummer är " + trim;
        addToChat(m, userContext);
        // nxtMsg = MESSAGE_EMAIL;
        nxtMsg = MESSAGE_FORSAKRINGIDAG;
        break;
      case MESSAGE_EMAIL:
        String trim2 = m.body.text.trim();
        userContext.putUserData("{EMAIL}", trim2);
        m.body.text = "Min email är " + trim2;
        memberService.updateEmail(userContext.getMemberId(), trim2);
        addToChat(m, userContext);
        endConversation(userContext);
        return;
      // nxtMsg = MESSAGE_FORSAKRINGIDAG;
      case MESSAGE_50K_LIMIT_YES:
        MessageBodySingleSelect body1 = (MessageBodySingleSelect) m.body;
        for (SelectItem o : body1.choices) {
          if (o.selected) {
            m.body.text = o.text;
            addToChat(m, userContext);
            nxtMsg = o.value;
          }
        }
        nxtMsg = handle50KLimitAnswer(nxtMsg, userContext, (MessageBodySingleSelect) m.body);
        break;
      // case "message.bytesinfo":
      case "message.bytesinfo2":
      case MESSAGE_FORSAKRINGIDAG:
      case "message.missingvalue":
      case MESSAGE_FORSLAG2:
        SelectItem item = ((MessageBodySingleSelect) m.body).getSelectedItem();

        /*
         * Check if there is any data missing. Keep ask until Hedvig has got all info
         */
        String missingItems = userContext.getMissingDataItem();
        if (missingItems != null) {

          createMessage("message.missingvalue", new MessageBodyText(
              "Oj, nu verkar det som om jag saknar lite viktig information." + missingItems));

          m.body.text = item.text;
          nxtMsg = "message.missingvalue";
          addToChat(m, userContext);
          addToChat(getMessage("message.missingvalue"), userContext);
          break;
        } else if (m.id.equals("message.missingvalue") || item.value.equals(MESSAGE_FORSLAG2)) {
          completeOnboarding(userContext);
        }
        break;
      case MESSAGE_ANNATBOLAG:
        String _comp = m.body.text;
        userContext.getOnBoardingData().setCurrentInsurer(_comp);
        m.body.text = _comp;
        nxtMsg = MESSAGE_BYTESINFO;
        addToChat(m, userContext);
        break;
      case MESSAGE_FORSAKRINGIDAGJA:
      case "message.bolag.annat.expand":
        String comp = getValue((MessageBodySingleSelect) m.body);
        if (!comp.startsWith("message.")) {
          userContext.getOnBoardingData().setCurrentInsurer(comp);
          m.body.text = comp;
          nxtMsg = MESSAGE_BYTESINFO;
          addToChat(m, userContext);
        }
        break;
      case "message.forslagstart3":
        addToChat(m, userContext);
        break;

      case "message.bankid.start.manual":
        String ssn = m.body.text;

        Optional<BankIdAuthResponse> ssnResponse = memberService.auth(ssn);

        if (!ssnResponse.isPresent()) {
          log.error("Could not start bankIdAuthentication!");
          nxtMsg = "message.bankid.start.manual.error";
        } else {
          userContext.startBankIdAuth(ssnResponse.get());
        }

        if (nxtMsg.equals("")) {
          nxtMsg = "message.bankid.autostart.respond";
        }

        addToChat(m, userContext);
        break;

      case "message.kontrakt":
        completeOnboarding(userContext);
        break;

      case "message.kontraktklar":
        m.body.text = ((MessageBodySingleSelect) m.body).getSelectedItem().text;
        addToChat(m, userContext);
        if (((MessageBodySingleSelect) m.body).getSelectedItem().value
            .equals("message.kontrakt.email")) {
          // NOOP
          nxtMsg = "message.kontrakt.email";
        } else {
          endConversation(userContext);
          return;
        }
        break;

      default:
        break;
    }

    /*
     * In a Single select, there is only one trigger event. Set default here to be a link to a new
     * message
     */
    if (nxtMsg.equals("") && m.body.getClass().equals(MessageBodySingleSelect.class)) {

      MessageBodySingleSelect body1 = (MessageBodySingleSelect) m.body;
      for (SelectItem o : body1.choices) {
        if (o.selected) {
          m.body.text = o.text;
          addToChat(m, userContext);
          nxtMsg = o.value;
        }
      }
    }

    completeRequest(nxtMsg, userContext);
  }

  private String handleStudentEntrypoint(String defaultMessage, UserContext uc) {
    val onboardingData = uc.getOnBoardingData();
    if (onboardingData.getAge() > 0 && onboardingData.getAge() < 30) {
      return "message.student";
    }
    return defaultMessage;
  }

  private String handleStudentPolicyLivingSpace(String defaultMessage, UserContext uc) {
    val onboardingData = uc.getOnBoardingData();
    val isStudent = onboardingData.isStudent();

    if (!isStudent) {
      return defaultMessage;
    }

    val livingSpace = onboardingData.getLivingSpace();
    if (livingSpace > 50) {
      return MESSAGE_STUDENT_LIMIT_LIVING_SPACE;
    }

    return defaultMessage;
  }

  private String handleStudentPolicyPersonLimit(String defaultMessage, UserContext uc) {
    val onboardingData = uc.getOnBoardingData();
    val isStudent = onboardingData.isStudent();
    if (!isStudent) {
      return defaultMessage;
    }

    val livingSpace = onboardingData.getLivingSpace();
    if (livingSpace > 50) {
      return defaultMessage;
    }

    val personsInHousehold = onboardingData.getPersonsInHouseHold();
    if (personsInHousehold > 2) {
      onboardingData.setStudentPolicyEligibility(false);
      return MESSAGE_STUDENT_LIMIT_PERSONS;
    }

    onboardingData.setStudentPolicyEligibility(true);

    val houseType = onboardingData.getHouseType();
    if (Objects.equals(houseType, ProductTypes.BRF.toString())) {
      onboardingData.setHouseType(ProductTypes.STUDENT_BRF.toString());
      return MESSAGE_STUDENT_ELIGIBLE_BRF;
    }

    if (Objects.equals(houseType, ProductTypes.RENT.toString())) {
      onboardingData.setHouseType(ProductTypes.STUDENT_RENT.toString());
      return MESSAGE_STUDENT_ELIGIBLE_RENT;
    }

    log.error("This state should be unreachable");
    return defaultMessage;
  }

  private String handle50KLimitAnswer(String nxtMsg, UserContext userContext,
      MessageBodySingleSelect body) {
    if (body.getSelectedItem().value.equalsIgnoreCase(MESSAGE_50K_LIMIT_YES_YES)) {
      UserData userData = userContext.getOnBoardingData();
      val studentPolicyEligibility = userData.getStudentPolicyEligibility();
      if (studentPolicyEligibility != null && studentPolicyEligibility == true) {
        userContext.putUserData(UserData.TWENTYFIVE_THOUSAND_LIMIT, "true");
      } else {
        userContext.putUserData("{50K_LIMIT}", "true");
      }
    }
    return MESSAGE_PHONENUMBER;
  }

  private void handleFriFraga(UserContext userContext, Message m) {
    userContext.putUserData("{ONBOARDING_QUESTION_" + LocalDateTime.now().toString() + "}",
        m.body.text);
    eventPublisher
        .publishEvent(new OnboardingQuestionAskedEvent(userContext.getMemberId(), m.body.text));
    addToChat(m, userContext);
  }

  private String handleUnderwritingLimitResponse(UserContext userContext, Message m,
      String messageId) {
    String nxtMsg;
    userContext.putUserData("{PHONENUMBER}", m.body.text);
    UnderwritingLimitExcededEvent.UnderwritingType type = messageId.endsWith("householdsize")
        ? UnderwritingLimitExcededEvent.UnderwritingType.HouseholdSize
        : UnderwritingLimitExcededEvent.UnderwritingType.HouseingSize;

    final UserData onBoardingData = userContext.getOnBoardingData();
    eventPublisher.publishEvent(new UnderwritingLimitExcededEvent(userContext.getMemberId(),
        m.body.text, onBoardingData.getFirstName(), onBoardingData.getFamilyName(), type));

    addToChat(m, userContext);
    nxtMsg = "message.uwlimit.tack";
    return nxtMsg;
  }

  private void endConversation(UserContext userContext) {
    userContext.completeConversation(this);
    userContext
        .startConversation(conversationFactory.createConversation(CharityConversation.class));
  }

  /*
   * Generate next chat message or ends conversation
   */
  @Override
  public void completeRequest(String nxtMsg, UserContext userContext) {

    switch (nxtMsg) {
      case "message.medlem":
      case "message.bankid.start":
      case MESSAGE_LAGENHET:
        Optional<BankIdAuthResponse> authResponse = memberService.auth(userContext.getMemberId());

        if (!authResponse.isPresent()) {
          log.error("Could not start bankIdAuthentication!");

          nxtMsg = MESSAGE_WAITLIST_START;
        } else {
          BankIdAuthResponse bankIdAuthResponse = authResponse.get();
          userContext.startBankIdAuth(bankIdAuthResponse);
        }

        break;
      case "onboarding.done":
        // userContext.onboardingComplete(true);
        break;
      case "":
        log.error("I dont know where to go next...");
        nxtMsg = "error";
        break;
    }

    super.completeRequest(nxtMsg, userContext);
  }

  @Override
  public List<SelectItem> getSelectItemsForAnswer(UserContext uc) {

    final List<SelectItem> items = Lists.newArrayList();

    String questionId;
    if (uc.getOnBoardingData().getHouseType().equals(MESSAGE_HUS)) {
      questionId = MESSAGE_FRIONBOARDINGFRAGA;

    } else {
      questionId = MESSAGE_FRIFRAGA;
      items.add(SelectLink.toOffer("Visa mig förslaget", "message.forslag.dashboard"));
    }

    items.add(new SelectOption("Jag har en till fråga", questionId));

    return items;
  }

  @Override
  public boolean canAcceptAnswerToQuestion(UserContext uc) {
    return uc.getOnBoardingData().getHouseType() != null;
  }

  @Override
  void addToChat(Message m, UserContext userContext) {
    super.addToChat(m, userContext);
  }

  @Override
  public void bankIdAuthComplete(UserContext userContext) {

    if (userContext.getOnBoardingData().getUserHasSigned()) {
      userContext.completeConversation(this);
      Conversation mc = conversationFactory.createConversation(MainConversation.class);
      userContext.startConversation(mc);
    } else if (userContext.getDataEntry(LOGIN) != null) {
      userContext.removeDataEntry(LOGIN);
      addToChat(getMessage("message.membernotfound"), userContext);
    } else {
      addToChat(getMessage(MESSAGE_BANKIDJA), userContext);
    }
  }

  @Override
  public void bankIdAuthCompleteNoAddress(UserContext uc) {
    addToChat(getMessage("message.bankidja.noaddress"), uc);
  }

  @Override
  public void bankIdAuthGeneralCollectError(UserContext userContext) {
    addToChat(getMessage("message.bankid.error"), userContext);
    String bankIdStartMessage = userContext.getOnBoardingData().getBankIdMessage();
    addToChat(getMessage(bankIdStartMessage), userContext);
  }

  public void quoteAccepted(UserContext userContext) {
    addToChat(getMessage("message.kontrakt.great"), userContext);
  }

  @Override
  public void memberSigned(String referenceId, UserContext userContext) {
    Boolean signed = userContext.getOnBoardingData().getUserHasSigned();

    if (!signed) {
      val maybeActiveConversation = userContext.getActiveConversation();
      if (maybeActiveConversation.isPresent()) {
        val activeConversation = maybeActiveConversation.get();
        if (activeConversation.containsConversation(FreeChatConversation.class)) {
          activeConversation.setConversationStatus(conversationStatus.COMPLETE);
          userContext.setActiveConversation(this);

          // Duct tape to shift onboarding conversation back into the correct state
          val onboardingConversation =
              userContext.getActiveConversation().orElseThrow(() -> new RuntimeException(
                  "active conversation is for some reason not onboarding chat anymore"));
          onboardingConversation.conversationStatus = conversationStatus.ONGOING;

        }
      }

      addToChat(getMessage("message.kontraktklar"), userContext);
      userContext.getOnBoardingData().setUserHasSigned(true);
      userContext.setInOfferState(false);
      val productType = userContext.getDataEntry(UserData.HOUSE);
      val memberId = userContext.getMemberId();
      val fiftyKLimit = userContext.getDataEntry("{50K_LIMIT}");
      val twentyFiveKLimit = userContext.getDataEntry(UserData.TWENTYFIVE_THOUSAND_LIMIT);
      if (Objects.equals(fiftyKLimit, "true")) {
        eventPublisher.publishEvent(new RequestObjectInsuranceEvent(memberId, productType));
      } else if (Objects.equals(twentyFiveKLimit, "true")) {
        eventPublisher.publishEvent(new RequestStudentObjectInsuranceEvent(memberId, productType));
      } else {
        eventPublisher.publishEvent(new MemberSignedEvent(memberId, productType));
      }
      // userContext.completeConversation(this);
    }
  }

  @Override
  public void bankIdSignError(UserContext uc) {
    addToChat(getMessage("message.kontrakt.signError"), uc);
  }

  @Override
  public void oustandingTransaction(UserContext uc) {}

  @Override
  public void noClient(UserContext uc) {}

  @Override
  public void started(UserContext uc) {}

  @Override
  public void userSign(UserContext uc) {}

  @Override
  public void couldNotLoadMemberProfile(UserContext uc) {
    addToChat(getMessage("message.missing.bisnode.data"), uc);
  }

  @Override
  public void signalSignFailure(ErrorType errorType, String detail, UserContext uc) {
    addBankIdErrorMessage(errorType, "message.kontraktpop.startBankId", uc);
  }

  @Override
  public void signalAuthFailiure(ErrorType errorType, String detail, UserContext uc) {
    addBankIdErrorMessage(errorType, uc.getOnBoardingData().getBankIdMessage(), uc);
  }

  private void addBankIdErrorMessage(ErrorType errorType, String baseMessage, UserContext uc) {
    String errorPostfix;
    switch (errorType) {
      case EXPIRED_TRANSACTION:
        errorPostfix = ".bankid.error.expiredTransaction";
        break;
      case CERTIFICATE_ERR:
        errorPostfix = ".bankid.error.certificateError";
        break;
      case USER_CANCEL:
        errorPostfix = ".bankid.error.userCancel";
        break;
      case CANCELLED:
        errorPostfix = ".bankid.error.cancelled";
        break;
      case START_FAILED:
        errorPostfix = ".bankid.error.startFailed";
        break;
      case INVALID_PARAMETERS:
        errorPostfix = ".bankid.error.invalidParameters";
        break;
      default:
        errorPostfix = "";
    }
    final String messageID = baseMessage + errorPostfix;
    log.info("Adding bankIDerror message: {}", messageID);
    addToChat(getMessage(messageID), uc);
  }

  private SignupCode createSignupCode(String email) {
    log.debug("Generate signup code for email: " + email);
    SignupCode sc = signupRepo.findByEmail(email).orElseGet(() -> {
      SignupCode newCode = new SignupCode(email);
      signupRepo.save(newCode);
      return newCode;
    });
    signupRepo.saveAndFlush(sc);

    eventPublisher.publishEvent(new SignedOnWaitlistEvent(email));

    return sc;
  }

  private Optional<SignupCode> findSignupCodeByEmail(String email) {
    return signupRepo.findByEmail(email);
  }

  private int getSignupQueuePosition(String email) {
    ArrayList<SignupCode> scList = (ArrayList<SignupCode>) signupRepo.findAllByOrderByDateAsc();
    int pos = 1;
    for (SignupCode sc : scList) {
      if (!sc.used) {
        log.debug(sc.code + "|" + sc.email + "(" + sc.date + "): " + (pos));
        if (sc.email.equals(email)) {
          return queuePos + pos;
        }
        pos++;
      }
    }
    return -1;
  }

  private boolean emailIsActivated(String email) {
    if (email == null) {
      return false;
    }

    val maybeSignupCode = signupRepo.findByEmail(email);
    if (maybeSignupCode.isPresent() == false) {
      return false;
    }

    val signupCode = maybeSignupCode.get();
    if (signupCode.getActive()) {
      return true;
    }

    return false;
  }

  private boolean emailIsRegistered(String email) {
    val maybeSignupCode = signupRepo.findByEmail(email);

    return maybeSignupCode.isPresent();
  }

  private void flagCodeAsUsed(String email) {
    val maybeSignupCode = signupRepo.findByEmail(email);
    if (maybeSignupCode.isPresent() == false) {
      log.error("Attempted to flag nonexistent code as used with email: {}", email);
      return;
    }
    val signupCode = maybeSignupCode.get();
    signupCode.setUsed(true);
    signupRepo.save(signupCode);
  }
}
