package com.message.unitedmessageengine.config;

import com.message.unitedmessageengine.core.kafka.serializer.MessageEntityDeserializer;
import com.message.unitedmessageengine.core.kafka.serializer.MessageEntitySerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String MESSAGE_CONSUMER_BEAN_NAME = "MESSAGE_CONSUMER_BEAN_NAME";
    private final Map<String, Object> CONSUMER_CONFIG = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.90.0.72:9092",
            ConsumerConfig.GROUP_ID_CONFIG, "group_1",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageEntityDeserializer.class,
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"
    );

    private final Map<String, Object> PRODUCER_CONFIG = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.90.0.72:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MessageEntitySerializer.class,
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 1000,
            ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 1000
    );

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        producerFactory.updateConfigs(PRODUCER_CONFIG);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean(name = MESSAGE_CONSUMER_BEAN_NAME)
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory, ConcurrentKafkaListenerContainerFactoryConfigurer configurer
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 카프카 수동 커밋 비동기 모드로 전환
        consumerFactory.updateConfigs(CONSUMER_CONFIG);
        configurer.configure(factory, consumerFactory);
        return factory;
    }
}