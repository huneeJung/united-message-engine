package com.message.unitedmessageengine.core.worker.first.result.repository;

import com.message.unitedmessageengine.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ResultRepository extends JpaRepository<MessageEntity, Long>, ResultBatchRepository {


    @Modifying
    @Transactional
    @Query("UPDATE MessageEntity m SET m.statusCode = :statusCode, m.resultCode = :resultCode, m.resultMessage = :resultMessage WHERE m.messageId = :messageId")
    int updateMessageResult(
            @Param("statusCode") String statusCode, @Param("resultCode") String resultCode,
            @Param("resultMessage") String resultMessage, @Param("messageId") String messageId
    );

}
