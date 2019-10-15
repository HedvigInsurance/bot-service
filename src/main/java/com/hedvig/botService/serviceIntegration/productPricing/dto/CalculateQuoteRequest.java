package com.hedvig.botService.serviceIntegration.productPricing.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class CalculateQuoteRequest {

  private String memberId;
  private String ssn;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;

  private Boolean student;

  private Address address;

  private Float livingSpace;
  private String houseType;
  private String currentInsurer;
  private int personsInHouseHold;

  private List<SafetyIncreaserType> safetyIncreasers;

  private Integer ancillaryArea;
  private Integer yearOfConstruction;
  private Integer numberOfBathrooms;
  private List<ExtraBuilding> extraBuildings;
  private Boolean isSubleted;
}
