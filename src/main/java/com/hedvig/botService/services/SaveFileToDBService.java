//package com.hedvig.botService.services;
//
//import com.hedvig.botService.enteties.SaveFile;
//import com.hedvig.botService.enteties.SaveFileRepository;
//import com.hedvig.botService.services.events.FileUploadedEvent;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//import javax.persistence.Id;
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//public class SaveFileToDBService {
//  private SaveFileRepository saveFileRepository;
//
//  @Autowired
//  public SaveFileToDBService(SaveFileRepository saveFileRepository) {
//    this.saveFileRepository = saveFileRepository;
//  }
//
//  @EventListener
//  public void on(FileUploadedEvent e) {
//    final Instant timestamp = Instant.now();
//    final String memberId = e.getMemberId();
//    final String mimeType = e.getMimeType();
//    final String fileUploadKey = e.getKey();
//
//    SaveFile memberFile = new SaveFile(fileUploadKey, memberId, timestamp, mimeType);
//    saveFileRepository.save(memberFile);
//  }
//
//  public void getFiles(String memberId) {
//      List<SaveFile> files = saveFileRepository.findByMemberId(memberId);
//  }
//
//}
