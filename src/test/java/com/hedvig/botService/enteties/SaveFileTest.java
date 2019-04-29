//package com.hedvig.botService.enteties;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.time.Instant;
//
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertNotNull;
//
//@AutoConfigureMockMvc
//@RunWith(SpringRunner.class)
//
//@SpringBootTest
//public class SaveFileTest {
//  private static final Instant timestamp = Instant.now();
//  private static final String mimeType = "image/png";
//
//  private static final String file1fileUploadKey = "123a";
//  private static final String file1memberId = "testId";
//
//  private static final String file2fileUploadKey = "123a";
//  private static final String file2memberId = "testId";
//
//
//  @Autowired
//  private SaveFileRepository repository;
//
//  @Test
//  public void shouldCreateNewEntryInDB() {
//
//    SaveFile file1 = new SaveFile(file1fileUploadKey, file1memberId, timestamp, mimeType);
//    System.out.println(file1.fileUploadKey);
//    System.out.println(file1memberId);
//    System.out.println(timestamp);
//    System.out.println(mimeType);
//    System.out.println(file1);
//     file1 = repository.save(file1);
//     System.out.println(file1);
//
//    SaveFile file2 = new SaveFile(file2fileUploadKey, file2memberId, timestamp, mimeType);
//    System.out.println(file2);
//    file2 = repository.save(file2);
//
//    assertNotNull(file1.id);
//    assertNotNull(file2.id);
//    assertNotEquals(file1.id, file2.id);
//  }
//}
//



