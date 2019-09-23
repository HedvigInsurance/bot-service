package com.hedvig.botService.dataTypes;

public class SubfaceSquareMeters extends HedvigDataType {

  public SubfaceSquareMeters() {}

  private Integer livingSpaceSquareMeters;

  @Override
  public boolean validate(String input) {
    try {
      livingSpaceSquareMeters = Integer.parseInt(input);
      if (livingSpaceSquareMeters < 0) {
        this.errorMessage = "{INPUT}kvm låter väldigt litet. Prova igen tack!";
        return false;
      }
      if (livingSpaceSquareMeters > 400) {
        this.errorMessage = "{INPUT}kvm biyta?! Låter väldigt mycket? Hmm... Prova igen tack!";
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
      return "hedvig.data.type.sub.face.square.meters.not.a.number";
    }

    if (livingSpaceSquareMeters < 0) {
      return "hedvig.data.type.sub.face.square.meters.to.small";
    }

    if (livingSpaceSquareMeters > 400) {
      return "hedvig.data.type.sub.face.square.meters.to.big";
    }

    return null;
  }
}
