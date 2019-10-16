package com.hedvig.botService.dataTypes;

import com.hedvig.botService.utils.BirthDateFromSSNUtil;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSNSweden extends HedvigDataType {

  private static final String SSN_PATTERN = "^((19|20)?[0-9]{6})[- ]?[0-9]{4}$";
  private Pattern pattern;
  private Matcher matcher;

  private String trimmedInput;

  public SSNSweden() {
    pattern = Pattern.compile(SSN_PATTERN);
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

    trimmedInput = input.trim().replace(" ", "");

    if (trimmedInput.length() != 10 && trimmedInput.length() != 12) {
      this.errorMessage = "Personnummret måste skrivas med 10 eller 12 siffor.";//todo add to translation
      return false;
    }

    if (trimmedInput.length() == 10) {

    }

    try {
      BirthDateFromSSNUtil birthDateFromSSNUtil = new BirthDateFromSSNUtil();
      birthDateFromSSNUtil.birthDateFromSSN(trimmedInput);
    }
    catch(DateTimeParseException exception) {
      this.errorMessage = "{INPUT} ser ut som ett konstigt personnummer. Ange gärna igen tack!";
      return false;
    }

    matcher = pattern.matcher(trimmedInput);

    boolean ok = matcher.matches();
    if (!ok) {
      this.errorMessage = "{INPUT} ser ut som ett konstigt personnummer. Ange gärna igen tack!";
      return false;
    }

    return true;
  }

  @Override
  public String getErrorMessageId() {
    if (trimmedInput == null) {
      return "hedvig.data.type.ssn.no.input";
    }
    if (trimmedInput.length() != 10 && trimmedInput.length() != 12) {
      return "hedvig.data.type.ssn.not.ten.or.twelve.digits";//todo add to translation
    }
    return "hedvig.data.type.ssn.did.not.match";
  }
}
