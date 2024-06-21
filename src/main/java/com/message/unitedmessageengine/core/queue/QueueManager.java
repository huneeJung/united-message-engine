package com.message.unitedmessageengine.core.queue;

import com.message.unitedmessageengine.core.worker.first.result.dto.AckDto;
import com.message.unitedmessageengine.core.worker.first.result.dto.ResultDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueManager {

    public static final ArrayBlockingQueue<Map<String, Object>> FETCH_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<Map<String, Object>> MESSAGE_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<Map<String, Object>> KAKAO_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<AckDto> ACK_QUEUE = new ArrayBlockingQueue<>(100000);
    public static final ArrayBlockingQueue<ResultDto> RESULT_QUEUE = new ArrayBlockingQueue<>(100000);
    public static Integer fetchQueueSizeLimit;
    public static Integer messageQueueSizeLimit;
    public static Integer kakaoQueueSizeLimit;
    public static Integer ackQueueSizeLimit;
    public static Integer resultQueueSizeLimit;

    private final QueueProperties queueProperties;

    @PostConstruct
    public void init() {
        fetchQueueSizeLimit = queueProperties.getLimitFetchSize();
        messageQueueSizeLimit = queueProperties.getLimitMessageSize();
        kakaoQueueSizeLimit = queueProperties.getLimitKakaoSize();
        ackQueueSizeLimit = queueProperties.getLimitAckSize();
        resultQueueSizeLimit = queueProperties.getLimitResultSize();
    }

    @PreDestroy
    // TODO : 종료 시그널 감지시 처리 로직 구성
    private void stop() {
        if (!FETCH_QUEUE.isEmpty()) {

        }
        if (!MESSAGE_QUEUE.isEmpty()) {

        }
        if (!KAKAO_QUEUE.isEmpty()) {

        }
        if (!RESULT_QUEUE.isEmpty()) {

        }
    }
}
