package com.message.unitedmessageengine.core.kafka;

import com.message.unitedmessageengine.core.first.service.SenderService;
import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final SenderService senderService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${agentA.useYN}")
    private String useYN;
    @Value("${worker.fetch-count:5000}")
    private Integer fetchCount;
    @Value("${topic.name:sendMessageEvent}")
    private String topic;

    @Transactional
    @SchedulerLock(name = "fetch_lock", lockAtLeastFor = "1s", lockAtMostFor = "20s")
    @Scheduled(initialDelayString = "1000", fixedDelayString = "1000")
    public void fetch() {
        if (useYN.equals("N")) return;
        log.info("TEST1");
        var messageList = senderService.findAllMessages("SLM", fetchCount);
        for (MessageEntity message : messageList) {
            try {
                kafkaTemplate.send(topic, message);
            } catch (Exception e) {
                message.setStatusCode("W");
            }
        }
    }

}