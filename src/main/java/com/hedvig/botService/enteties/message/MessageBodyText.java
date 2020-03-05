package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.localization.service.LocalizationService;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue("text")
@ToString
public class MessageBodyText extends MessageBody {



  public MessageBodyText(String content) {
    super(content);
  }

  public MessageBodyText(String content, KeyboardType keyboardType) {
    super(content);
    this.keyboardType = keyboardType;
  }

  public MessageBodyText(String content,  KeyboardType keyboardType, String placeholder) {
    this(content, keyboardType);
    this.placeholder = placeholder;
  }

  public MessageBodyText(String content, TextContentType textContentType, KeyboardType keyboardType, String placeholder) {
    this(content, keyboardType);
    this.textContentType = textContentType;
    this.placeholder = placeholder;
  }

  public MessageBodyText(String content, TextContentType textContentType, KeyboardType keyboardType) {
    this(content, keyboardType);
    this.textContentType = textContentType;
  }

  MessageBodyText() {}

  @Enumerated(EnumType.STRING)
  public KeyboardType keyboardType = KeyboardType.DEFAULT;

  public String placeholder;

  @Enumerated(EnumType.STRING)
  public TextContentType textContentType;

  @Override
  public void render(String id, Boolean fromUser, UserContext userContext, LocalizationService localizationService) {
    super.render(id, fromUser, userContext, localizationService);
    String localizedText = localizationService.getText(userContext.getLocale(), id + ID_PLACEHOLDER_POST_FIX);
    if (localizedText != null){
      placeholder = localizedText;
    }
  }
}
