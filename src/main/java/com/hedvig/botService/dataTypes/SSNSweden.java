package com.hedvig.botService.dataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSNSweden extends HedvigDataType {

  private static final String ZIPCODE_PATTERN = "^((19|20)?[0-9]{6})[- ]?[0-9]{4}$";
  private Pattern pattern;
  private Matcher matcher;

  public SSNSweden() {
    pattern = Pattern.compile(ZIPCODE_PATTERN);
  }

  public static void main(String args[]) {
    SSNSweden z = new SSNSweden();
    System.out.println(z.validate("200202020200"));
    System.out.println(z.validate("198720939393"));
    System.out.println(z.validate("8711909999  "));
    System.out.println(z.validate("se1230"));
  }

  @Override
  public boolean validate(String input) {
    if (input == null) {
      this.errorMessage = "Nu blev något fel tror jag... Försök igen";
      return false;
    }

    String trimmedInput = input.trim().replace(" ", "");

    if (trimmedInput.length() != 12) {
      this.errorMessage = "Personnummret måste skrivas med 12 siffor.";
      return false;
    }
    matcher = pattern.matcher(trimmedInput);

    boolean ok = matcher.matches();
    if (!ok) {
      this.errorMessage = input + " ser ut som ett konstigt personnummer. Ange gärna igen tack!";
      return false;
    }

    return true;
  }
}
