package com.message.unitedmessageengine.core.worker.sender.repository;

import com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto;
import com.message.unitedmessageengine.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SenderRepository extends JpaRepository<Message, Long>, SenderBatchRepository {
    @Query("""
              SELECT new com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto(
              m.serviceType,
              m.messageId,
              m.toNumber,
              m.fromNumber,
              m.content)
              FROM Message m WHERE m.statusCode = "W" AND m.serviceDivision = :serviceDivision
            """)
    List<ExternalMessageDto> findAllMessages(@Param("serviceDivision") String serviceDivision, Pageable pageable);

}
