package com.message.unitedmessageengine.core.worker.result.service;

import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;
import com.message.unitedmessageengine.core.worker.result.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.message.unitedmessageengine.core.queue.QueueManager.RESULT_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ResultService {

    private final ResultRepository resultRepository;

    @Transactional
    public void processResult(Integer updateSize) {
        List<ResultDto> batchList = new ArrayList<>();
        while (!RESULT_QUEUE.isEmpty()) {
            var resultDto = RESULT_QUEUE.poll();
            batchList.add(resultDto);
            if (batchList.size() >= updateSize) break;
            log.info("[RESULT] 결과 처리 완료 ::: messageId {}", resultDto.getMessageId());
        }
        resultRepository.batchUpdate(batchList);
    }

}
