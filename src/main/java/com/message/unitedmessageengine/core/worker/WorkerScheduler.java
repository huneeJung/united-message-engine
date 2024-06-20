package com.message.unitedmessageengine.core.worker;


import com.message.unitedmessageengine.core.worker.result.service.ResultService;
import com.message.unitedmessageengine.core.worker.sender.service.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static com.message.unitedmessageengine.core.queue.QueueManager.ACK_QUEUE;
import static com.message.unitedmessageengine.core.queue.QueueManager.RESULT_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerScheduler {

    private final SenderService senderService;
    private final ResultService resultService;

    @Value("${worker.fetch-count:1000}")
    private Integer fetchCount;

    @Value("${worker.update-count:1000}")
    private Integer updateSize;

    //    @Async
    @Transactional
    @Scheduled(initialDelayString = "2000", fixedDelayString = "1000")
    public void sendMessageFromQueue() {
        var fetchList = senderService.findAllMessages("SLM", fetchCount);
        if (fetchList.isEmpty()) {
            log.info("SENDER");
            return;
        }
        log.info("[SENDER] 발송 처리 시작 ::: messageListSize {}", fetchList.size());

        var start = Instant.now();
        senderService.send(fetchList);
        var end = Instant.now();

        log.info("[SENDER] 발송 처리 종료 ::: messageListSize {}, WORKING_TIME {}ms,",
                fetchList.size(), Duration.between(start, end).toMillis());
    }

    @Transactional
    @Scheduled(initialDelayString = "2000", fixedDelayString = "10000")
    public void processAck() {
        if (ACK_QUEUE.isEmpty()) {
            return;
        }
        log.info("[ACK] 결과 처리 시작 ::: RESULT_QUEUE_SIZE {}", ACK_QUEUE.size());

        var start = Instant.now();
        resultService.processAck(updateSize);
        var end = Instant.now();

        log.info("[ACK] 결과 처리 종료 ::: RESULT_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                ACK_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    @Transactional
    @Scheduled(initialDelayString = "2000", fixedDelayString = "5000")
    public void processResult() {
        if (RESULT_QUEUE.isEmpty()) {
            return;
        }
        log.info("[RESULT] 결과 처리 시작 ::: RESULT_QUEUE_SIZE {}", RESULT_QUEUE.size());

        var start = Instant.now();
        resultService.processResult(updateSize);
        var end = Instant.now();

        log.info("[RESULT] 결과 처리 종료 ::: RESULT_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                RESULT_QUEUE.size(), Duration.between(start, end).toMillis());
    }

}
