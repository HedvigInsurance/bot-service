package com.hedvig.botService.web;

import com.hedvig.botService.enteties.SaveFile;
import com.hedvig.botService.enteties.SaveFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hedvig.botService.web.dto.FileUploadDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileUploadController {

  private final SaveFileRepository saveFileRepo;

  @Autowired
  public FileUploadController(SaveFileRepository saveFile) { this.saveFileRepo = saveFile; }

  @GetMapping("id/{id}")

    public FileUploadDTO getFile(@PathVariable UUID id) {
      SaveFile saveFile = saveFileRepo.findOne(id);
      FileUploadDTO fileUploadDTO = new FileUploadDTO(saveFile.getId(), saveFile.getFileUploadKey(), saveFile.getTimestamp(), saveFile.getMimeType(), saveFile.getMemberId());
      return fileUploadDTO;
  }

  @GetMapping("memberId/{id}")
  public List<FileUploadDTO> getFilesForMember(@PathVariable String id) {
    List<SaveFile> memberFiles = saveFileRepo.findByMemberId(id);
    return memberFiles.stream()
      .map(saveFile -> new FileUploadDTO(saveFile.getId(), saveFile.getFileUploadKey(), saveFile.getTimestamp(), saveFile.getMimeType(), saveFile.getMemberId()))
      .collect(Collectors.toList());
  }
}
