package com.message.unitedmessageengine.core.worker;


import com.message.unitedmessageengine.core.worker.first.sender.service.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerScheduler {

    private final SenderService senderService;

    @Value("${agentA.useYN}")
    private String useYN;

    @Value("${worker.fetch-count:1000}")
    private Integer fetchCount;
    
    @Scheduled(initialDelayString = "1000", fixedDelayString = "1")
    @SchedulerLock(name = "send_lock", lockAtLeastFor = "PT1S", lockAtMostFor = "PT1H")
    public void sendMessageFromQueue() {
        if (useYN.equals("N")) return;

        var start = Instant.now();
        var fetchList = senderService.findAllMessages("SLM", fetchCount);
        if (fetchList.isEmpty()) return;
        log.info("[SENDER] 발송 처리 시작 ::: messageListSize {}", fetchList.size());

        senderService.send(fetchList);
        var end = Instant.now();

        log.info("[SENDER] 발송 처리 종료 ::: messageListSize {}, WORKING_TIME {}ms,",
                fetchList.size(), Duration.between(start, end).toMillis());
    }

}
