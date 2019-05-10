package com.hedvig.botService.web;

import com.fasterxml.jackson.databind.ser.impl.MapEntrySerializer;
import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.MemberChatRepository;
//import com.hedvig.botService.enteties.SaveFile;
//import com.hedvig.botService.enteties.SaveFileRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import com.hedvig.botService.web.dto.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hedvig.botService.web.dto.FileUploadDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileUploadController {

//  private final SaveFileRepository saveFileRepo;
  private final MemberChatRepository memberChatRepository;

  @Autowired
  public FileUploadController(MemberChatRepository memberChatRepository) {
    this.memberChatRepository = memberChatRepository;
  }

//  @GetMapping("id/{id}")
//
//    public FileUploadDTO getFile(@PathVariable UUID id) {
//      SaveFile saveFile = saveFileRepo.findOne(id);
//      FileUploadDTO fileUploadDTO = new FileUploadDTO(saveFile.getId(), saveFile.getFileUploadKey(), saveFile.getTimestamp(), saveFile.getMimeType(), saveFile.getMemberId());
//      return fileUploadDTO;
//  }

  //    get our member chat for our member ID
  //     get a list of messages for that memberID (memberChat.chatHistory)
  //      iterate over these messages and check if the messagebody has a type of file upload (message.body?)
//      if it does - push into empty array list so you have a list of messages
  //      map over the list of messages


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

//  @GetMapping("memberId/{id}")
//  public List<FileUploadDTO> getFilesForMember(@PathVariable String id) {
//    List<SaveFile> memberFiles = saveFileRepo.findByMemberId(id);
//    return memberFiles.stream()
//      .map(saveFile -> new FileUploadDTO(saveFile.getId(), saveFile.getFileUploadKey(), saveFile.getTimestamp(), saveFile.getMimeType(), saveFile.getMemberId()))
//      .collect(Collectors.toList());
//  }

}
