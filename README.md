# UMS Message Relay Engine

통신사 메시지 발송을 위한 중계 엔진 시스템입니다. NIO 기반의 비동기 소켓 통신을 통해 고성능 메시지 처리를 구현하였으며, Kafka를 활용한 메시지 큐잉 시스템을 포함합니다.

## 📋 목차
- [시스템 개요](#시스템-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [주요 구현사항](#주요-구현사항)
- [성능 최적화](#성능-최적화)
- [보안](#보안)
- [향후 개선사항](#향후-개선사항)

## 시스템 개요
UMS(Unified Messaging Service) 중계 엔진은 기업형 메시징 서비스를 위한 고성능 메시지 처리 시스템입니다. 
대량의 SMS, MMS, LMS 메시지를 안정적으로 통신사에 전달하며, 비동기 처리 방식을 통해 높은 처리량을 제공합니다.

## 주요 기능
- 대용량 메시지 발송 처리
- 실시간 발송 결과 수신 (Report) 처리
- TPS(Transaction Per Second) 기반 메시지 제어
- 이미지 캐싱을 통한 MMS 최적화
- 실시간 모니터링 및 상태 관리

## 기술 스택
- **Language**: Java 21 (Virtual Thread 활용)
- **Build Tool**: Gradle
- **Message Queue**: Apache Kafka
- **Database**: 
  - JPA (엔티티 관리)
  - Spring Data JPA (기본 CRUD)
  - JdbcTemplate (배치 처리 최적화)
- **Socket**: Java NIO (Non-blocking I/O)
- **Cache**: Guava Cache (이미지 인메모리 캐싱)
- **Security**: Jasypt (설정 암호화)
- **Others**:
  - Spring Boot
  - Lombok
  - SLF4J & Logback

## 시스템 아키텍처
### 메시지 처리 흐름
```
DB → Kafka Producer → Kafka Topic → Consumer → Protocol Converter → Socket Channel → Gateway
                                                                              
                                                         Result Processing ← Report Channel
```

### 통신 채널 구조
1. **SEND Channel**
   - NIO SocketChannel 기반 비동기 통신
   - 메시지 발송 프로토콜 처리
   - 버퍼 관리 및 흐름 제어

2. **REPORT Channel**
   - 발송 결과 수신 전용 채널
   - 실시간 처리 결과 업데이트

## 주요 구현사항
### 1. 비동기 소켓 통신
```java
// NIO SocketChannel 구현
   var channel = SocketChannel.open();
   channel.socket().setSoTimeout(readTimeout);
   channel.socket().setSendBufferSize(1024 * 1024);
   channel.socket().setReceiveBufferSize(1024 * 1024);
   channel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
   channel.configureBlocking(false);
   socketChannelService.authenticate(type, channel);
```

### 2. 버퍼 오버플로우 제어
```java
// Virtual Thread를 활용한 버퍼 제어
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      executor.submit(() -> {
         try {
            var sendBuffer = ByteBuffer.wrap(payload);
            var cnt = 0;
            while (cnt < payload.length) cnt += sendChannel.write(sendBuffer);
            mainSendChannel.setLastUsedTime(Instant.now());
            mainSendChannelQueue.add(mainSendChannel);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      });
   }
```

### 3. Kafka 설정
```java
   @Configuration
   @RequiredArgsConstructor
   public class KafkaConfig {

      public static final String MESSAGE_CONSUMER_BEAN_NAME = "MESSAGE_CONSUMER_BEAN_NAME";

      private final Map<String, Object> PRODUCER_CONFIG = Map.of(
               ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.90.4.211:9092",
               ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
               ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MessageEntitySerializer.class,
               ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000,
               ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 1000,
               ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 1000,
               ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
      );

      private final Map<String, Object> CONSUMER_CONFIG = Map.of(
               ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.90.4.211:9092",
               ConsumerConfig.GROUP_ID_CONFIG, "group_1",
               ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
               ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageEntityDeserializer.class,
               ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000,
               ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"
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
```

### 4. 이미지 캐싱
```java
   @Configuration
   public class CacheConfig {

      @Value("${cache.size:100}")
      private Integer cacheSize;

      @Value("${cache.expireTime:1800000}")
      private Integer cacheExpireTime;

      @Bean
      public Cache<Long, byte[]> imageCache() {
         return CacheBuilder.newBuilder()
                  .maximumSize(cacheSize)
                  .expireAfterAccess(cacheExpireTime, TimeUnit.MILLISECONDS)
                  .build();
      }

   }
```

## 성능 최적화
1. **NIO 채널 활용**
   - 비동기 I/O로 리소스 사용 최적화
   - Selector 기반 다중 채널 관리

2. **Virtual Thread 활용**
   - 버퍼 제어를 위한 경량 스레드 사용
   - 플랫폼 스레드 대비 리소스 효율성 향상

3. **배치 처리 최적화**
   - JdbcTemplate 활용한 벌크 연산
   - 배치 사이즈 조정을 통한 성능 튜닝

## 보안
1. **설정 암호화**
   - Jasypt 활용한 중요 정보 암호화

2. **접근 제어**
   - IP 기반 접근 통제
   - 세션별 TPS 제어

## 향후 개선사항
- [ ] Reactive 스택 도입 검토
- [ ] 버퍼 제어 로직 개선
- [ ] 모니터링 시스템 강화
- [ ] 장애 복구 프로세스 개선
