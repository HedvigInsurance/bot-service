package com.hedvig.botService.dataTypes;

public class LivingSpaceSquareMeters extends HedvigDataType {

  public LivingSpaceSquareMeters() {}

  @Override
  public boolean validate(String input) {
    try {
      Integer m = Integer.parseInt(input);
      if (m < 5) {
        this.errorMessage = input + "kvm låter väldigt litet. Prova igen tack!";
        return false;
      }
      if (m > 400) {
        this.errorMessage = input + "kvm?! Kan man bo så stort? Hmm... Prova igen tack!";
        return false;
      }
    } catch (NumberFormatException e) {
      this.errorMessage = input + " verkar vara ett konstigt antal kvadratmeter. Prova igen tack";
      return false;
    }
    return true;
  }
}
