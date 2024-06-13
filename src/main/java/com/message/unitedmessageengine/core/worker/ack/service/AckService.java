package com.message.unitedmessageengine.core.worker.ack.service;

import com.message.unitedmessageengine.core.worker.ack.repository.AckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

import static com.message.unitedmessageengine.core.queue.QueueManager.ACK_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AckService {

    private final AckRepository ackRepository;

    @Value("")
    private Integer ackUpdateSize;

    @Transactional
    // TODO : ACK 배치 처리
    public void processAck() {
        var count = 0;
        var ackList = new ArrayList<Map<String, Object>>(ackUpdateSize);
        while (!ACK_QUEUE.isEmpty() && count < ackUpdateSize) {
            ackList.add(ACK_QUEUE.poll());
        }
        ackRepository.updateBatchAck(ackList);
    }

}
