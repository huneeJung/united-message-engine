package com.message.unitedmessageengine.core.worker.first.result.service;

import com.message.unitedmessageengine.core.socket.manager.first.FirstChannelManager;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.first.result.dto.AckDto;
import com.message.unitedmessageengine.core.worker.first.result.dto.ResultDto;
import com.message.unitedmessageengine.core.worker.first.result.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.message.unitedmessageengine.core.queue.QueueManager.ACK_QUEUE;
import static com.message.unitedmessageengine.core.queue.QueueManager.RESULT_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ResultService {

    private final FirstChannelManager channelManager;
    private final ResultRepository resultRepository;

    @Qualifier("firstTranslator")
    private final FirstTranslator translator;

    @Transactional
    public void processAck(Integer updateSize) {
        List<AckDto> batchList = new ArrayList<>();
        while (!ACK_QUEUE.isEmpty()) {
            var ackDto = ACK_QUEUE.poll();
            batchList.add(ackDto);
            if (batchList.size() >= updateSize) break;
//            log.info("[ACK] 결과 처리 완료 ::: messageId {}", ackDto.getMessageId());
        }
        resultRepository.batchUpdateAck(batchList);
    }

    @Transactional
    public void processResult(Integer updateSize) {
        List<ResultDto> batchList = new ArrayList<>();
        while (!RESULT_QUEUE.isEmpty()) {
            var resultDto = RESULT_QUEUE.poll();
            batchList.add(resultDto);
            if (batchList.size() >= updateSize) break;
//            log.info("[RESULT] 결과 처리 완료 ::: messageId {}", resultDto.getMessageId());
        }
        resultRepository.batchUpdateResult(batchList);
        log.info("[RESULT] 결과 처리 배치 완료 ::: batchSize {}", batchList.size());
    }

}
