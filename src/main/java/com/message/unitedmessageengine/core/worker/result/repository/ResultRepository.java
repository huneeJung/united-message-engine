package com.message.unitedmessageengine.core.worker.result.repository;

import com.message.unitedmessageengine.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Locale;

@Repository
public interface ResultRepository extends JpaRepository<Message, Locale>, ResultBatchRepository {
}
