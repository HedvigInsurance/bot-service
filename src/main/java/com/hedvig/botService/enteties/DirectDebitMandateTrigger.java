package com.hedvig.botService.enteties;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
public class DirectDebitMandateTrigger implements Serializable {

  public enum TriggerStatus {
    CRATED,
    IN_PROGRESS,
    FAILED,
    SUCCESS
  }

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid", updatable = false)
  UUID id;

  String firstName;
  String lastName;
  String email;
  String ssn;

  String url;

  @NotNull String memberId;

  String orderId;

  TriggerStatus status;
}
