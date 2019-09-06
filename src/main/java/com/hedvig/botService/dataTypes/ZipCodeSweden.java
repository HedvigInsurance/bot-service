package com.hedvig.botService.dataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZipCodeSweden extends HedvigDataType {

  private static final String ZIPCODE_PATTERN = "^(s-|S-|SE|se|S|s){0,1}[0-9]{3}\\s?[0-9]{2}$";
  private Pattern pattern;
  private Matcher matcher;

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
    if (input == null) {
      this.errorMessage = "Nu blev något fel tror jag... Försök igen";
    }
    matcher = pattern.matcher(input.trim());
    this.errorMessage = input + " är inte ett postnummer jag känner till. Ange gärna igen tack!";
    return matcher.matches();
  }
}
