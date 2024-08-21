package com.message.unitedmessageengine.core.first.repository;

import com.message.unitedmessageengine.entity.MessageEntity;

import java.util.List;

public interface MessageBatchRepository {

    void batchUpdate(List<MessageEntity> batchList);

}
