//package com.hedvig.botService.enteties;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import java.time.Instant;
//import java.util.UUID;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//
//public class SaveFile {
//  @Id
//  @GeneratedValue(strategy = GenerationType.AUTO)
//  @Column( updatable = false, nullable = false)
//  UUID id;
//  String fileUploadKey;
//  Instant timestamp;
//  String mimeType;
//  @NotNull String memberId;
//
//  public SaveFile(String fileUploadKey, String memberId, Instant timestamp, String mimeType) {
//    this.id = UUID.randomUUID();
//    this.fileUploadKey = fileUploadKey;
//    this.memberId = memberId;
//    this.timestamp = timestamp;
//    this.mimeType = mimeType;
//  }
//}
//

