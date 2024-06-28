package com.message.unitedmessageengine.core.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.message.unitedmessageengine.entity.MessageEntity;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

// MessageEntityDeserializer
public class MessageEntityDeserializer implements Deserializer<MessageEntity> {

    @Override
    public MessageEntity deserialize(String topic, byte[] data) {
        try {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(data, MessageEntity.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing MessageEntity", e);
        }
    }
}