package com.hedvig.botService.web.v2.dto;

public final class MarkAsReadRequest {
  private final Integer globalId;

  @java.beans.ConstructorProperties({"globalId"})
  public MarkAsReadRequest(Integer globalId) {
    this.globalId = globalId;
  }

  public Integer getGlobalId() {
    return this.globalId;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof MarkAsReadRequest)) return false;
    final MarkAsReadRequest other = (MarkAsReadRequest) o;
    final Object this$globalId = this.getGlobalId();
    final Object other$globalId = other.getGlobalId();
    if (this$globalId == null ? other$globalId != null : !this$globalId.equals(other$globalId)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $globalId = this.getGlobalId();
    result = result * PRIME + ($globalId == null ? 43 : $globalId.hashCode());
    return result;
  }

  public String toString() {
    return "MarkAsReadRequest(globalId=" + this.getGlobalId() + ")";
  }
}

