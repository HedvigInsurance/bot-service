package com.hedvig.botService.enteties.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hedvig.botService.enteties.UserContext;
import java.io.Serializable;
import lombok.ToString;

/*
 * A select item is a super type for everything one can put in a list of options
 * An "option" triggers a post and a "link" triggers a load of a screen on the client side
 * */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = SelectOption.class, name = "selection"),
  @JsonSubTypes.Type(value = SelectLink.class, name = "link"),
  @JsonSubTypes.Type(value = SelectItemTrustly.class, name = "trustly")
})
@ToString
public class SelectItem implements Serializable {

  static final long serialVersionUID = 1L;

  public boolean selected;
  public String text;
  public String value;

  public SelectItem() {}

  public SelectItem(boolean selected, String text, String value) {
    this.selected = selected;
    this.text = text;
    this.value = value;
  }

  public void render(UserContext userContext) {
    // this.text = userContext.replaceWithContext(this.text);
  }
}
