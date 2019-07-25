package com.hedvig.botService.web.v2.dto;

public final class RegisterPushTokenRequest {
  private final String token;

  @java.beans.ConstructorProperties({"token"})
  public RegisterPushTokenRequest(String token) {
    this.token = token;
  }

  public String getToken() {
    return this.token;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RegisterPushTokenRequest)) return false;
    final RegisterPushTokenRequest other = (RegisterPushTokenRequest) o;
    final Object this$token = this.getToken();
    final Object other$token = other.getToken();
    if (this$token == null ? other$token != null : !this$token.equals(other$token)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $token = this.getToken();
    result = result * PRIME + ($token == null ? 43 : $token.hashCode());
    return result;
  }

  public String toString() {
    return "RegisterPushTokenRequest(token=" + this.getToken() + ")";
  }
}
