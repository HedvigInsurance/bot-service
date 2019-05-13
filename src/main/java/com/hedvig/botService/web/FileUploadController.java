package com.hedvig.botService.web;

import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.MemberChatRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hedvig.botService.web.dto.FileUploadDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileUploadController {

  private final MemberChatRepository memberChatRepository;

  @Autowired
  public FileUploadController(MemberChatRepository memberChatRepository) {
    this.memberChatRepository = memberChatRepository;
  }

  @GetMapping("memberId/{id}")
  public List<FileUploadDTO> getFilesForMember(@PathVariable String id) {

    List<FileUploadDTO> fileUploadDTO = new ArrayList<>();

    Optional<MemberChat> memberChat = memberChatRepository.findByMemberId(id);

    if(!memberChat.isPresent())
      return fileUploadDTO;

    List<Message> messages = memberChat.get().chatHistory;
    List<Message> fileUploadMessages;

    fileUploadMessages = messages.stream()
      .filter(message -> message.body.getClass().equals(MessageBodyFileUpload.class))
      .collect(Collectors.toList());

      fileUploadDTO = fileUploadMessages.stream()
        .map(message -> new FileUploadDTO(
          ((MessageBodyFileUpload)message.getBody()).key,
          message.getTimestamp(),
          ((MessageBodyFileUpload)message.getBody()).mimeType,
          message.getChat().getMemberId()
        ))
        .collect(Collectors.toList());

      return fileUploadDTO;
  }
}
