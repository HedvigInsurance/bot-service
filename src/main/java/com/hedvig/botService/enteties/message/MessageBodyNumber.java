package com.hedvig.botService.enteties.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hedvig.botService.enteties.UserContext;
import com.hedvig.localization.service.LocalizationService;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("number")
@ToString
public class MessageBodyNumber extends MessageBodyText {

  public MessageBodyNumber(String content) {
    super(content, KeyboardType.NUMBER_PAD);
  }

  public MessageBodyNumber(String content, String placeholder){
    this(content);
    this.placeholder = placeholder;
  }

  public MessageBodyNumber(String content, TextContentType textContentType){
    super(content, textContentType, KeyboardType.NUMBER_PAD);
  }

  public MessageBodyNumber(String content, TextContentType textContentType, String placeholder){
    super(content, textContentType, KeyboardType.NUMBER_PAD);
    this.placeholder = placeholder;
  }

  MessageBodyNumber() {}

  @JsonIgnore
  public Integer getValue(){
    return Integer.parseInt(text);
  }

  @Override
  public void render(String id, Boolean fromUser, UserContext userContext, LocalizationService localizationService) {
    super.render(id, fromUser, userContext, localizationService);
    String localizedText = localizationService.getText(userContext.getLocale(), id + ID_PLACEHOLDER_POST_FIX);
    if (localizedText != null){
      placeholder = localizedText;
    }
  }
}
