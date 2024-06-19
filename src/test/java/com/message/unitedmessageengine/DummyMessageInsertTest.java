package com.message.unitedmessageengine;

import com.message.unitedmessageengine.core.worker.sender.repository.SenderRepository;
import com.message.unitedmessageengine.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
public class DummyMessageInsertTest {


    @Autowired
    private SenderRepository senderRepository;

    @Test
    public void dummyInsert() {
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

}
