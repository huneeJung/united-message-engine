package com.message.unitedmessageengine.core.kafka;

import com.message.unitedmessageengine.core.first.service.SenderService;
import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.message.unitedmessageengine.config.KafkaConfig.MESSAGE_CONSUMER_BEAN_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SenderService senderService;

    @KafkaListener(topics = "sendMessageEvent", containerFactory = MESSAGE_CONSUMER_BEAN_NAME)
    public void listener(MessageEntity message, Acknowledgment acknowledgment) {

        log.info("[SENDER] 발송 처리 시작 ::: messageId {}", message.getMessageId());

        var start = Instant.now();
        try {
            senderService.send(message);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            acknowledgment.nack(Duration.ofMillis(1000));
        }
        var end = Instant.now();

        log.info("[SENDER] 발송 처리 종료 ::: messageId {}, WORKING_TIME {}ms,",
                message.getMessageId(), Duration.between(start, end).toMillis());
    }

}
