package com.hedvig.botService.enteties.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hedvig.botService.enteties.UserContext;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("singleSelect")
@ToString
public class MessageBodySingleSelect extends MessageBody {

  public ArrayList<SelectItem> choices = new ArrayList<SelectItem>();

  public MessageBodySingleSelect(String content, List<SelectItem> items) {
    super(content);
    this.choices.addAll(items);
  }

  public MessageBodySingleSelect(String content, SelectItem... items){
    super(content);
    this.choices.addAll(Arrays.asList(items));
  }

  MessageBodySingleSelect() {}

  @JsonIgnore
  public SelectItem getSelectedItem() {
    for (SelectItem o : this.choices) {
      if (o.selected) {
        return o;
      }
    }
    throw new RuntimeException(
        String.format(
            "No item selected for list: [%s]",
            this.choices.stream().map(SelectItem::toString).collect(Collectors.joining(","))));
  }

  @Override
  public void render(UserContext userContext) {
    choices.forEach(x -> x.render(userContext));

    super.render(userContext);
  }

  public boolean removeItemIf(Predicate<? super SelectItem> predicate) {
    return choices.removeIf(predicate);
  }
}
