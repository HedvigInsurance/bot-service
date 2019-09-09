package com.hedvig.botService.dataTypes;

public class HouseholdMemberNumber extends HedvigDataType {

  public HouseholdMemberNumber() {}

  private Integer householdMemberNumber;

  @Override
  public boolean validate(String input) {
    try {
      householdMemberNumber = Integer.parseInt(input);
      if (householdMemberNumber < 1) {
        this.errorMessage = "{INPUT} låter som väldigt få personer. Prova att ange igen tack";
        return false;
      }
      if (householdMemberNumber > 20) {
        this.errorMessage = "{INPUT}? I ett och samma hushåll?. Hmm... Prova att ange igen tack";
        return false;
      }
    } catch (NumberFormatException e) {
      householdMemberNumber = null;
      this.errorMessage = "{INPUT} verkar vara ett kontigt antal personer. Prova igen tack";
      return false;
    }
    return true;
  }

  @Override
  public String getErrorMessageId() {
    if (householdMemberNumber == null) {
      return "hedvig.data.type.household.member.number.not.a.number";
    }
    if (householdMemberNumber < 1) {
      return "hedvig.data.type.household.member.number.less.than.one";
    }
    if (householdMemberNumber > 20) {
      return "hedvig.data.type.household.member.number.more.than.20";
    }

    return null;
  }
}
