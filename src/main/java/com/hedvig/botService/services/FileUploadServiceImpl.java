package com.hedvig.botService.services;

import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.MemberChatRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import com.hedvig.botService.web.dto.FileUploadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FileUploadServiceImpl implements FileUploadService {

  private final MemberChatRepository memberChatRepository;

  @Autowired
  public FileUploadServiceImpl(MemberChatRepository memberChatRepository) {
    this.memberChatRepository = memberChatRepository;
  }

  @Override
  public List<FileUploadDTO> getFileUploadDTOs(String id) {
    Optional<MemberChat> memberChat = memberChatRepository.findByMemberId(id);

    if(!memberChat.isPresent()) {
      return new ArrayList<>();
    }

    List<Message> messages = memberChat.get().chatHistory;
    List<Message> fileUploadMessages;

    fileUploadMessages = messages.stream()
      .filter(message -> message.body.getClass().equals(MessageBodyFileUpload.class))
      .collect(Collectors.toList());

    List <FileUploadDTO> fileUploadDTO = fileUploadMessages.stream()
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
