package com.message.unitedmessageengine.core.worker.first.repository;

import com.message.unitedmessageengine.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SenderRepository extends JpaRepository<MessageEntity, Long>, SenderBatchRepository {
//    @Query("""
//              SELECT new com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto(
//              m.serviceType,
//              m.messageId,
//              m.toNumber,
//              m.fromNumber,
//              m.content)
//              FROM Message m WHERE m.statusCode = "W" AND m.serviceDivision = :serviceDivision
//            """)
//    List<SMSDto> findAllMessages(@Param("serviceDivision") String serviceDivision, Pageable pageable);

    List<MessageEntity> findByStatusCodeAndServiceDivisionOrderBySendDttAsc(String statusCode, String serviceDivision, Pageable pageable);

    List<MessageEntity> findByStatusCodeAndResultCodeIsNullAndSendDttBefore(String statusCode, LocalDateTime sendDttBefore);

}
