package com.message.unitedmessageengine.core.first.repository;

import com.message.unitedmessageengine.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long>, MessageBatchRepository {

    List<MessageEntity> findByStatusCodeAndServiceDivisionOrderBySendDttAsc(String statusCode, String serviceDivision, Pageable pageable);

}
