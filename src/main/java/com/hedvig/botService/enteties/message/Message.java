package com.hedvig.botService.enteties.message;

/*
 * Base class for interaction between Hedvig and members
 * */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hedvig.botService.dataTypes.HedvigDataType;
import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.UserContext;
import java.time.Instant;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;

@Entity
@ToString(exclude = "chat")
@Data
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer globalId;

  public String id;

  @JsonIgnore public Boolean deleted; // We do not remove anything but mark deleted

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "header_id")
  public MessageHeader header;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "body2_id")
  public MessageBody body;

  @NotNull
  @Column(nullable = false)
  private Instant timestamp;

  private String author;

  public String getAuthor(){
    return author;
  }
  public void setAuthor(String value) {
    author = value;
  }

  /** @return Message id without trailing numbers" */
  @JsonIgnore
  public String getBaseMessageId() {
    if (id.matches("^.+?\\d$")) {
      return id.substring(0, id.lastIndexOf("."));
    }
    return id;
  }

  public Integer getGlobalId() {
    return globalId;
  }

  @NotNull @ManyToOne @JsonIgnore public MemberChat chat;

  @Transient @JsonIgnore public HedvigDataType expectedType;

  public Message() {
    header = new MessageHeader();
  }

  public void render(UserContext userContext) {
    this.body.render(userContext);
  }
}
