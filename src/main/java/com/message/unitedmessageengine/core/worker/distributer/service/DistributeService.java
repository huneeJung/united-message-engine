package com.message.unitedmessageengine.core.worker.distributer.service;

import com.message.unitedmessageengine.core.worker.distributer.repository.DistributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.message.unitedmessageengine.core.queue.QueueManager.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DistributeService {

    private final DistributeRepository distributeRepository;

    public void distributeElement() {
        var fetchQueueSize = FETCH_QUEUE.size();
        while (!FETCH_QUEUE.isEmpty() && fetchQueueSize > 0) {

            var element = FETCH_QUEUE.poll();
            var type = element.get("TYPE");
            var id = element.get("ID");

            if (type.equals("KAKAO")) {
                if (KAKAO_QUEUE.size() >= kakaoQueueSizeLimit) {
                    log.warn("[카카오 큐 사이즈 초과] ::: KAKAO_QUEUE_SIZE {}", KAKAO_QUEUE.size());
                    FETCH_QUEUE.offer(element);
                } else {
                    log.info("[카카오 큐 삽입] ::: KAKAO_QUEUE_SIZE {}, ID {}", KAKAO_QUEUE.size(), id);
                    KAKAO_QUEUE.offer(element);
                }
            } else if (type.equals("MESSAGE")) {
                if (MESSAGE_QUEUE.size() >= messageQueueSizeLimit) {
                    log.warn("[메시지 큐 사이즈 초과] ::: MESSAGE_QUEUE_SIZE {}", MESSAGE_QUEUE.size());
                    FETCH_QUEUE.offer(element);
                } else {
                    log.info("[메시지 큐 삽입] ::: MESSAGE_QUEUE_SIZE {} , ID {}", MESSAGE_QUEUE.size(), id);
                    MESSAGE_QUEUE.offer(element);
                }
            } else {
                log.warn("[비정상 타입 감지] ::: ID {} , TYPE {}", id, type);
            }
            fetchQueueSize--;
        }
    }
}
