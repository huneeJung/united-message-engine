package com.message.unitedmessageengine.core.queue;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
@Component
public class QueueManager {

    public static final ArrayBlockingQueue<Map<String, Object>> FETCH_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<Map<String, Object>> MESSAGE_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<Map<String, Object>> KAKAO_QUEUE = new ArrayBlockingQueue<>(1000);
    public static final ArrayBlockingQueue<Map<String, Object>> ACK_QUEUE = new ArrayBlockingQueue<>(5000);
    public static final ArrayBlockingQueue<Map<String, Object>> RESULT_QUEUE = new ArrayBlockingQueue<>(5000);

    public static Integer fetchQueueSizeLimit;
    public static Integer messageQueueSizeLimit;
    public static Integer kakaoQueueSizeLimit;
    public static Integer ackQueueSizeLimit;
    public static Integer resultQueueSizeLimit;

    static {
        fetchQueueSizeLimit = Integer.parseInt(System.getProperty(""));
        messageQueueSizeLimit = Integer.parseInt(System.getProperty(""));
        kakaoQueueSizeLimit = Integer.parseInt(System.getProperty(""));
        ackQueueSizeLimit = Integer.parseInt(System.getProperty(""));
        resultQueueSizeLimit = Integer.parseInt(System.getProperty(""));
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
        if (!ACK_QUEUE.isEmpty()) {

        }
        if (!RESULT_QUEUE.isEmpty()) {

        }
    }
}
