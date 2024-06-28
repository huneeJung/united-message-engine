package com.message.unitedmessageengine.core.kafka;

import com.message.unitedmessageengine.core.first.service.SenderService;
import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SenderService senderService;

    @Value("${topic.name:sendMessageEvent}")
    private String topic;

    @KafkaListener(topics = "sendMessageEvent", groupId = "group_1")
    public void listener(MessageEntity message) {

        log.info("[SENDER] 발송 처리 시작 ::: messageId {}", message.getMessageId());

        var start = Instant.now();
        senderService.send(message);
        var end = Instant.now();

        log.info("[SENDER] 발송 처리 종료 ::: messageId {}, WORKING_TIME {}ms,",
                message.getMessageId(), Duration.between(start, end).toMillis());
    }

}
