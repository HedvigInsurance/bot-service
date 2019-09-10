package com.hedvig.botService.dataTypes;

import lombok.ToString;

@ToString
public abstract class HedvigDataType {

  public String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public abstract boolean validate(String input);
}
