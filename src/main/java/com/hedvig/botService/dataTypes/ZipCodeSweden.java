package com.hedvig.botService.dataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZipCodeSweden extends HedvigDataType {

  static final String ERROR_MESSAGE_NO_INPUT = "hedvig.data.type.zip.code.sweden.no.input";
  static final String ERROR_MESSAGE_NO_MATCH = "hedvig.data.type.zip.code.sweden.no.match";

  private static final String ZIPCODE_PATTERN = "^(s-|S-|SE|se|S|s){0,1}[0-9]{3}\\s?[0-9]{2}$";
  private Pattern pattern;
  private Matcher matcher;

  private String input;

  public ZipCodeSweden() {
    pattern = Pattern.compile(ZIPCODE_PATTERN);
  }

  public static void main(String args[]) {
    ZipCodeSweden z = new ZipCodeSweden();
    System.out.println(z.validate("932 68"));
    System.out.println(z.validate("S-621 46"));
    System.out.println(z.validate("12323"));
    System.out.println(z.validate("se1230"));
  }

  @Override
  public boolean validate(String input) {
    this.input = input;
    if (input == null) {
      this.errorMessage = "Nu blev något fel tror jag... Försök igen";
    }
    matcher = pattern.matcher(input.trim());
    this.errorMessage = "{INPUT} är inte ett postnummer jag känner till. Ange gärna igen tack!";
    return matcher.matches();
  }

  @Override
  public String getErrorMessageId() {
    if (input == null) {
      return ERROR_MESSAGE_NO_INPUT;
    }
    return ERROR_MESSAGE_NO_MATCH;
  }
}
