package com.hedvig.botService.web;

import com.hedvig.botService.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hedvig.botService.web.dto.FileUploadDTO;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileUploadController {

  private final FileUploadService fileUploadService;

  @Autowired
  public FileUploadController(FileUploadService fileUploadService) {
    this.fileUploadService = fileUploadService;
  }

  @GetMapping("memberId/{id}")
  public List<FileUploadDTO> getFilesForMember(@PathVariable String id) {
      return fileUploadService.getFileUploadDTOs(id);
  }
}
