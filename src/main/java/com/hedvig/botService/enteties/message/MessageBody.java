package com.hedvig.botService.enteties.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hedvig.botService.utils.ConversationUtils;
import com.hedvig.botService.utils.MessageUtil;
import com.hedvig.botService.enteties.UserContext;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.hedvig.botService.services.LocalizationService;
import lombok.ToString;

import static com.hedvig.botService.enteties.message.SelectItem.SELECT_POST_FIX;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "body_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = MessageBodyText.class, name = "text"),
  @JsonSubTypes.Type(value = MessageBodyNumber.class, name = "number"),
  @JsonSubTypes.Type(value = MessageBodySingleSelect.class, name = "single_select"),
  @JsonSubTypes.Type(value = MessageBodyMultipleSelect.class, name = "multiple_select"),
  @JsonSubTypes.Type(value = MessageBodyDatePicker.class, name = "date_picker"),
  @JsonSubTypes.Type(value = MessageBodyAudio.class, name = "audio"),
  @JsonSubTypes.Type(value = MessageBodyPhotoUpload.class, name = "photo_upload"),
  @JsonSubTypes.Type(value = MessageBodyVideo.class, name = "video"),
  @JsonSubTypes.Type(value = MessageBodyHero.class, name = "hero"),
  @JsonSubTypes.Type(value = MessageBodyParagraph.class, name = "paragraph"),
  @JsonSubTypes.Type(value = MessageBodyBankIdCollect.class, name = "bankid_collect"),
  @JsonSubTypes.Type(value = MessageBodyPolling.class, name = "polling"),
  @JsonSubTypes.Type(value = MessageBodyFileUpload.class, name = "file_upload")
})
@ToString
public class MessageBody {

  protected final String ID_PLACEHOLDER_POST_FIX = ".placeholder";
  protected final String ID_FROM_USER_POST_FIX = ".from.user";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @Column(length = 10485760)
  public String text;

  @Column(length = 10485)
  public String imageURL;

  @Column
  public String language;

  public Integer imageWidth;
  public Integer imageHeight;

  public MessageBody(String text) {
    this.text = text;
  }

  MessageBody() {}

  public void render(String id, Boolean fromUser, UserContext userContext, LocalizationService localizationService) {
    if (text.isEmpty()) {
      return;
    }

    String localizationKey;
    if (fromUser) {
      if (this instanceof MessageBodySingleSelect) {
        localizationKey = ((MessageBodySingleSelect) this).getSelectedItem().value + SELECT_POST_FIX + ID_FROM_USER_POST_FIX;
      } else {
        localizationKey = MessageUtil.INSTANCE.getBaseMessageId(id) + ID_FROM_USER_POST_FIX;
      }
    } else {
      localizationKey = MessageUtil.INSTANCE.getBaseMessageId(id);
    }

    String localizedText = localizationService.getText(userContext.getLocale(), localizationKey);

    if (localizedText != null) {
      Integer index = ConversationUtils.INSTANCE.getSplitIndexFromText(id);
      localizedText = ConversationUtils.INSTANCE.getSplitFromIndex(localizedText, index);
    }

    this.language = userContext.getLocale().getLanguage();
    this.text = userContext.replaceWithContext(localizedText != null ? localizedText : this.text);
  }
}
