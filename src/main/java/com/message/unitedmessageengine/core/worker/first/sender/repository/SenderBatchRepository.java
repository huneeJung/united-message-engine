package com.message.unitedmessageengine.core.worker.first.sender.repository;

import com.message.unitedmessageengine.entity.MessageEntity;

import java.util.List;

public interface SenderBatchRepository {

    void batchUpdate(List<MessageEntity> batchList);

}
