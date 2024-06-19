package com.message.unitedmessageengine.core.worker.sender.repository;

import com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto;
import com.message.unitedmessageengine.entity.Message;

import java.util.List;

public interface SenderBatchRepository {

    void batchUpdate(List<ExternalMessageDto> batchList);

    void batchInsert(List<Message> batchList);

}
