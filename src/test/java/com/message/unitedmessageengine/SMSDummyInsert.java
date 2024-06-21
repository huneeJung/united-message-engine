package com.message.unitedmessageengine;

import com.message.unitedmessageengine.entity.MessageEntity;
import com.message.unitedmessageengine.sample.DummyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
public class SMSDummyInsert {


    @Autowired
    private DummyRepository dummyRepository;

    // 10건
    @Test
    public void dummyInsertTen() {
        for (int i = 0; i < 10; i++) {
            var message = MessageEntity.builder()
                    .messageId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .content("TEST")
                    .fromNumber("01046176166")
                    .regDtt(LocalDateTime.now())
                    .sendDtt(LocalDateTime.now())
                    .serviceDivision("SLM")
                    .serviceType("SMS")
                    .statusCode("W")
                    .toNumber("01912341234")
                    .build();
            dummyRepository.save(message);
        }
    }

    // 1000건
    @Test
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
                    .serviceType("SMS")
                    .statusCode("W")
                    .toNumber("0191234" + i)
                    .build();
            list.add(message);
        }
        dummyRepository.batchInsertMessage(list);
    }

    // 10000건
    @Test
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
                    .serviceType("SMS")
                    .statusCode("W")
                    .toNumber("019123" + i)
                    .build();
            list.add(message);
        }
        dummyRepository.batchInsertMessage(list);
    }

    // 100000건
    @Test
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
                    .serviceType("SMS")
                    .statusCode("W")
                    .toNumber("01912" + i)
                    .build();
            list.add(message);
            if (list.size() == 5000) {
                dummyRepository.batchInsertMessage(list);
                list = new ArrayList<>();
            }
        }
    }

}
