package com.message.unitedmessageengine;

import com.message.unitedmessageengine.entity.MessageEntity;
import com.message.unitedmessageengine.entity.MessageImageEntity;
import com.message.unitedmessageengine.sample.DummyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
public class MMSDummyInsert {


    @Autowired
    private DummyRepository dummyRepository;

    // 10건
    @Test
    @Commit
    @Transactional
    public void dummyInsertTen() {
        for (int i = 0; i < 10; i++) {
            var message = MessageEntity.builder()
                    .messageId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .content("TEST")
                    .fromNumber("01046176166")
                    .regDtt(LocalDateTime.now())
                    .sendDtt(LocalDateTime.now())
                    .serviceDivision("SLM")
                    .serviceType("MMS")
                    .statusCode("W")
                    .toNumber("01912341234")
                    .build();
            dummyRepository.save(message);
        }

        List<MessageImageEntity> messageImageEntityList = new ArrayList<>();
        for (MessageEntity message : dummyRepository.findAll()) {
            var imageEntity = MessageImageEntity.builder()
                    .imageName("mms.jpg")
                    .imagePath("/Users/mz01-junghunee/Desktop/mms2.jpg")
                    .message(message)
                    .build();
            messageImageEntityList.add(imageEntity);
        }
        dummyRepository.batchInsertImage(messageImageEntityList);
    }

    // 1000건
    @Test
    @Commit
    @Transactional
    public void dummyInsertThousand() {
        List<MessageEntity> list = new ArrayList<>();
        for (int i = 1000; i < 2000; i++) {
            var message = MessageEntity.builder()
                    .messageId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .content("TEST")
                    .fromNumber("01046176166")
                    .regDtt(LocalDateTime.now())
                    .sendDtt(LocalDateTime.now())
                    .serviceDivision("SLM")
                    .serviceType("MMS")
                    .statusCode("W")
                    .toNumber("0191234" + i)
                    .build();
            list.add(message);
        }
        dummyRepository.batchInsertMessage(list);

        List<MessageImageEntity> messageImageEntityList = new ArrayList<>();
        for (MessageEntity message : dummyRepository.findAll()) {
            var imageEntity = MessageImageEntity.builder()
                    .imageName("mms.jpg")
                    .imagePath("/Users/mz01-junghunee/Desktop/mms2.jpg")
                    .message(message)
                    .build();
            messageImageEntityList.add(imageEntity);
        }
        dummyRepository.batchInsertImage(messageImageEntityList);

    }

    // 10000건
    @Test
    @Commit
    @Transactional
    public void dummyInsertBig() {
        List<MessageEntity> list = new ArrayList<>();
        for (int i = 10000; i < 20000; i++) {
            var message = MessageEntity.builder()
                    .messageId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .content("TEST")
                    .fromNumber("01046176166")
                    .regDtt(LocalDateTime.now())
                    .sendDtt(LocalDateTime.now())
                    .serviceDivision("SLM")
                    .serviceType("MMS")
                    .statusCode("W")
                    .toNumber("019123" + i)
                    .build();
            list.add(message);
        }
        dummyRepository.batchInsertMessage(list);

        List<MessageImageEntity> messageImageEntityList = new ArrayList<>();
        for (MessageEntity message : dummyRepository.findAll()) {
            var imageEntity = MessageImageEntity.builder()
                    .imageName("mms.jpg")
                    .imagePath("/Users/mz01-junghunee/Desktop/mms2.jpg")
                    .message(message)
                    .build();
            messageImageEntityList.add(imageEntity);
        }
        dummyRepository.batchInsertImage(messageImageEntityList);
    }

    // 100000건
    @Test
    @Commit
    @Transactional
    public void dummyInsertMoreBig() {
        List<MessageEntity> list = new ArrayList<>();
        for (int i = 100000; i < 200000; i++) {
            var message = MessageEntity.builder()
                    .messageId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .content("TEST")
                    .fromNumber("01046176166")
                    .regDtt(LocalDateTime.now())
                    .sendDtt(LocalDateTime.now())
                    .serviceDivision("SLM")
                    .serviceType("MMS")
                    .statusCode("W")
                    .toNumber("01912" + i)
                    .build();
            list.add(message);
            if (list.size() == 5000) {
                dummyRepository.batchInsertMessage(list);
                list = new ArrayList<>();
            }
        }

        List<MessageImageEntity> messageImageEntityList = new ArrayList<>();
        for (MessageEntity message : dummyRepository.findAll()) {
            var imageEntity = MessageImageEntity.builder()
                    .imageName("mms.jpg")
                    .imagePath("/Users/mz01-junghunee/Desktop/mms2.jpg")
                    .message(message)
                    .build();
            messageImageEntityList.add(imageEntity);
        }
        dummyRepository.batchInsertImage(messageImageEntityList);
    }

}
