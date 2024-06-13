package com.message.unitedmessageengine.core.worker.result.service;

import com.message.unitedmessageengine.core.worker.result.repository.ResultRepository;
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
public class ResultService {

    private final ResultRepository resultRepository;

    @Value("")
    private Integer resultUpdateSize;

    @Transactional
    // TODO RESULT 배치 처리
    public void processResult() {
        var count = 0;
        var resultList = new ArrayList<Map<String, Object>>(resultUpdateSize);
        while (!ACK_QUEUE.isEmpty() && count < resultUpdateSize) {
            resultList.add(ACK_QUEUE.poll());
        }
        resultRepository.updateBatchResult(resultList);
    }

}
