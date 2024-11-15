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
        log.info("발송 시작 ::: messageId {}", message.getMessageId());
        var start = Instant.now();

        senderService.messageSend(message);
        acknowledgment.acknowledge();

        var end = Instant.now();
        log.info("발송 종료 ::: messageId {}, WORKING_TIME {}ms,",
                message.getMessageId(), Duration.between(start, end).toMillis());
    }

}
