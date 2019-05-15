package com.hedvig.botService.services;

import com.hedvig.botService.web.dto.FileUploadDTO;

import java.util.List;

public interface FileUploadService {
  public List<FileUploadDTO> getFileUploadDTOs(String id);
}

