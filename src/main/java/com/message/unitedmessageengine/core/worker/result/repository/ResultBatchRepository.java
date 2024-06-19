package com.message.unitedmessageengine.core.worker.result.repository;

import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;

import java.util.List;

public interface ResultBatchRepository {

    void batchUpdate(List<ResultDto> batchList);

}
