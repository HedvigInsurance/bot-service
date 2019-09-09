package com.hedvig.botService.dataTypes;

public class TextInput extends HedvigDataType {

  public TextInput() {}

  @Override
  public boolean validate(String input) {

    if (input == null) {
      this.errorMessage = "Nu tror jag du missade att skriva i något... Prova igen";
      return false;
    }
    return true;
  }
}
