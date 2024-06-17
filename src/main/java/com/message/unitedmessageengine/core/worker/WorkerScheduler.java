package com.message.unitedmessageengine.core.worker;


import com.message.unitedmessageengine.core.worker.ack.service.AckService;
import com.message.unitedmessageengine.core.worker.distributer.service.DistributeService;
import com.message.unitedmessageengine.core.worker.fetcher.service.FetcherService;
import com.message.unitedmessageengine.core.worker.result.service.ResultService;
import com.message.unitedmessageengine.core.worker.sender.service.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.message.unitedmessageengine.core.queue.QueueManager.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerScheduler {

    private final FetcherService fetcherService;
    private final DistributeService distributeService;
    private final SenderService senderService;
    private final AckService ackService;
    private final ResultService resultService;

    //    @Scheduled(fixedRateString = "1000L")
    public void offerElementFromQueue() {
        log.info("[패치 큐 DB 폴링 시작] ::: FETCH_QUEUE_SIZE {}", FETCH_QUEUE.size());

        var start = Instant.now();
        var isStart = fetcherService.existElement();
        if (isStart) fetcherService.offerElement();
        var end = Instant.now();

        log.info("[패치 큐 DB 폴링 종료] ::: FETCH_QUEUE_SIZE {} , WORKING_TIME {}ms",
                FETCH_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    //    @Scheduled(fixedRateString = "1000L")
    public void distributeFetchListFromQueue() {
        log.info("[패치 큐 분배 시작] ::: MESSAGE_QUEUE_SIZE {} , KAKAO_QUEUE_SIZE {}",
                MESSAGE_QUEUE.size(), KAKAO_QUEUE.size());

        var start = Instant.now();
        distributeService.distributeElement();
        var end = Instant.now();

        log.info("[패치 큐 분배 종료] ::: MESSAGE_QUEUE_SIZE {} , KAKAO_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                MESSAGE_QUEUE.size(), KAKAO_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    //    @Scheduled(fixedRateString = "1000L")
    public void sendMessageFromQueue() {
        log.info("[메시지 발송 시작] ::: MESSAGE_QUEUE_SIZE {} , KAKAO_QUEUE_SIZE {}",
                MESSAGE_QUEUE.size(), KAKAO_QUEUE.size());

        var start = Instant.now();
        distributeService.distributeElement();
        var end = Instant.now();

        log.info("[메시지 발송 종료] ::: MESSAGE_QUEUE_SIZE {} , KAKAO_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                MESSAGE_QUEUE.size(), KAKAO_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    //    @Scheduled(fixedRateString = "1000L")
    public void sendKakaoFromQueue() {
        log.info("[카카오 발송 시작] ::: KAKAO_QUEUE_SIZE {}", KAKAO_QUEUE.size());

        var start = Instant.now();
        senderService.send();
        var end = Instant.now();

        log.info("[카카오 발송 종료] ::: KAKAO_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                KAKAO_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    //    @Scheduled(fixedRateString = "1000L")
    public void processAck() {
        log.info("[ACK 처리 시작] ::: KAKAO_QUEUE_SIZE {}", ACK_QUEUE.size());

        var start = Instant.now();
        ackService.processAck();
        var end = Instant.now();

        log.info("[ACK 처리 종료] ::: KAKAO_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                ACK_QUEUE.size(), Duration.between(start, end).toMillis());
    }

    //    @Scheduled(fixedRateString = "1000L")
    public void processResult() {
        log.info("[RESULT 처리 시작] ::: KAKAO_QUEUE_SIZE {}", RESULT_QUEUE.size());

        var start = Instant.now();
        resultService.processResult();
        var end = Instant.now();

        log.info("[RESULT 처리 종료] ::: KAKAO_QUEUE_SIZE {}, WORKING_TIME {}ms,",
                RESULT_QUEUE.size(), Duration.between(start, end).toMillis());
    }

}
