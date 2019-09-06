package com.hedvig.botService.dataTypes;

public class HouseholdMemberNumber extends HedvigDataType {

  public HouseholdMemberNumber() {}

  @Override
  public boolean validate(String input) {
    try {
      Integer m = Integer.parseInt(input);
      if (m < 1) {
        this.errorMessage = input + " låter som väldigt få personer. Prova att ange igen tack";
        return false;
      }
      if (m > 20) {
        this.errorMessage = input + "? I ett och samma hushåll?. Hmm... Prova att ange igen tack";
        return false;
      }
    } catch (NumberFormatException e) {
      this.errorMessage = input + " verkar vara ett kontigt antal personer. Prova igen tack";
      return false;
    }
    return true;
  }
}
