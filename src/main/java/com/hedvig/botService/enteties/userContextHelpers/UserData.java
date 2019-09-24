package com.hedvig.botService.enteties.userContextHelpers;

import com.hedvig.botService.chat.house.ExtraBuildingType;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.services.LocalizationService;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserData {

  public static final String MEMBER_BIRTH_DATE = "{MEMBER_BIRTH_DATE}";
  public static final String FIRST_NAME = "{NAME}";
  public static final String EMAIL = "{EMAIL}";
  public static final String FAMILY_NAME = "{FAMILY_NAME}";
  public static final String ADDRESS = "{ADDRESS}";
  public static final String SECURE_ITEM_ = "{SECURE_ITEM_%s}";
  private static final String ADDRESS_CITY = "{ADDRESS_CITY}";
  private static final String ADDRESS_ZIP = "{ADDRESS_ZIP}";
  public static final String SECURE_ITEMS_NO = "{SECURE_ITEMS_NO}";
  public static final String HOUSE = "{HOUSE}";
  public static final String EMAIL_NEWS_LETTER = "{EMAIL_NEWS_LETTER}";
  public static final String RECOMMEND_FRIEND_EMAIL = "{RECOMMEND_FRIEND_EMAIL}";
  public static final String NR_PERSONS = "{NR_PERSONS}";
  public static final String KVM = "{KVM}";
  public static final String SSN = "{SSN}";
  public static final String INSURANCE_COMPANY_TODAY = "{INSURANCE_COMPANY_TODAY}";
  public static final String PRODUCT_ID = "{PRODUCT_ID}";
  public static final String USER_HAS_SIGNED = "{USER_HAS_SIGNED}";
  public static final String USER_AUTHED_BANKID = "USER_AUTHED_BANKID";
  public static final String IS_STUDENT = "{STUDENT}";
  public static final String BANK_ID_MESSAGE = "{BANK_ID_MESSAGE}";
  public static final String FLOOR = "{FLOOR}";
  public static final String STUDENT_POLICY_ELIGIBILITY = "{STUDENT_POLICY_ELIGIBILITY}";
  public static final String TWENTYFIVE_THOUSAND_LIMIT = "{25K_LIMIT}";
  public static final String PHONE_NUMBER = "{PHONE_NUMBER}";
  public static final String LOGIN = "{LOGIN}";
  public static final String HOUSE_SUBFACE_KVM = "{HOUSE_SUBFACE_KVM}";
  public static final String HOUSE_NR_BATHROOMS = "{HOUSE_NR_BATHROOMS}";
  public static final String HOUSE_HAS_EXTRA_BUILDINGS = "{HOUSE_HAS_EXTRA_BUILDINGS}";
  public static final String HOUSE_NR_EXTRA_BUILDINGS = "{HOUSE_NR_EXTRA_BUILDINGS}";
  public static final String HOUSE_IS_SUBLETTING = "{HOUSE_IS_SUBLETTING}";

  public static final String HOUSE_EXTRA_BUILDINGS_TYPE_TEXT = "{HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}";

  private final UserContext ctx;

  public UserData(UserContext ctx) {
    this.ctx = ctx;
  }

  public int getAge() {
    String dateString = ctx.getDataEntry(MEMBER_BIRTH_DATE);
    if (dateString == null) {
      return -1;
    }
    LocalDate date = LocalDate.parse(dateString);

    return date.until(LocalDate.now()).getYears();
  }

  public void setBirthDate(LocalDate birthDate) {
    ctx.putUserData(MEMBER_BIRTH_DATE, birthDate.toString()); // = birthDate;
  }

  public LocalDate getBirthDate() {
    String bDate = ctx.getDataEntry(MEMBER_BIRTH_DATE);
    if (bDate == null) return null;
    return LocalDate.parse(bDate); // = birthDate;
  }

  public void setFirstName(String firstName) {
    ctx.putUserData(FIRST_NAME, firstName);
  }

  @Nullable
  public String getFirstName() {
    return ctx.getDataEntry(FIRST_NAME);
  }

  public Boolean isStudent() {
    return (ctx.getDataEntry(IS_STUDENT) != null
        && Objects.equals(ctx.getDataEntry(IS_STUDENT), "1"));
  }

  public void setEmail(String email) {
    ctx.putUserData(EMAIL, email);
  }

  public String getEmail() {
    return ctx.getDataEntry(EMAIL);
  }

  public String getPhoneNumber(){ return ctx.getDataEntry(PHONE_NUMBER); }

  public void setPhoneNumber(String phoneNumber) { ctx.putUserData(PHONE_NUMBER, phoneNumber); }

  public void setFamilyName(String familyName) {
    ctx.putUserData(FAMILY_NAME, familyName);
  }

  public String getFamilyName() {
    return ctx.getDataEntry(FAMILY_NAME);
  }

  public void setAddressStreet(String address) {
    ctx.putUserData(ADDRESS, address);
  }

  public String getAddressStreet() {
    return ctx.getDataEntry(ADDRESS);
  }

  public void setHouseType(String value) {
    ctx.putUserData(HOUSE, value);
  }

  public String getHouseType() {
    return ctx.getDataEntry(HOUSE);
  }

  public void setNewsLetterEmail(String text) {
    ctx.putUserData(EMAIL_NEWS_LETTER, text);
  }

  public String getNewsLetterEmail() {
    return ctx.getDataEntry(EMAIL_NEWS_LETTER);
  }

  public void setRecommendFriendEmail(String text) {
    ctx.putUserData(RECOMMEND_FRIEND_EMAIL, text);
  }

  public void setPersonInHouseHold(int nrPersons) {
    ctx.putUserData(NR_PERSONS, Objects.toString(nrPersons));
  }

  public int getPersonsInHouseHold() {
    return Integer.parseInt(ctx.getDataEntry(NR_PERSONS));
  }

  public void setLivingSpace(Float v) {
    ctx.putUserData(KVM, Objects.toString(Math.round(v)));
  }

  public Float getLivingSpace() {
    return Float.parseFloat(ctx.getDataEntry(KVM));
  }

  public void setSSN(String text) {
    ctx.putUserData(SSN, text);
  }

  public String getSSN() {
    return ctx.getDataEntry(SSN);
  }

  public void setCurrentInsurer(String comp) {
    ctx.putUserData(INSURANCE_COMPANY_TODAY, comp);
  }

  public String getCurrentInsurer() {
    return ctx.getDataEntry(INSURANCE_COMPANY_TODAY);
  }

  public void addSecurityItem(String value) {
    String items = ctx.getDataEntry(SECURE_ITEMS_NO);

    int nrItems = items != null ? Integer.parseInt(items) : 0;
    ctx.putUserData(String.format(SECURE_ITEM_, nrItems), value);
    ctx.putUserData(SECURE_ITEMS_NO, Objects.toString(nrItems + 1));
  }

  public List<String> getSecurityItems() {
    String nrOfItemsString = ctx.getDataEntry(SECURE_ITEMS_NO);
    int nrOfItems = nrOfItemsString != null ? Integer.parseInt(nrOfItemsString) : 0;
    List<String> retlist = new ArrayList<>();
    for (int i = 0; i < nrOfItems; i++) {
      retlist.add(ctx.getDataEntry(String.format(SECURE_ITEM_, i)));
    }

    return retlist;
  }

  public void setAddressCity(String city) {
    ctx.putUserData(ADDRESS_CITY, city);
  }

  public String getAddressCity() {
    return ctx.getDataEntry(ADDRESS_CITY);
  }

  public void setAddressZipCode(String zipCode) {
    ctx.putUserData(ADDRESS_ZIP, zipCode);
  }

  public String getAddressZipCode() {
    return ctx.getDataEntry(ADDRESS_ZIP);
  }

  public String getProductId() {
    return ctx.getDataEntry(PRODUCT_ID);
  }

  public void setProductId(String productId) {
    ctx.putUserData(PRODUCT_ID, productId);
  }

  public Boolean getUserHasSigned() {
    String value = ctx.getDataEntry(USER_HAS_SIGNED);
    if (value == null) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  public void setUserHasSigned(boolean b) {
    ctx.putUserData(USER_HAS_SIGNED, Objects.toString(b));
  }

  public boolean userHasAuthedWithBankId(String referenceId) {
    String b = ctx.getDataEntry(USER_AUTHED_BANKID);
    return b != null && b.equals(referenceId);
  }

  public void setBankIdMessage(String bankIdMessage) {
    ctx.putUserData(BANK_ID_MESSAGE, bankIdMessage);
  }

  public String getBankIdMessage() {
    return ctx.getDataEntry(BANK_ID_MESSAGE);
  }

  public void setUserHasAuthedWithBankId(String referenceId) {
    ctx.putUserData(USER_AUTHED_BANKID, referenceId);
  }

  public void setHouseSubface(String subfaceSqm){
    ctx.putUserData(HOUSE_SUBFACE_KVM, subfaceSqm);
  }

  public String getHouseSubface() {
    return ctx.getDataEntry(HOUSE_SUBFACE_KVM);
  }

  public void setBathroomsInHouse(int bathrooms) {
    ctx.putUserData(HOUSE_NR_BATHROOMS, Objects.toString(bathrooms));
  }

  public int getBathroomsInHouse() {
    return Integer.parseInt(ctx.getDataEntry(HOUSE_NR_BATHROOMS));
  }


  public void setHasExtraBuildings(boolean hasExtraBuildings) {
    ctx.putUserData(HOUSE_NR_EXTRA_BUILDINGS, Objects.toString(hasExtraBuildings));
  }

  public boolean getHasExtraBuildings() {
    String value = ctx.getDataEntry(HOUSE_HAS_EXTRA_BUILDINGS);
    if (value == null) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  public void setNrExtraBuildings(int extraBuildings) {
    ctx.putUserData(HOUSE_NR_EXTRA_BUILDINGS, Objects.toString(extraBuildings));
  }

  public int getNrExtraBuildings() {
    return Integer.parseInt(ctx.getDataEntry(HOUSE_NR_EXTRA_BUILDINGS));
  }

  public void setHouseExtraBuildingType(ExtraBuildingType type, int buildingNumber, Locale locale, LocalizationService localizationService){
    ctx.putUserData("{HOUSE_EXTRA_BUILDINGS_TYPE_" + buildingNumber +"}", type.toString());
    String text = localizationService.getText(locale, "HOUSE_EXTRA_BUILDING_" + type.toString());
    ctx.putUserData("{HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}", text);
  }

  public String getHouseExtraBuildingType(int buildingNumber) {
    return ctx.getDataEntry("{HOUSE_EXTRA_BUILDINGS_TYPE_" + buildingNumber +"}");
  }

  public String getHouseExtraBuildingTypeText(int buildingNumber) {
    return ctx.getDataEntry("{HOUSE_EXTRA_BUILDINGS_TYPE_TEXT}");
  }

  public void setHouseExtraBuildingSQM(int sqm, int buildingNumber){
    ctx.putUserData("{HOUSE_EXTRA_BUILDINGS_KVM_" + buildingNumber +"}", String.valueOf(sqm));
  }

  public int getHouseExtraBuildingSQM(int buildingNumber) {
    return Integer.parseInt(ctx.getDataEntry("{HOUSE_EXTRA_BUILDINGS_KVM_" + buildingNumber +"}"));
  }

  public void setHouseExtraBuildingHasWater(boolean hasWater, int buildingNumber){
    ctx.putUserData("{HOUSE_EXTRA_BUILDING_HAS_WATER_" + buildingNumber +"}", String.valueOf(hasWater));
  }

  public boolean getHouseExtraBuildingHasWater(int buildingNumber) {
    String value = ctx.getDataEntry("{HOUSE_EXTRA_BUILDING_HAS_WATER_" + buildingNumber +"}");
    if (value == null) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  public void setIsSubLetting(boolean isSubLetting) {
    ctx.putUserData(HOUSE_IS_SUBLETTING, Objects.toString(isSubLetting));
  }

  public boolean getIsSubLetting() {
    String value = ctx.getDataEntry(HOUSE_IS_SUBLETTING);
    if (value == null) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  public void clear() {
    ctx.removeDataEntry(MEMBER_BIRTH_DATE);
    ctx.removeDataEntry(FIRST_NAME);
    ctx.removeDataEntry(FAMILY_NAME);
    ctx.removeDataEntry(EMAIL);
    ctx.removeDataEntry(ADDRESS);
    ctx.removeDataEntry(ADDRESS_CITY);
    ctx.removeDataEntry(ADDRESS_ZIP);
    ctx.removeDataEntry(HOUSE);
    ctx.removeDataEntry(EMAIL_NEWS_LETTER);
    ctx.removeDataEntry(RECOMMEND_FRIEND_EMAIL);
    ctx.removeDataEntry(NR_PERSONS);
    ctx.removeDataEntry(KVM);
    ctx.removeDataEntry(SSN);
    ctx.removeDataEntry(INSURANCE_COMPANY_TODAY);
    ctx.removeDataEntry(PRODUCT_ID);
    ctx.removeDataEntry(RECOMMEND_FRIEND_EMAIL);
    ctx.removeDataEntry(USER_HAS_SIGNED);
    ctx.removeDataEntry(USER_AUTHED_BANKID);
    ctx.removeDataEntry(IS_STUDENT);
    ctx.removeDataEntry(STUDENT_POLICY_ELIGIBILITY);

    // TODO clear more

    String nrOfItemsString = ctx.getDataEntry(SECURE_ITEMS_NO);
    int nrOfItems = nrOfItemsString != null ? Integer.parseInt(nrOfItemsString) : 0;
    for (int i = 0; i < nrOfItems; i++) {
      ctx.removeDataEntry(String.format(SECURE_ITEM_, i));
    }

    ctx.removeDataEntry(SECURE_ITEMS_NO);
  }

  public void setFloor(int floor) {
    ctx.putUserData(FLOOR, Integer.toString(floor));
  }

  public Integer getFloor() {
    try {
      return Integer.parseInt(ctx.getDataEntry(FLOOR));
    } catch (Exception ex) {
      return 0;
    }
  }

  public void setStudentPolicyEligibility(Boolean eligible) {
    ctx.putUserData(STUDENT_POLICY_ELIGIBILITY, Boolean.toString(eligible));
  }

  public Boolean getStudentPolicyEligibility() {
    try {
      return Boolean.parseBoolean(ctx.getDataEntry(STUDENT_POLICY_ELIGIBILITY));
    } catch (Exception e) {
      return false;
    }
  }

  public void clearAddress() {
    setAddressZipCode(null);
    setAddressCity(null);
    setFloor(0);
    setAddressStreet(null);
  }
}
