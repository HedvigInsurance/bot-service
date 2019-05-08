package com.hedvig.botService.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor

@Data
public class FileUploadDTO {
//    public UUID id;
    public String fileUploadKey;
    public Instant timestamp;
    public String mimeType;
    public String memberId;

}

