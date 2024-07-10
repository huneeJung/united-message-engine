package com.message.unitedmessageengine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

    @Value("${topic.name:sendMessageEvent}")
    private String topic;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topic)
                .partitions(3) // 토픽의 파티션 개수를 3으로 설정
                .replicas(1) // 복제본 개수를 1로 설정
                .build();
    }
}
