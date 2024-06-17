package com.message.unitedmessageengine.core.worker.fetcher.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class FetchRepositoryImpl implements FetcherRepository {
    @Override
    public boolean existElement() {
        return false;
    }

    @Override
    public List<Map<String, Object>> getFetchList(Integer fetchSize) {
        return null;
    }

    @Override
    public void updateBatchFetchList(List<Map<String, Object>> fetchList) {

    }
}
