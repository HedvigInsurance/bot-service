package com.hedvig.botService.web;

import com.hedvig.botService.enteties.MemberChat;
import com.hedvig.botService.enteties.MemberChatRepository;
import com.hedvig.botService.enteties.message.Message;
import com.hedvig.botService.enteties.message.MessageBody;
import com.hedvig.botService.enteties.message.MessageBodyFileUpload;
import com.hedvig.botService.web.dto.FileUploadDTO;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Optional;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
public class FileUploadControllerTest {
  @MockBean private MemberChatRepository memberChatRepository;

  private FileUploadController fileUploadController;

  @Before
  public void before() {
    fileUploadController = new FileUploadController(memberChatRepository);
  }

  @Test
  public void testFileUploadMessageShouldContainKeyTimeStampMimeTypeAndMemberIDAsExpected() {
    MessageBody messageBody1 = new MessageBodyFileUpload("test", "image/png", "423");
    MessageBody messageBody2 = new MessageBodyFileUpload("other message body", "image2/png", "6435");

    Message message1 = new Message();
    Message message2 = new Message();

    message1.body = messageBody1;
    message2.body = messageBody2;

    MemberChat memberChat = new MemberChat("12345");

    memberChat.addToHistory(message1);
    memberChat.addToHistory(message2);

    Mockito.when(memberChatRepository.findByMemberId("12345")).thenReturn(Optional.of(memberChat));
    val testfiles = fileUploadController.getFilesForMember("12345");

    FileUploadDTO fileUpload1 = new FileUploadDTO(((MessageBodyFileUpload) messageBody1).key, message1.getTimestamp(), ((MessageBodyFileUpload) messageBody1).mimeType, message1.chat.getMemberId());
    FileUploadDTO fileUpload2 = new FileUploadDTO(((MessageBodyFileUpload) messageBody2).key, message2.getTimestamp(), ((MessageBodyFileUpload) messageBody2).mimeType, message2.chat.getMemberId());

    assertTrue(testfiles.contains(fileUpload1));
    assertTrue(testfiles.contains(fileUpload2));
  }

  @Test
  public void testShouldReturnExpectedNumberOfFileUploadDTOS() {
    MessageBody messageBody1 = new MessageBodyFileUpload("test", "image/png", "423");
    MessageBody messageBody2 = new MessageBodyFileUpload("other message body", "image2/png", "6435");
    MessageBody messageBody3 = new MessageBody("not file upload");

    Message message1 = new Message();
    Message message2 = new Message();
    Message message3 = new Message();

    message1.body = messageBody1;
    message2.body = messageBody2;
    message3.body = messageBody3;

    MemberChat memberChat = new MemberChat("12345");

    memberChat.addToHistory(message1);
    memberChat.addToHistory(message2);
    memberChat.addToHistory(message3);

    Mockito.when(memberChatRepository.findByMemberId("12345")).thenReturn(Optional.of(memberChat));
    val testfiles = fileUploadController.getFilesForMember("12345");

    assertTrue(testfiles.size() == 2);
  }

  @Test
  public void testShouldReturnEmptyListIfNoMemberChatFound() {

    Mockito.when(memberChatRepository.findByMemberId(Mockito.anyString())).thenReturn(Optional.empty());
    val testfiles = fileUploadController.getFilesForMember("12345");

    assertEquals(Collections.emptyList(), testfiles);
  }

  @Test
  public void testShouldReturnAnEmptyListIfNoFileUploadsFound() {
    MessageBody messageBody1 = new MessageBody("other message body");
    MessageBody messageBody2 = new MessageBody("not file upload");

    Message message1 = new Message();
    Message message2 = new Message();

    message1.body = messageBody1;
    message2.body = messageBody2;

    MemberChat memberChat = new MemberChat("12345");

    memberChat.addToHistory(message1);
    memberChat.addToHistory(message2);

    Mockito.when(memberChatRepository.findByMemberId("12345")).thenReturn(Optional.of(memberChat));
    val testfiles = fileUploadController.getFilesForMember("12345");

    assertEquals(Collections.emptyList(), testfiles);
  }
}
