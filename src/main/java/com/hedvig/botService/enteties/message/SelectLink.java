package com.hedvig.botService.enteties.message;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.libs.translations.Translations;
import lombok.ToString;

@ToString
public class SelectLink extends SelectItem {

  static final long serialVersionUID = 1L;

  public SelectLink(
    String text, String value, String view, String appUrl, String webUrl, boolean selected) {
    super(selected, text, value);
    this.view = view;
    this.appUrl = appUrl;
    this.webUrl = webUrl;
  }

  public SelectLink() {
  } // NOTE! All objects need to have a default constructor in order for Jackson to
  // marshall.

  public static SelectLink toDashboard(String text, String value) {
    return new SelectLink(text, value, "Dashboard", null, null, false);
  }

  public static SelectLink toOffer(String text, String value) {
    return new SelectLink(text, value, "Offer", null, null, false);
  }

//  public static SelectLink closeChat(String text, String value) {  // TODO: add this option when iOS can handle closeChat "link"
//    return new SelectLink(text, value, "CloseChat", null, null, false);
//  }

  public String view;
  public String appUrl;
  public String webUrl;

  @Override
  public void render(String id, final UserContext context, Translations translations) {
    super.render(id, context, translations);

    if (this.appUrl != null) {
      this.appUrl = context.replaceWithContext(this.appUrl);
    }
    if (this.webUrl != null) {
      this.webUrl = context.replaceWithContext(this.webUrl);
    }
  }
}
