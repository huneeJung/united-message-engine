package com.message.unitedmessageengine.core.worker;


import com.message.unitedmessageengine.core.worker.first.service.SenderService;
import com.message.unitedmessageengine.entity.MessageEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerScheduler {

    private final SenderService senderService;
    @Value("${agentA.useYN}")
    private String useYN;
    @Value("${worker.fetch-count:2000}")
    private Integer fetchCount;

    //TODO 레디스 캐시로 구현하는 것이 더 안정적인 방식같아보임
    private ArrayBlockingQueue<MessageEntity> fetchMessageQueue;

    @PostConstruct
    public void init() {
        fetchMessageQueue = new ArrayBlockingQueue<>(fetchCount);
    }

    @SchedulerLock(name = "fetch_lock")
    @Scheduled(initialDelayString = "1000", fixedDelayString = "1000")
    public void selectMessage() {
        if (useYN.equals("N")) return;
        if (!fetchMessageQueue.isEmpty()) return;
        senderService.findAllMessages(fetchMessageQueue, "SLM", fetchCount);
    }

    @Scheduled(initialDelayString = "1000", fixedDelayString = "1000")
    public void sendMessage() {
        if (useYN.equals("N")) return;
        if (fetchMessageQueue.isEmpty()) return;
        log.info("[SENDER] 발송 처리 시작 ::: messageListSize {}", fetchMessageQueue.size());

        var start = Instant.now();
        var cnt = 0;
        var fetchQueue = new ArrayDeque<MessageEntity>();
        while (!fetchMessageQueue.isEmpty() && cnt < fetchCount) {
            fetchQueue.add(fetchMessageQueue.poll());
            cnt++;
        }
        senderService.send(fetchQueue);
        var end = Instant.now();

        log.info("[SENDER] 발송 처리 종료 ::: messageListSize {}, WORKING_TIME {}ms,",
                fetchMessageQueue.size(), Duration.between(start, end).toMillis());
    }

    @SchedulerLock(name = "anomaly_lock")
    @Scheduled(initialDelayString = "900000", fixedDelayString = "900000")
    public void resendAnomalyMessage() {
        if (useYN.equals("N")) return;
        log.info("[DETECTOR] 이상 메시지 재처리 시작 ::: messageListSize {}", fetchMessageQueue.size());

        var start = Instant.now();
        senderService.updateAnomalyMessages();
        var end = Instant.now();

        log.info("[DETECTOR] 이상 메시지 재처리 종료 ::: messageListSize {}, WORKING_TIME {}ms,",
                fetchMessageQueue.size(), Duration.between(start, end).toMillis());
    }

}
