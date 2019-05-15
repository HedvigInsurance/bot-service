package com.hedvig.botService.enteties.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hedvig.botService.enteties.UserContext;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import lombok.ToString;

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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @Column(length = 10485760)
  public String text;

  @Column(length = 10485)
  public String imageURL;

  public Integer imageWidth;
  public Integer imageHeight;

  public MessageBody(String text) {
    this.text = text;
  }

  MessageBody() {}

  public void render(UserContext userContext) {
    this.text = userContext.replaceWithContext(this.text);
  }
}
