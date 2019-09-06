package com.hedvig.botService.dataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailAdress extends HedvigDataType {

  private static final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
          + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private static Logger log = LoggerFactory.getLogger(EmailAdress.class);
  private Pattern pattern;
  private Matcher matcher;

  public EmailAdress() {
    this.errorMessage = "{INPUT} låter inte som en korrekt emailadress... Prova igen tack!";
    pattern = Pattern.compile(EMAIL_PATTERN);
  }

  @Override
  public boolean validate(String input) {
    log.debug("Validating email adress: " + input);
    matcher = pattern.matcher(input);
    this.errorMessage = this.errorMessage.replace("{INPUT}", input);
    return matcher.matches();
  }
}
