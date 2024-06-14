package com.message.unitedmessageengine.core.worker.fetcher.service;

import com.message.unitedmessageengine.core.translater.TranslateRouter;
import com.message.unitedmessageengine.core.worker.fetcher.repository.FetcherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.message.unitedmessageengine.core.queue.QueueManager.FETCH_QUEUE;
import static com.message.unitedmessageengine.core.queue.QueueManager.fetchQueueSizeLimit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class FetcherService {

    private final TranslateRouter translateRouter;
    private final FetcherRepository fetcherRepository;

    // TODO 사이즈 지정
    @Value("")
    private Integer fetchSize;

    public boolean existElement() {
        return fetcherRepository.existElement();
    }

    @Transactional
    public void offerElement() {
        if (FETCH_QUEUE.size() + fetchSize > fetchQueueSizeLimit) {
            log.warn("[패치 큐 사이즈 초과 위험] ::: FETCH_QUEUE_SIZE {}", FETCH_QUEUE.size());
            return;
        }
        var fetchList = fetcherRepository.getFetchList(fetchSize);
        fetcherRepository.updateBatchFetchList(fetchList);

        FETCH_QUEUE.addAll(fetchList);
    }

}
