package com.hedvig.botService.dataTypes;

public class LivingSpaceSquareMeters extends HedvigDataType {

  public LivingSpaceSquareMeters() {}

  private Integer livingSpaceSquareMeters;

  @Override
  public boolean validate(String input) {
    try {
      livingSpaceSquareMeters = Integer.parseInt(input);
      if (livingSpaceSquareMeters < 5) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!";
        return false;
      }
      if (livingSpaceSquareMeters > 400) {
        this.errorMessage = "{INPUT}kvm?! Kan man bo så stort? Hmm... Prova igen tack!";
        return false;
      }
    } catch (NumberFormatException e) {
      livingSpaceSquareMeters = null;
      this.errorMessage = "{INPUT} verkar vara ett konstigt antal kvadratmeter. Prova igen tack";
      return false;
    }
    return true;
  }

  @Override
  public String getErrorMessageId() {
    if (livingSpaceSquareMeters == null) {
      return "hedvig.data.type.living.space.square.meters.not.a.number";
    }

    if (livingSpaceSquareMeters < 5) {
      return "hedvig.data.type.living.space.square.meters.to.small";
    }

    if (livingSpaceSquareMeters > 400) {
      return "hedvig.data.type.living.space.square.meters.to.big";
    }

    return null;
  }
}
