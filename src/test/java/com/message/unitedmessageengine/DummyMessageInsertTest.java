package com.message.unitedmessageengine;

import com.message.unitedmessageengine.core.worker.sender.repository.SenderRepository;
import com.message.unitedmessageengine.entity.Message;
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
public class DummyMessageInsertTest {


    @Autowired
    private SenderRepository senderRepository;

    @Test
    public void dummyInsertTen() {
        for (int i = 0; i < 10; i++) {
            var message = Message.builder()
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
            senderRepository.save(message);
        }
    }

    @Test
    public void dummyInsertThousand() {
        List<Message> list = new ArrayList<>();
        for (int i = 1000; i < 2000; i++) {
            var message = Message.builder()
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
        senderRepository.batchInsert(list);
    }

    @Test
    public void dummyInsertBig() {
        List<Message> list = new ArrayList<>();
        for (int i = 10000; i < 20000; i++) {
            var message = Message.builder()
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
        senderRepository.batchInsert(list);
    }

    @Test
    public void dummyInsertMoreBig() {
        List<Message> list = new ArrayList<>();
        for (int i = 100000; i < 200000; i++) {
            var message = Message.builder()
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
                senderRepository.batchInsert(list);
                list = new ArrayList<>();
            }
        }
    }

}
