package com.message.unitedmessageengine.core.worker.fetcher.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FetcherRepository {

    boolean existElement();

    List<Map<String, Object>> getFetchList(Integer fetchSize);

    void updateBatchFetchList(List<Map<String, Object>> fetchList);
}
