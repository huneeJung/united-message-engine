package com.message.unitedmessageengine.core.worker.ack.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AckRepositoryImpl implements AckRepository {
    @Override
    public void updateBatchAck(List<Map<String, Object>> ackDataList) {

    }
}
