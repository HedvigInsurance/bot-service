package com.hedvig.botService.dataTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumber extends HedvigDataType {

  private static final String PHONE_NUMBER_PATTERN = "^((46)(7)|07)\\d{8}$";
  private static Logger log = LoggerFactory.getLogger(PhoneNumber.class);
  private Pattern pattern;
  private Matcher matcher;

  public PhoneNumber() {
    this.errorMessage = "{INPUT} låter inte som en korrekt telefonnummer... Prova igen tack!";
    pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
  }

  @Override
  public boolean validate(String input) {
    log.debug("Validating phone number: " + input);
    matcher = pattern.matcher(input);
    this.errorMessage = this.errorMessage.replace("{INPUT}", input);
    return matcher.matches();
  }
}
