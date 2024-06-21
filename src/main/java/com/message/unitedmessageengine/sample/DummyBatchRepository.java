package com.message.unitedmessageengine.sample;

import com.message.unitedmessageengine.entity.ImageEntity;
import com.message.unitedmessageengine.entity.MessageEntity;

import java.util.List;

public interface DummyBatchRepository {

    void batchInsertMessage(List<MessageEntity> batchList);

    void batchInsertImage(List<ImageEntity> batchList);

}
