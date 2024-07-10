package com.message.unitedmessageengine.core.kafka;

import com.message.unitedmessageengine.core.first.service.SenderService;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${agentA.useYN}")
    private String useYN;
    @Value("${worker.fetch-count:5000}")
    private Integer fetchCount;
    @Value("${topic.name:sendMessageEvent}")
    private String topic;


    @Transactional
    @SchedulerLock(name = "fetch_lock")
    @Scheduled(initialDelayString = "1000", fixedDelayString = "1")
    public void fetch() {
        if (useYN.equals("N")) return;
        var messageList = senderService.findAllMessages("SLM", fetchCount);
        messageList.forEach((message) -> kafkaTemplate.send(topic, message));
    }

}