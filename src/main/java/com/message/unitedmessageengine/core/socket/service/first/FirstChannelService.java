package com.message.unitedmessageengine.core.socket.service.first;

import com.message.unitedmessageengine.core.socket.service.ChannelService;
import com.message.unitedmessageengine.core.socket.vo.ConnectVo;
import com.message.unitedmessageengine.core.socket.vo.PingVo;
import com.message.unitedmessageengine.core.socket.vo.ReportAckVo;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.first.result.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;

import static com.message.unitedmessageengine.constant.ProtocolConstant.First.REPORT;
import static com.message.unitedmessageengine.constant.ProtocolConstant.First.SEND;
import static com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType.*;
import static com.message.unitedmessageengine.core.queue.QueueManager.RESULT_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("firstChannelService")
public class FirstChannelService implements ChannelService {

    private final JdbcTemplate jdbcTemplate;
    @Qualifier("firstTranslator")
    private final FirstTranslator translator;

    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

    public void authenticate(String channelType, SocketChannel channel) {
        try {
            var connectVO = ConnectVo.builder().USERNAME(username).PASSWORD(password)
                    .LINE(channelType).VERSION(version).build();
            var payload = translator.convertToBytes(connectVO);
            var authPayload = translator.addTcpFraming(CONNECT, payload);
            if (authPayload.isEmpty()) return;
            var authBuffer = ByteBuffer.wrap(authPayload.get());
            channel.write(authBuffer);
        } catch (IOException e) {
            log.info("[{} Channel] 인증 에러 발생", channelType);
        }
    }

    public void sendPing(SocketChannel senderChannel) {
        try {
            var pingVO = new PingVo();
            var payload = translator.convertToBytes(pingVO);
            var pingPayload = translator.addTcpFraming(PING, payload);
            if (pingPayload.isEmpty()) return;
            var pingBuffer = ByteBuffer.wrap(pingPayload.get());
            senderChannel.write(pingBuffer);
        } catch (IOException e) {
            log.warn("[PING] 처리 에러 발생 ::: messageEntity {}", e.getMessage());
            log.warn("", e);
        }
    }

    public void processSendResponse(Queue<String> dataQueue) {
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) {
//                log.info("[PONG] 수신 ::: key {}", key);
                continue;
            }
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(SEND, mapData);
                continue;
            }
            if (mapData.getOrDefault("CODE", "100").equals("100")) return;

            jdbcTemplate.update("""
                            UPDATE MESSAGE SET STATUS_CODE=?, RESULT_CODE=?, RESULT_MESSAGE=? 
                            where MESSAGE_ID=?
                            """,
                    "F", mapData.get("CODE"), mapData.get("DATA"), key);
//            log.info("[ACK QUEUE] 메시지 결과 삽입 ::: messageId {}", key);
        }
    }

    public void processReportResponse(SocketChannel reportChannel, Queue<String> dataQueue) {
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) {
//                log.info("[PONG] 수신 ::: key {}", key);
                continue;
            }
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(REPORT, mapData);
                continue;
            }

            RESULT_QUEUE.add(
                    ResultDto.builder()
                            .messageId(key)
                            .resultCode(mapData.get("CODE"))
                            .resultMessage(mapData.get("DATA"))
                            .build()
            );
//            log.info("[RESULT QUEUE] 메시지 결과 삽입 ::: messageId {}", key);
//            resultRepository.updateMessageResult("C", mapData.get("CODE"), mapData.get("DATA"), key);
            try {
                var reportAckVo = new ReportAckVo(key);
                var payload = translator.convertToBytes(reportAckVo);
                var reportAckPayload = translator.addTcpFraming(ACK, payload);
                if (reportAckPayload.isEmpty()) continue;
                var reportAckBuffer = ByteBuffer.wrap(reportAckPayload.get());
                reportChannel.write(reportAckBuffer);
//                log.info("[REPORT CHANNEL] 결과 수신 응답 성공 ::: messageId {}", key);
            } catch (IOException e) {
                log.info("[REPORT CHANNEL] 결과 수신 응답 실패 ::: messageId {}", key);
            }
        }
    }

    private void checkAuth(String channelType, Map<String, String> authData) {
        var code = authData.get("CODE");
        var messageEntity = authData.get("DATA");
        if (code.equals("100")) {
            log.info("[{} Channel] 인증 완료 ::: code {}, messageEntity {}", channelType, code, messageEntity);
        } else {
            throw new RuntimeException(String.format("[Channel] 인증 실패 ::: code %s, messageEntity %s", code, messageEntity));
        }
    }

}
