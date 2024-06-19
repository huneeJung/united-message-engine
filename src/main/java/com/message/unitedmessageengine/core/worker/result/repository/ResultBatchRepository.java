package com.message.unitedmessageengine.core.worker.result.repository;

import com.message.unitedmessageengine.core.worker.result.dto.AckDto;
import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;

import java.util.List;

public interface ResultBatchRepository {

    void batchUpdateResult(List<ResultDto> batchList);

    void batchUpdateAck(List<AckDto> batchList);

}
