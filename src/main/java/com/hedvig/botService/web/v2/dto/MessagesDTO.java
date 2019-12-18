package com.hedvig.botService.web.v2.dto;

import com.hedvig.botService.enteties.message.Message;

import java.util.List;

public final class MessagesDTO {

  @java.beans.ConstructorProperties({"state", "messages", "fabOptions"})
  public MessagesDTO(State state, List<Message> messages, List<FABOption> fabOptions) {
    this.state = state;
    this.messages = messages;
    this.fabOptions = fabOptions;
  }

  public State getState() {
    return this.state;
  }

  public List<Message> getMessages() {
    return this.messages;
  }

  public List<FABOption> getFabOptions() {
    return this.fabOptions;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof MessagesDTO)) return false;
    final MessagesDTO other = (MessagesDTO) o;
    final Object this$state = this.getState();
    final Object other$state = other.getState();
    if (this$state == null ? other$state != null : !this$state.equals(other$state)) return false;
    final Object this$messages = this.getMessages();
    final Object other$messages = other.getMessages();
    if (this$messages == null ? other$messages != null : !this$messages.equals(other$messages)) return false;
    final Object this$fabOptions = this.getFabOptions();
    final Object other$fabOptions = other.getFabOptions();
    if (this$fabOptions == null ? other$fabOptions != null : !this$fabOptions.equals(other$fabOptions))
      return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $state = this.getState();
    result = result * PRIME + ($state == null ? 43 : $state.hashCode());
    final Object $messages = this.getMessages();
    result = result * PRIME + ($messages == null ? 43 : $messages.hashCode());
    final Object $fabOptions = this.getFabOptions();
    result = result * PRIME + ($fabOptions == null ? 43 : $fabOptions.hashCode());
    return result;
  }

  public String toString() {
    return "MessagesDTO(state=" + this.getState() + ", messages=" + this.getMessages() + ", fabOptions=" + this.getFabOptions() + ")";
  }

  public static final class State {
    private final boolean ongoingClaim;
    private final boolean showOfferScreen;
    private final boolean onboardingDone;

    @java.beans.ConstructorProperties({"ongoingClaim", "showOfferScreen", "onboardingDone"})
    public State(boolean ongoingClaim, boolean showOfferScreen, boolean onboardingDone) {
      this.ongoingClaim = ongoingClaim;
      this.showOfferScreen = showOfferScreen;
      this.onboardingDone = onboardingDone;
    }

    public boolean isOngoingClaim() {
      return this.ongoingClaim;
    }

    public boolean isShowOfferScreen() {
      return this.showOfferScreen;
    }

    public boolean isOnboardingDone() {
      return this.onboardingDone;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof State)) return false;
      final State other = (State) o;
      if (this.isOngoingClaim() != other.isOngoingClaim()) return false;
      if (this.isShowOfferScreen() != other.isShowOfferScreen()) return false;
      if (this.isOnboardingDone() != other.isOnboardingDone()) return false;
      return true;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = result * PRIME + (this.isOngoingClaim() ? 79 : 97);
      result = result * PRIME + (this.isShowOfferScreen() ? 79 : 97);
      result = result * PRIME + (this.isOnboardingDone() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "MessagesDTO.State(ongoingClaim=" + this.isOngoingClaim() + ", showOfferScreen=" + this.isShowOfferScreen() + ", onboardingDone=" + this.isOnboardingDone() + ")";
    }
  }

  public static final class FABOption {
    private final String text;
    private final String triggerUrl;
    private final boolean enabled;

    @java.beans.ConstructorProperties({"text", "triggerUrl", "enabled"})
    public FABOption(String text, String triggerUrl, boolean enabled) {
      this.text = text;
      this.triggerUrl = triggerUrl;
      this.enabled = enabled;
    }

    public String getText() {
      return this.text;
    }

    public String getTriggerUrl() {
      return this.triggerUrl;
    }

    public boolean isEnabled() {
      return this.enabled;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof FABOption)) return false;
      final FABOption other = (FABOption) o;
      final Object this$text = this.getText();
      final Object other$text = other.getText();
      if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
      final Object this$triggerUrl = this.getTriggerUrl();
      final Object other$triggerUrl = other.getTriggerUrl();
      if (this$triggerUrl == null ? other$triggerUrl != null : !this$triggerUrl.equals(other$triggerUrl))
        return false;
      if (this.isEnabled() != other.isEnabled()) return false;
      return true;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $text = this.getText();
      result = result * PRIME + ($text == null ? 43 : $text.hashCode());
      final Object $triggerUrl = this.getTriggerUrl();
      result = result * PRIME + ($triggerUrl == null ? 43 : $triggerUrl.hashCode());
      result = result * PRIME + (this.isEnabled() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "MessagesDTO.FABOption(text=" + this.getText() + ", triggerUrl=" + this.getTriggerUrl() + ", enabled=" + this.isEnabled() + ")";
    }
  }

  private final State state;

  private final List<Message> messages;
  private final List<FABOption> fabOptions;
}
