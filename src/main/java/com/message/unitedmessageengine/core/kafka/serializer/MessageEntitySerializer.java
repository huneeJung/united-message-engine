package com.message.unitedmessageengine.core.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.message.unitedmessageengine.entity.MessageEntity;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

// MessageEntitySerializer
public class MessageEntitySerializer implements Serializer<MessageEntity> {

    @Override
    public byte[] serialize(String topic, MessageEntity data) {
        try {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing MessageEntity", e);
        }
    }
}