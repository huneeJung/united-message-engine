package com.message.unitedmessageengine.sample;

import com.message.unitedmessageengine.entity.MessageEntity;
import com.message.unitedmessageengine.entity.MessageImageEntity;

import java.util.List;

public interface DummyBatchRepository {

    void batchInsertMessage(List<MessageEntity> batchList);

    void batchInsertImage(List<MessageImageEntity> batchList);

}
